package brs.services

import brs.entity.Block

interface OclPocService {
    val maxItems: Long
    fun validatePoC(blocks: Collection<Block>, pocVersion: Int, blockService: BlockService)
    fun destroy()

    class OCLCheckerException : RuntimeException {
        internal constructor(message: String) : super(message)

        internal constructor(message: String, cause: Throwable) : super(message, cause)
    }

    class PreValidateFailException internal constructor(message: String, cause: Throwable, @field:Transient val block: Block) : RuntimeException(message, cause)
}