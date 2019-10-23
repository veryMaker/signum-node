package brs.services

import brs.Block
import brs.BlockchainProcessor

interface BlockService {
    @Throws(BlockchainProcessor.BlockNotAcceptedException::class, InterruptedException::class)
    suspend fun preVerify(block: Block)

    @Throws(BlockchainProcessor.BlockNotAcceptedException::class, InterruptedException::class)
    suspend fun preVerify(block: Block, scoopData: ByteArray?)

    fun getBlockReward(block: Block): Long

    suspend fun calculateBaseTarget(block: Block, previousBlock: Block)

    suspend fun setPrevious(block: Block, previousBlock: Block?)

    suspend fun verifyGenerationSignature(block: Block): Boolean

    suspend fun verifyBlockSignature(block: Block): Boolean

    suspend fun apply(block: Block)

    fun getScoopNum(block: Block): Int
}
