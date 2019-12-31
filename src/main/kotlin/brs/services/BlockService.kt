package brs.services

import brs.entity.Block
import java.math.BigInteger

interface BlockService {
    /**
     * Checks whether a block is verified, and verifies it if it is not,
     * before removing it from the download cache unverified list.
     *
     * Pre-verification verifies the following:
     * - Calculates the nonce used for the PoC proof and verifies the PoC proof
     * - Verifies block's generation signature
     * - Verifies fee slot order (if enabled at block's height)
     * - Verifies block hash
     * - For each transaction, runs pre-validation ([TransactionService.preValidate()])
     * - For each transaction, verifies signature
     *
     * @param scoopData Pre-calculated scoop data for this block to use to verify the block's PoC proof. Will be calculated if null
     * @param warnIfNotVerified Emit a logger debug warning if the block was not pre-verified yet
     */
    fun preVerify(block: Block, scoopData: ByteArray? = null, warnIfNotVerified: Boolean = false)

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
     * Can be and is done during pre-verification.
     */
    fun verifyGenerationSignature(block: Block, pocTime: BigInteger): Boolean

    /**
     * TODO
     * Cannot be done during pre-verification as it requires
     * the information to determine the signer (reward
     * recipient assignments etc) and the signer's public key
     * to be available in the chain.
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
