package brs.services

import brs.entity.Block

interface OclPocService {
    /**
     * TODO
     */
    val maxItems: Long

    /**
     * Validates the PoC Proofs for `blocks` and pre-verifies the blocks.
     */
    fun validateAndPreVerify(blocks: Collection<Block>, pocVersion: Int)

    /**
     * TODO
     */
    fun destroy()

    class OCLCheckerException : Exception {
        internal constructor(message: String) : super(message)

        internal constructor(message: String, cause: Throwable) : super(message, cause)
    }

    class PreValidateFailException internal constructor(
        message: String,
        cause: Throwable, @field:Transient val block: Block
    ) : Exception(message, cause)
}