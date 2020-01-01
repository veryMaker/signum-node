package brs.services

import brs.entity.Block
import brs.entity.Transaction
import brs.peer.Peer
import brs.util.BurstException
import brs.util.Observable
import brs.util.json.toJsonString
import com.google.gson.JsonObject

interface BlockchainProcessorService : Observable<Block, BlockchainProcessorService.Event> {
    /**
     * TODO
     */
    val lastBlockchainFeeder: Peer?

    /**
     * TODO
     */
    val lastBlockchainFeederHeight: Int?

    /**
     * TODO
     */
    val minRollbackHeight: Int

    /**
     * TODO
     */
    enum class Event {
        BLOCK_PUSHED,
        BLOCK_POPPED,
        BLOCK_GENERATED, BLOCK_SCANNED,
        BEFORE_BLOCK_ACCEPT,
        BEFORE_BLOCK_APPLY,
        AFTER_BLOCK_APPLY
    }

    /**
     * TODO
     */
    fun processPeerBlock(request: JsonObject, peer: Peer)

    /**
     * TODO
     */
    fun fullReset()

    /**
     * TODO
     */
    fun generateBlock(secretPhrase: String, publicKey: ByteArray, nonce: Long?)

    /**
     * TODO
     */
    fun popOffTo(height: Int): List<Block>

    /**
     * TODO
     */
    open class BlockNotAcceptedException : BurstException {
        internal constructor(message: String) : super(message)
        internal constructor(message: String, cause: Throwable) : super(message, cause)
    }

    /**
     * TODO
     */
    class TransactionNotAcceptedException : BlockNotAcceptedException {
        val transaction: Transaction

        constructor(message: String, transaction: Transaction)
                : super(message + " transaction: " + transaction.toJsonObject().toJsonString()) {
            this.transaction = transaction
        }

        constructor(message: String, transaction: Transaction, cause: Throwable) : super(message, cause) {
            this.transaction = transaction
        }
    }

    /**
     * TODO
     */
    class BlockOutOfOrderException(message: String) : BlockNotAcceptedException(message)
}
