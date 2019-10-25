package brs.services

import brs.entity.Block
import brs.util.BurstException
import brs.peer.Peer
import brs.entity.Transaction
import brs.util.Observable
import brs.util.json.toJsonString
import com.google.gson.JsonObject

interface BlockchainProcessorService : Observable<Block, BlockchainProcessorService.Event> {

    val oclVerify: Boolean

    val lastBlockchainFeeder: Peer?

    val lastBlockchainFeederHeight: Int?

    val minRollbackHeight: Int

    enum class Event {
        BLOCK_PUSHED,
        BLOCK_POPPED,
        BLOCK_GENERATED, BLOCK_SCANNED,
        RESCAN_BEGIN,
        BEFORE_BLOCK_ACCEPT,
        BEFORE_BLOCK_APPLY,
        AFTER_BLOCK_APPLY
    }

    fun processPeerBlock(request: JsonObject, peer: Peer)

    fun fullReset()

    fun generateBlock(secretPhrase: String, publicKey: ByteArray, nonce: Long?)

    fun popOffTo(height: Int): List<Block>

    open class BlockNotAcceptedException internal constructor(message: String) : BurstException(message)

    class TransactionNotAcceptedException(message: String, val transaction: Transaction) : BlockNotAcceptedException(message + " transaction: " + transaction.toJsonObject().toJsonString())

    class BlockOutOfOrderException(message: String) : BlockNotAcceptedException(message)

}
