package brs.grpc.handlers

import brs.Block
import brs.Blockchain
import brs.BlockchainProcessor
import brs.Generator
import brs.grpc.StreamResponseGrpcApiHandler
import brs.grpc.proto.BrsApi
import brs.grpc.proto.toByteString
import brs.util.delegates.Atomic
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

class GetMiningInfoHandler(blockchainProcessor: BlockchainProcessor, blockchain: Blockchain, private val generator: Generator) : StreamResponseGrpcApiHandler<Empty, BrsApi.MiningInfo> {
    /**
     * Listener should close connection if it receives null.
     */
    private val listeners = mutableSetOf<(BrsApi.MiningInfo?) -> Unit>()
    private val listenersLock = Mutex()
    private var currentMiningInfo by Atomic<BrsApi.MiningInfo?>()
    private val miningInfoLock = Mutex()

    init {
        runBlocking {
            blockchainProcessor.addListener(BlockchainProcessor.Event.BLOCK_PUSHED) { block: Block -> onBlock(block) }
            onBlock(blockchain.lastBlock)
        }
    }

    private suspend fun onBlock(block: Block) {
        miningInfoLock.withLock {
            val nextGenSig = generator.calculateGenerationSignature(block.generationSignature, block.generatorId)
            val miningInfo = currentMiningInfo
            if (miningInfo == null || !Arrays.equals(miningInfo.generationSignature.toByteArray(), nextGenSig) || miningInfo.height - 1 != block.height || miningInfo.baseTarget != block.baseTarget) {
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

    private suspend fun notifyListeners(miningInfo: BrsApi.MiningInfo) {
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

    private suspend fun addListener(listener: (BrsApi.MiningInfo?) -> Unit) {
        listenersLock.withLock {
            listeners.add(listener)
        }
    }

    override suspend fun handleStreamRequest(request: Empty, responseObserver: StreamObserver<BrsApi.MiningInfo>) {
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
