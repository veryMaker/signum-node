package brs.services

import brs.Block
import brs.BlockchainProcessor
import brs.BlockchainProcessor.BlockNotAcceptedException
import brs.BlockchainProcessor.BlockOutOfOrderException

interface BlockService {
    @Throws(BlockchainProcessor.BlockNotAcceptedException::class, InterruptedException::class)
    fun preVerify(block: Block)

    @Throws(BlockchainProcessor.BlockNotAcceptedException::class, InterruptedException::class)
    fun preVerify(block: Block, scoopData: ByteArray?)

    fun getBlockReward(block: Block): Long

    fun calculateBaseTarget(block: Block, previousBlock: Block)

    fun setPrevious(block: Block, previousBlock: Block?)

    fun verifyGenerationSignature(block: Block): Boolean

    fun verifyBlockSignature(block: Block): Boolean

    fun apply(block: Block)

    fun getScoopNum(block: Block): Int
}
