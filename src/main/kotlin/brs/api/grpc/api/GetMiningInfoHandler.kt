package brs.api.grpc.api

import brs.api.grpc.StreamResponseGrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.toByteString
import brs.entity.Block
import brs.services.BlockchainProcessorService
import brs.services.BlockchainService
import brs.services.GeneratorService
import brs.util.delegates.Atomic
import brs.util.sync.Mutex
import brs.util.sync.withLock
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver

class GetMiningInfoHandler(
    blockchainProcessorService: BlockchainProcessorService,
    blockchainService: BlockchainService,
    private val generatorService: GeneratorService
) : StreamResponseGrpcApiHandler<Empty, BrsApi.MiningInfo> {
    /**
     * Listener should close connection if it receives null.
     */
    private val listeners = mutableSetOf<(BrsApi.MiningInfo?) -> Unit>()
    private val listenersLock = Mutex()
    private var currentMiningInfo by Atomic<BrsApi.MiningInfo?>(null)
    private val miningInfoLock = Mutex()

    init {
        blockchainProcessorService.addListener(BlockchainProcessorService.Event.BLOCK_PUSHED) { block: Block ->
            onBlock(
                block
            )
        }
        onBlock(blockchainService.lastBlock)
    }

    private fun onBlock(block: Block) {
        miningInfoLock.withLock {
            val nextGenSig = generatorService.calculateGenerationSignature(block.generationSignature, block.generatorId)
            val miningInfo = currentMiningInfo
            if (miningInfo == null || !nextGenSig.contentEquals(miningInfo.generationSignature.toByteArray())
                || miningInfo.height - 1 != block.height || miningInfo.baseTarget != block.baseTarget
            ) {
                val newMiningInfo = BrsApi.MiningInfo.newBuilder()
                    .setGenerationSignature(nextGenSig.toByteString())
                    .setHeight(block.height + 1)
                    .setBaseTarget(block.baseTarget)
                    .build()
                currentMiningInfo = newMiningInfo
                notifyListeners(newMiningInfo)
            }
        }
    }

    private fun notifyListeners(miningInfo: BrsApi.MiningInfo) {
        listenersLock.withLock {
            listeners.removeIf { listener ->
                try {
                    listener.invoke(miningInfo)
                    return@removeIf false
                } catch (e: Exception) {
                    try {
                        listener.invoke(null)
                    } catch (ignored: Exception) {
                        // Ignore any errors attempting to disconnect as we may already be disconnected
                    }

                    return@removeIf true
                }
            }
        }
    }

    private fun addListener(listener: (BrsApi.MiningInfo?) -> Unit) {
        listenersLock.withLock {
            listeners.add(listener)
        }
    }

    override fun handleStreamRequest(request: Empty, responseObserver: StreamObserver<BrsApi.MiningInfo>) {
        responseObserver.onNext(currentMiningInfo)
        addListener { miningInfo ->
            if (miningInfo == null) {
                responseObserver.onCompleted()
            } else {
                responseObserver.onNext(miningInfo)
            }
        }
    }
}
