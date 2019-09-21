package brs

import brs.peer.Peer
import brs.util.Observable
import brs.util.toJsonString
import com.google.gson.JsonObject

interface BlockchainProcessor : Observable<Block, BlockchainProcessor.Event> {

    val oclVerify: Boolean

    val lastBlockchainFeeder: Peer?

    val lastBlockchainFeederHeight: Int?

    val isScanning: Boolean

    val minRollbackHeight: Int

    enum class Event {
        BLOCK_PUSHED,
        BLOCK_POPPED,
        BLOCK_GENERATED, BLOCK_SCANNED,
        RESCAN_BEGIN,
        RESCAN_END,
        BEFORE_BLOCK_ACCEPT,
        BEFORE_BLOCK_APPLY,
        AFTER_BLOCK_APPLY
    }

    @Throws(BurstException::class)
    fun processPeerBlock(request: JsonObject, peer: Peer)

    fun fullReset()

    @Throws(BlockNotAcceptedException::class)
    fun generateBlock(secretPhrase: String, publicKey: ByteArray, nonce: Long?)

    fun popOffTo(height: Int): List<Block>

    open class BlockNotAcceptedException internal constructor(message: String) : BurstException(message)

    class TransactionNotAcceptedException(message: String, val transaction: Transaction) : BlockNotAcceptedException(message + " transaction: " + transaction.jsonObject.toJsonString())

    class BlockOutOfOrderException(message: String) : BlockNotAcceptedException(message)

}
