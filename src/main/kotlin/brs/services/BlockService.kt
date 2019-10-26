package brs.services

import brs.entity.Block

interface BlockService {
    /**
     * TODO
     */
    @Throws(BlockchainProcessorService.BlockNotAcceptedException::class, InterruptedException::class)
    fun preVerify(block: Block)

    /**
     * TODO
     */
    @Throws(BlockchainProcessorService.BlockNotAcceptedException::class, InterruptedException::class)
    fun preVerify(block: Block, scoopData: ByteArray?)

    /**
     * TODO
     */
    fun getBlockReward(block: Block): Long

    /**
     * TODO
     */
    fun calculateBaseTarget(block: Block, previousBlock: Block)

    /**
     * TODO
     */
    fun setPrevious(block: Block, previousBlock: Block?)

    /**
     * TODO
     */
    fun verifyGenerationSignature(block: Block): Boolean

    /**
     * TODO
     */
    fun verifyBlockSignature(block: Block): Boolean

    /**
     * TODO
     */
    fun apply(block: Block)

    /**
     * TODO
     */
    fun getScoopNum(block: Block): Int
}
