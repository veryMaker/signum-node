package brs.at

import brs.objects.Constants
import brs.entity.DependencyProvider
import brs.objects.FluxValues


class AtConstants(private val dp: DependencyProvider) {
    //platform based
    // TODO replace with a single object for each version
    private val minFee = mutableMapOf<Short, Long>()
    private val stepFee = mutableMapOf<Short, Long>()
    private val maxSteps = mutableMapOf<Short, Long>()
    private val apiStepMultiplier = mutableMapOf<Short, Long>()
    private val costPerPage = mutableMapOf<Short, Long>()
    private val maxWaitForNumOfBlocks = mutableMapOf<Short, Long>()
    private val maxSleepBetweenBlocks = mutableMapOf<Short, Long>()
    private val pageSize = mutableMapOf<Short, Long>()
    private val maxMachineCodePages = mutableMapOf<Short, Long>()
    private val maxMachineDataPages = mutableMapOf<Short, Long>()
    private val maxMachineUserStackPages = mutableMapOf<Short, Long>()
    private val maxMachineCallStackPages = mutableMapOf<Short, Long>()
    private val blocksForRandom = mutableMapOf<Short, Long>()
    private val maxPayloadForBlock = mutableMapOf<Short, Long>()
    private val averageBlockMinutes = mutableMapOf<Short, Long>()


    init {
        // constants for AT version 1
        minFee[1.toShort()] = 1000L
        stepFee[1.toShort()] = Constants.ONE_BURST / 10L
        maxSteps[1.toShort()] = 2000L
        apiStepMultiplier[1.toShort()] = 10L

        costPerPage[1.toShort()] = Constants.ONE_BURST

        maxWaitForNumOfBlocks[1.toShort()] = 31536000L
        maxSleepBetweenBlocks[1.toShort()] = 31536000L

        pageSize[1.toShort()] = 256L

        maxMachineCodePages[1.toShort()] = 10L
        maxMachineDataPages[1.toShort()] = 10L
        maxMachineUserStackPages[1.toShort()] = 10L
        maxMachineCallStackPages[1.toShort()] = 10L

        blocksForRandom[1.toShort()] = 15L //for testing 2 -> normally 1440
        maxPayloadForBlock[1.toShort()] =
            dp.fluxCapacitorService.getValue(FluxValues.MAX_PAYLOAD_LENGTH) / 2L //use at max half size of the block.
        averageBlockMinutes[1.toShort()] = 4L
        // end of AT version 1

        // constants for AT version 2
        minFee[2.toShort()] = 1000L
        stepFee[2.toShort()] = Constants.FEE_QUANT / 10L
        maxSteps[2.toShort()] = 100000L
        apiStepMultiplier[2.toShort()] = 10L

        costPerPage[2.toShort()] = Constants.FEE_QUANT * 10

        maxWaitForNumOfBlocks[2.toShort()] = 31536000L
        maxSleepBetweenBlocks[2.toShort()] = 31536000L

        pageSize[2.toShort()] = 256L

        maxMachineCodePages[2.toShort()] = 20L
        maxMachineDataPages[2.toShort()] = 10L
        maxMachineUserStackPages[2.toShort()] = 10L
        maxMachineCallStackPages[2.toShort()] = 10L

        blocksForRandom[2.toShort()] = 15L //for testing 2 -> normally 1440
        maxPayloadForBlock[2.toShort()] =
            dp.fluxCapacitorService.getValue(FluxValues.MAX_PAYLOAD_LENGTH) / 2L //use at max half size of the block.
        averageBlockMinutes[2.toShort()] = 4L
        // end of AT version 2
    }

    fun atVersion(blockHeight: Int): Short {
        return dp.fluxCapacitorService.getValue(FluxValues.AT_VERSION, blockHeight)
    }

    fun stepFee(height: Int): Long {
        return stepFee[atVersion(height)]!!
    }

    fun maxSteps(height: Int): Long {
        return maxSteps[atVersion(height)]!!
    }

    fun apiStepMultiplier(height: Int): Long {
        return apiStepMultiplier[atVersion(height)]!!
    }

    fun costPerPage(height: Int): Long {
        return costPerPage[atVersion(height)]!!
    }

    fun getMaxWaitForNumOfBlocks(height: Int): Long {
        return maxWaitForNumOfBlocks[atVersion(height)]!!
    }

    fun maxSleepBetweenBlocks(height: Int): Long {
        return maxSleepBetweenBlocks[atVersion(height)]!!
    }

    fun pageSize(height: Int): Long {
        return pageSize[atVersion(height)]!!
    }

    fun maxMachineCodePages(height: Int): Long {
        return maxMachineCodePages[atVersion(height)]!!
    }

    fun maxMachineDataPages(height: Int): Long {
        return maxMachineDataPages[atVersion(height)]!!
    }

    fun maxMachineUserStackPages(height: Int): Long {
        return maxMachineUserStackPages[atVersion(height)]!!
    }

    fun maxMachineCallStackPages(height: Int): Long {
        return maxMachineCallStackPages[atVersion(height)]!!
    }

    fun blocksForRandom(height: Int): Long {
        return blocksForRandom[atVersion(height)]!!
    }

    fun maxPayloadForBlock(height: Int): Long {
        return maxPayloadForBlock[atVersion(height)]!!
    }

    fun averageBlockMinutes(height: Int): Long {
        return averageBlockMinutes[atVersion(height)]!!
    }

    companion object {
        const val AT_ID_SIZE = 8
    }
}
