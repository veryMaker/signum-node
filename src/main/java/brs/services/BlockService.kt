package brs.services

import brs.Block
import brs.BlockchainProcessor
import brs.BlockchainProcessor.BlockNotAcceptedException
import brs.BlockchainProcessor.BlockOutOfOrderException

interface BlockService {

    @Throws(BlockchainProcessor.BlockNotAcceptedException::class, InterruptedException::class)
    fun preVerify(block: Block)

    @Throws(BlockchainProcessor.BlockNotAcceptedException::class, InterruptedException::class)
    fun preVerify(block: Block, scoopData: ByteArray)

    fun getBlockReward(block: Block): Long

    @Throws(BlockOutOfOrderException::class)
    fun calculateBaseTarget(block: Block, lastBlock: Block)

    fun setPrevious(block: Block, previousBlock: Block?)

    @Throws(BlockNotAcceptedException::class)
    fun verifyGenerationSignature(block: Block): Boolean

    @Throws(BlockOutOfOrderException::class)
    fun verifyBlockSignature(block: Block): Boolean

    fun apply(block: Block)

    fun getScoopNum(block: Block): Int
}
