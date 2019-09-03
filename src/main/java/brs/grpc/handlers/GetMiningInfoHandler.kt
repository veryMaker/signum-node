package brs.grpc.handlers

import brs.Block
import brs.Blockchain
import brs.BlockchainProcessor
import brs.Generator
import brs.grpc.StreamResponseGrpcApiHandler
import brs.grpc.proto.BrsApi
import com.google.protobuf.ByteString
import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver

import java.util.Arrays
import java.util.HashSet
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

class GetMiningInfoHandler(blockchainProcessor: BlockchainProcessor, blockchain: Blockchain, private val generator: Generator) : StreamResponseGrpcApiHandler<Empty, BrsApi.MiningInfo> {

    /**
     * Listener should close connection if it receives null.
     */
    private val listeners = HashSet<(BrsApi.MiningInfo?) -> Unit>()
    private val currentMiningInfo = AtomicReference<BrsApi.MiningInfo>()

    init {
        blockchainProcessor.addListener({ block: Block -> this.onBlock(block) }, BlockchainProcessor.Event.BLOCK_PUSHED)
        onBlock(blockchain.lastBlock)
    }

    private fun onBlock(block: Block) {
        synchronized(currentMiningInfo) {
            val nextGenSig = generator.calculateGenerationSignature(block.generationSignature, block.getGeneratorId())
            val miningInfo = currentMiningInfo.get()
            if (miningInfo == null || !Arrays.equals(miningInfo.generationSignature.toByteArray(), nextGenSig) || miningInfo.height - 1 != block.height || miningInfo.baseTarget != block.baseTarget) {
                currentMiningInfo.set(BrsApi.MiningInfo.newBuilder()
                        .setGenerationSignature(ByteString.copyFrom(nextGenSig))
                        .setHeight(block.height + 1)
                        .setBaseTarget(block.baseTarget)
                        .build())
                notifyListeners(currentMiningInfo.get())
            }
        }
    }

    private fun notifyListeners(miningInfo: BrsApi.MiningInfo) {
        synchronized(listeners) {
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
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    override fun handleStreamRequest(input: Empty, responseObserver: StreamObserver<BrsApi.MiningInfo>) {
        responseObserver.onNext(currentMiningInfo.get())
        addListener { miningInfo ->
            if (miningInfo == null) {
                responseObserver.onCompleted()
            } else {
                responseObserver.onNext(miningInfo)
            }
        }
    }
}
