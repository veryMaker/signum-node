package brs

import brs.props.Props
import brs.services.BlockService
import brs.util.MiningPlot
import brs.util.logging.safeDebug
import brs.util.logging.safeInfo
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jocl.*
import org.jocl.CL.*
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.math.min

class OCLPoC(dp: DependencyProvider) {

    private val logger = LoggerFactory.getLogger(OCLPoC::class.java)

    private val hashesPerBatch: Int
    private val memPercent: Int

    private var ctx: cl_context? = null
    private var queue: cl_command_queue? = null
    private var program: cl_program? = null
    private var genKernel: cl_kernel? = null
    private var getKernel: cl_kernel? = null
    private var getKernel2: cl_kernel? = null

    var maxItems: Long = 0
        private set
    private val maxGroupItems: Long

    private val oclLock = Mutex()

    init {
        val propertyService = dp.propertyService
        hashesPerBatch = propertyService.get(Props.GPU_HASHES_PER_BATCH)
        memPercent = propertyService.get(Props.GPU_MEM_PERCENT)

        try {
            val autoChoose = propertyService.get(Props.GPU_AUTODETECT)
            setExceptionsEnabled(true)

            val platformIndex: Int
            val deviceIndex: Int
            if (autoChoose) {
                val ac = autoChooseDevice() ?: throw OCLCheckerException("Autochoose failed to select a GPU")
                platformIndex = ac.platform
                deviceIndex = ac.device
                logger.safeInfo { "Choosing Platform ${platformIndex} - DeviceId: ${deviceIndex}" }
            } else {
                platformIndex = propertyService.get(Props.GPU_PLATFORM_IDX)
                deviceIndex = propertyService.get(Props.GPU_DEVICE_IDX)
            }

            val numPlatforms = IntArray(1)
            clGetPlatformIDs(0, null, numPlatforms)

            if (numPlatforms[0] == 0) {
                throw OCLCheckerException("No OpenCL platforms found")
            }

            if (numPlatforms[0] <= platformIndex) {
                throw OCLCheckerException("Invalid OpenCL platform index")
            }

            val platforms = arrayOfNulls<cl_platform_id>(numPlatforms[0])
            clGetPlatformIDs(platforms.size, platforms, null)

            val platform = platforms[platformIndex]

            val numDevices = IntArray(1)
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 0, null, numDevices)

            if (numDevices[0] == 0) {
                throw OCLCheckerException("No OpenCl Devices found")
            }

            if (numDevices[0] <= deviceIndex) {
                throw OCLCheckerException("Invalid OpenCL device index")
            }

            val devices = arrayOfNulls<cl_device_id>(numDevices[0])
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, devices.size, devices, null)

            val device = devices[deviceIndex]

            if (!checkAvailable(device)) {
                throw OCLCheckerException("Chosen GPU must be available")
            }

            if (!checkLittleEndian(device)) {
                throw OCLCheckerException("Chosen GPU must be little endian")
            }

            val ctxProps = cl_context_properties()
            ctxProps.addProperty(CL_CONTEXT_PLATFORM.toLong(), platform)

            ctx = clCreateContext(ctxProps, 1, arrayOf<cl_device_id>(device!!), null, null, null)
            queue = clCreateCommandQueueWithProperties(ctx, device, cl_queue_properties(), null)

            val source: String
            try {
                source = String(Files.readAllBytes(Paths.get("genscoop.cl")))
            } catch (e: IOException) {
                throw OCLCheckerException("Cannot read ocl file", e)
            }

            program = clCreateProgramWithSource(ctx, 1, arrayOf(source), null, null)
            clBuildProgram(program, 0, null, null, null, null)

            genKernel = clCreateKernel(program, "generate_scoops", null)
            getKernel = clCreateKernel(program, "get_scoops", null)
            getKernel2 = clCreateKernel(program, "get_scoops2", null)

            val genGroupSize = LongArray(1)
            val getGroupSize = LongArray(1)
            clGetKernelWorkGroupInfo(genKernel, device, CL_KERNEL_WORK_GROUP_SIZE, 8,
                    Pointer.to(genGroupSize), null)
            clGetKernelWorkGroupInfo(getKernel, device, CL_KERNEL_WORK_GROUP_SIZE, 8,
                    Pointer.to(getGroupSize), null)

            maxGroupItems = Math.min(genGroupSize[0], getGroupSize[0])

            if (maxGroupItems <= 0) {
                throw OCLCheckerException(
                        "OpenCL init error. Invalid max group items: $maxGroupItems")
            }

            val maxItemsByComputeUnits = getComputeUnits(device) * maxGroupItems

            maxItems = Math.min(calculateMaxItemsByMem(device), maxItemsByComputeUnits)

            if (maxItems % maxGroupItems != 0L) {
                maxItems -= maxItems % maxGroupItems
            }

            if (maxItems <= 0) {
                throw OCLCheckerException(
                        "OpenCL init error. Invalid calculated max items: $maxItems")
            }
            logger.safeInfo { "OCL max items: ${maxItems}" }
        } catch (e: CLException) {
            if (true) {
                logger.safeInfo(e) { "OpenCL exception: ${e.message}" }
            }
            runBlocking { destroy() }
            throw OCLCheckerException("OpenCL exception", e)
        }

    }

    suspend fun validatePoC(blocks: Collection<Block>, pocVersion: Int, blockService: BlockService) {
        try {
            if (true) {
                logger.safeDebug { "starting ocl verify for: ${blocks.size}" }
            }
            val scoopsOut = ByteArray(MiningPlot.SCOOP_SIZE * blocks.size)

            var jobSize = blocks.size.toLong()
            if (jobSize % maxGroupItems != 0L) {
                jobSize += maxGroupItems - jobSize % maxGroupItems
            }

            check(jobSize <= maxItems) { "Attempted to validate too many blocks at once with OCL" }

            val ids = LongArray(blocks.size)
            val nonces = LongArray(blocks.size)
            val scoopNums = IntArray(blocks.size)

            val buffer = ByteBuffer.allocate(16)
            for ((i, block) in blocks.withIndex()) {
                buffer.order(ByteOrder.LITTLE_ENDIAN)
                buffer.putLong(block.generatorId)
                buffer.putLong(block.nonce)
                buffer.flip()
                buffer.order(ByteOrder.BIG_ENDIAN)
                ids[i] = buffer.long
                nonces[i] = buffer.long
                buffer.clear()
                scoopNums[i] = blockService.getScoopNum(block)
            }
            if (true) {
                logger.safeDebug { "finished preprocessing: ${blocks.size}" }
            }

            oclLock.withLock {
                if (ctx == null) {
                    throw OCLCheckerException("OCL context no longer exists")
                }

                var idMem: cl_mem? = null
                var nonceMem: cl_mem? = null
                var bufferMem: cl_mem? = null
                var scoopNumMem: cl_mem? = null
                var scoopOutMem: cl_mem? = null

                try {
                    idMem = clCreateBuffer(ctx, CL_MEM_READ_ONLY or CL_MEM_COPY_HOST_PTR, 8L * blocks.size,
                            Pointer.to(ids), null)
                    nonceMem = clCreateBuffer(ctx, CL_MEM_READ_ONLY or CL_MEM_COPY_HOST_PTR,
                            8L * blocks.size, Pointer.to(nonces), null)
                    bufferMem = clCreateBuffer(ctx, CL_MEM_READ_WRITE,
                            (MiningPlot.PLOT_SIZE + 16).toLong() * blocks.size, null, null)
                    scoopNumMem = clCreateBuffer(ctx, CL_MEM_READ_ONLY or CL_MEM_COPY_HOST_PTR,
                            4L * blocks.size, Pointer.to(scoopNums), null)
                    scoopOutMem = clCreateBuffer(ctx, CL_MEM_READ_WRITE,
                            MiningPlot.SCOOP_SIZE.toLong() * blocks.size, null, null)

                    val totalSize = intArrayOf(blocks.size)

                    clSetKernelArg(genKernel, 0, Sizeof.cl_mem.toLong(), Pointer.to(idMem!!))
                    clSetKernelArg(genKernel, 1, Sizeof.cl_mem.toLong(), Pointer.to(nonceMem!!))
                    clSetKernelArg(genKernel, 2, Sizeof.cl_mem.toLong(), Pointer.to(bufferMem!!))
                    clSetKernelArg(genKernel, 5, Sizeof.cl_int.toLong(), Pointer.to(totalSize))

                    var c = 0
                    val step = hashesPerBatch
                    val cur = IntArray(1)
                    val st = IntArray(1)
                    while (c < 8192) {
                        cur[0] = c
                        st[0] = if (c + step > 8192) 8192 - c else step
                        clSetKernelArg(genKernel, 3, Sizeof.cl_int.toLong(), Pointer.to(cur))
                        clSetKernelArg(genKernel, 4, Sizeof.cl_int.toLong(), Pointer.to(st))
                        clEnqueueNDRangeKernel(queue, genKernel, 1, null, longArrayOf(jobSize),
                                longArrayOf(maxGroupItems), 0, null, null)

                        c += st[0]
                    }

                    if (pocVersion == 2) { // TODO why are these the same...?
                        clSetKernelArg(getKernel2, 0, Sizeof.cl_mem.toLong(), Pointer.to(scoopNumMem!!))
                        clSetKernelArg(getKernel2, 1, Sizeof.cl_mem.toLong(), Pointer.to(bufferMem))
                        clSetKernelArg(getKernel2, 2, Sizeof.cl_mem.toLong(), Pointer.to(scoopOutMem!!))
                        clSetKernelArg(getKernel2, 3, Sizeof.cl_int.toLong(), Pointer.to(totalSize))
                        clEnqueueNDRangeKernel(queue, getKernel2, 1, null, longArrayOf(jobSize),
                                longArrayOf(maxGroupItems), 0, null, null)
                    } else {
                        clSetKernelArg(getKernel, 0, Sizeof.cl_mem.toLong(), Pointer.to(scoopNumMem!!))
                        clSetKernelArg(getKernel, 1, Sizeof.cl_mem.toLong(), Pointer.to(bufferMem))
                        clSetKernelArg(getKernel, 2, Sizeof.cl_mem.toLong(), Pointer.to(scoopOutMem!!))
                        clSetKernelArg(getKernel, 3, Sizeof.cl_int.toLong(), Pointer.to(totalSize))
                        clEnqueueNDRangeKernel(queue, getKernel, 1, null, longArrayOf(jobSize),
                                longArrayOf(maxGroupItems), 0, null, null)
                    }

                    clEnqueueReadBuffer(queue, scoopOutMem, true, 0,
                            MiningPlot.SCOOP_SIZE.toLong() * blocks.size, Pointer.to(scoopsOut), 0, null, null)
                } catch (e: Exception) {
                    logger.safeInfo { "GPU error. Try to set a lower value on GPU.HashesPerBatch in properties." }
                    return
                } finally {
                    if (idMem != null) {
                        clReleaseMemObject(idMem)
                    }
                    if (nonceMem != null) {
                        clReleaseMemObject(nonceMem)
                    }
                    if (bufferMem != null) {
                        clReleaseMemObject(bufferMem)
                    }
                    if (scoopNumMem != null) {
                        clReleaseMemObject(scoopNumMem)
                    }
                    if (scoopOutMem != null) {
                        clReleaseMemObject(scoopOutMem)
                    }
                }
            }

            if (true) {
                logger.safeDebug { "finished ocl, doing rest: ${blocks.size}" }
            }

            val scoopsBuffer = ByteBuffer.wrap(scoopsOut)
            val scoop = ByteArray(MiningPlot.SCOOP_SIZE)

            blocks.forEach { block ->
                try {
                    scoopsBuffer.get(scoop)
                    blockService.preVerify(block, scoop)
                } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
                    throw PreValidateFailException("Block failed to prevalidate", e, block)
                }
            }
            if (true) {
                logger.safeDebug { "finished rest: ${blocks.size}" }
            }
        } catch (e: CLException) {
            // intentionally leave out of unverified cache. It won't slow it that much on one failure and
            // avoids infinite looping on repeat failed attempts.
            throw OCLCheckerException("OpenCL error", e)
        }

    }

    suspend fun destroy() {
        oclLock.withLock {
            if (program != null) {
                clReleaseProgram(program)
                program = null
            }
            if (genKernel != null) {
                clReleaseKernel(genKernel)
                genKernel = null
            }
            if (getKernel != null) {
                clReleaseKernel(getKernel)
                getKernel = null
            }
            if (queue != null) {
                clReleaseCommandQueue(queue)
                queue = null
            }
            if (ctx != null) {
                clReleaseContext(ctx)
                ctx = null
            }
        }
    }

    private fun checkAvailable(device: cl_device_id?): Boolean {
        if (device == null) return false
        val available = LongArray(1)
        clGetDeviceInfo(device, CL_DEVICE_AVAILABLE, Sizeof.cl_long.toLong(), Pointer.to(available), null)
        return available[0] == 1L
    }

    // idk if the kernel works on big endian, but I'm guessing not and I don't have the hardware to
    // find out
    private fun checkLittleEndian(device: cl_device_id?): Boolean {
        if (device == null) return false
        val endianLittle = LongArray(1)
        clGetDeviceInfo(device, CL_DEVICE_ENDIAN_LITTLE, Sizeof.cl_long.toLong(), Pointer.to(endianLittle), null)
        return endianLittle[0] == 1L
    }

    private fun getComputeUnits(device: cl_device_id?): Int {
        val maxComputeUnits = IntArray(1)
        clGetDeviceInfo(device, CL_DEVICE_MAX_COMPUTE_UNITS, 4, Pointer.to(maxComputeUnits), null)
        return maxComputeUnits[0]
    }

    private fun calculateMaxItemsByMem(device: cl_device_id): Long {
        val globalMemSize = LongArray(1)
        val maxMemAllocSize = LongArray(1)

        clGetDeviceInfo(device, CL_DEVICE_GLOBAL_MEM_SIZE, 8, Pointer.to(globalMemSize), null)
        clGetDeviceInfo(device, CL_DEVICE_MAX_MEM_ALLOC_SIZE, 8, Pointer.to(maxMemAllocSize), null)

        val maxItemsByGlobalMemSize = globalMemSize[0] * memPercent / 100 / MEM_PER_ITEM
        val maxItemsByMaxAllocSize = maxMemAllocSize[0] * memPercent / 100 / BUFFER_PER_ITEM

        logger.safeDebug { "Global Memory: ${globalMemSize[0]}" }
        logger.safeDebug { "Max alloc Memory: ${maxMemAllocSize[0]}" }
        logger.safeDebug { "maxItemsByGlobalMemSize: ${maxItemsByGlobalMemSize}" }
        logger.safeDebug { "maxItemsByMaxAllocSize: ${maxItemsByMaxAllocSize}" }

        return min(maxItemsByGlobalMemSize, maxItemsByMaxAllocSize)
    }

    private fun autoChooseDevice(): AutoChooseResult? {
        val numPlatforms = IntArray(1)
        clGetPlatformIDs(0, null, numPlatforms)

        if (numPlatforms[0] == 0) {
            throw OCLCheckerException("No OpenCL platforms found")
        }

        val platforms = arrayOfNulls<cl_platform_id>(numPlatforms[0])
        clGetPlatformIDs(platforms.size, platforms, null)

        var bestResult: AutoChooseResult? = null
        var bestScore: Long = 0
        var intel = false
        for (pfi in platforms.indices) {
            val platformNameSize = LongArray(1)
            clGetPlatformInfo(platforms[pfi], CL_PLATFORM_NAME, 0, null, platformNameSize)
            val platformNameChars = ByteArray(platformNameSize[0].toInt())
            clGetPlatformInfo(platforms[pfi], CL_PLATFORM_NAME, platformNameChars.size.toLong(),
                    Pointer.to(platformNameChars), null)
            val platformName = String(platformNameChars)

            logger.safeInfo { "Platform ${pfi}: ${platformName}" }

            val numDevices = IntArray(1)
            clGetDeviceIDs(platforms[pfi], CL_DEVICE_TYPE_GPU, 0, null, numDevices)

            if (numDevices[0] == 0) {
                continue
            }

            val devices = arrayOfNulls<cl_device_id>(numDevices[0])
            clGetDeviceIDs(platforms[pfi], CL_DEVICE_TYPE_GPU, devices.size, devices, null)

            for (dvi in devices.indices) {
                if (!checkAvailable(devices[dvi])) {
                    continue
                }

                if (!checkLittleEndian(devices[dvi])) {
                    continue
                }

                if (bestResult != null && platformName.toLowerCase(Locale.ENGLISH).contains("intel")) {
                    continue
                }

                val clock = LongArray(1)
                clGetDeviceInfo(devices[dvi], CL_DEVICE_MAX_CLOCK_FREQUENCY, Sizeof.cl_long.toLong(),
                        Pointer.to(clock), null)

                val maxItemsAtOnce = min(calculateMaxItemsByMem(devices[dvi]!!), getComputeUnits(devices[dvi]).toLong() * 256)

                val score = maxItemsAtOnce * clock[0]

                if (bestResult == null || score > bestScore || intel) {
                    bestResult = AutoChooseResult(pfi, dvi)
                    bestScore = score
                    if (platformName.toLowerCase(Locale.ENGLISH).contains("intel")) {
                        intel = true
                    }
                }
            }
        }

        return bestResult
    }

    private class AutoChooseResult internal constructor(internal val platform: Int, internal val device: Int)

    class OCLCheckerException : RuntimeException {
        internal constructor(message: String) : super(message) {}

        internal constructor(message: String, cause: Throwable) : super(message, cause) {}
    }

    class PreValidateFailException internal constructor(message: String, cause: Throwable, @field:Transient val block: Block) : RuntimeException(message, cause)

    companion object {
        private const val BUFFER_PER_ITEM = MiningPlot.PLOT_SIZE.toLong() + 16
        private const val MEM_PER_ITEM = (8 // id
                + 8 // nonce
                + BUFFER_PER_ITEM // buffer
                + 4 // scoop num
                + MiningPlot.SCOOP_SIZE.toLong()) // output scoop
    }
}
