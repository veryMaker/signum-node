package brs

import brs.crypto.Crypto
import brs.crypto.signUsing
import brs.db.TransactionDb
import brs.fluxcapacitor.FluxValues
import brs.peer.Peer
import brs.util.JSON
import brs.util.convert.*
import brs.util.delegates.Atomic
import brs.util.delegates.AtomicLazy
import brs.util.logging.safeDebug
import brs.util.logging.safeError
import brs.util.toJsonString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class Block internal constructor(private val dp: DependencyProvider, val version: Int, val timestamp: Int, val previousBlockId: Long, val totalAmountNQT: Long, val totalFeeNQT: Long,
                     val payloadLength: Int, val payloadHash: ByteArray, val generatorPublicKey: ByteArray, val generationSignature: ByteArray,
                     blockSignature: ByteArray?, val previousBlockHash: ByteArray?, transactions: Collection<Transaction>?,
                     val nonce: Long, val blockATs: ByteArray?, height: Int) {
    var transactions by AtomicLazy<Collection<Transaction>> {
        val txs = transactionDb().findBlockTransactions(id)
        txs.forEach { transaction -> transaction.setBlock(this) }
        txs
    }

    var blockSignature: ByteArray?

    var cumulativeDifficulty: BigInteger = BigInteger.ZERO

    var baseTarget = Constants.INITIAL_BASE_TARGET
    var nextBlockId by Atomic<Long>()
    var height = -1
    var id by AtomicLazy {
        hash.fullHashToId()
    }
    var stringId by AtomicLazy {
        id.toUnsignedString()
    }
    var generatorId by AtomicLazy {
        Account.getId(generatorPublicKey)
    }
    var hash by AtomicLazy<ByteArray> {
        checkNotNull(blockSignature) { "Block is not signed yet" }
        Crypto.sha256().digest(bytes)
    }

    var pocTime: BigInteger? = null

    var peer: Peer? = null
    var byteLength = 0

    val isVerified: Boolean
        get() = pocTime != null

    val blockHash: ByteArray
        get() = Crypto.sha256().digest(bytes)

    val jsonObject: JsonObject
        get() {
            val json = JsonObject()
            json.addProperty("version", version)
            json.addProperty("timestamp", timestamp)
            json.addProperty("previousBlock", previousBlockId.toUnsignedString())
            json.addProperty("totalAmountNQT", totalAmountNQT)
            json.addProperty("totalFeeNQT", totalFeeNQT)
            json.addProperty("payloadLength", payloadLength)
            json.addProperty("payloadHash", payloadHash.toHexString())
            json.addProperty("generatorPublicKey", generatorPublicKey.toHexString())
            json.addProperty("generationSignature", generationSignature.toHexString())
            if (version > 1) {
                json.addProperty("previousBlockHash", previousBlockHash!!.toHexString())
            }
            json.addProperty("blockSignature", blockSignature?.toHexString())
            val transactionsData = JsonArray()
            transactions.forEach { transaction -> transactionsData.add(transaction.jsonObject) }
            json.add("transactions", transactionsData)
            json.addProperty("nonce", nonce.toUnsignedString())
            json.addProperty("blockATs", blockATs?.toHexString() ?: "")
            return json
        }

    val bytes: ByteArray
        get() {
            val buffer = ByteBuffer.allocate(4 + 4 + 8 + 4 + (if (version < 3) 4 + 4 else 8 + 8) + 4
                    + 32 + 32 + (32 + 32) + 8 + (blockATs?.size ?: 0) + 64)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.putInt(version)
            buffer.putInt(timestamp)
            buffer.putLong(previousBlockId)
            buffer.putInt(transactions.size)
            if (version < 3) {
                buffer.putInt((totalAmountNQT / Constants.ONE_BURST).toInt())
                buffer.putInt((totalFeeNQT / Constants.ONE_BURST).toInt())
            } else {
                buffer.putLong(totalAmountNQT)
                buffer.putLong(totalFeeNQT)
            }
            buffer.putInt(payloadLength)
            buffer.put(payloadHash)
            buffer.put(generatorPublicKey)
            buffer.put(generationSignature)
            if (version > 1) {
                buffer.put(previousBlockHash)
            }
            buffer.putLong(nonce)
            if (blockATs != null)
                buffer.put(blockATs)
            if (buffer.limit() - buffer.position() < blockSignature!!.size)
                logger.safeError { "Something is too large here - buffer should have ${blockSignature!!.size} bytes left but only has ${buffer.limit() - buffer.position()}" }
            buffer.put(blockSignature!!)
            return buffer.array()
        }

    init {
        if (payloadLength > dp.fluxCapacitor.getValue(FluxValues.MAX_PAYLOAD_LENGTH, height) || payloadLength < 0) {
            throw BurstException.NotValidException(
                    "attempted to create a block with payloadLength " + payloadLength + " height " + height + "previd " + previousBlockId)
        }
        this.blockSignature = blockSignature
        if (transactions != null) {
            if (transactions.size > dp.fluxCapacitor.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS, height)) {
                throw BurstException.NotValidException(
                        "attempted to create a block with " + transactions.size + " transactions")
            }
            var previousId: Long = 0
            transactions.forEach { transaction ->
                if (transaction.id <= previousId && previousId != 0L) {
                    throw BurstException.NotValidException("Block transactions are not sorted!")
                }
                previousId = transaction.id
            }
            this.transactions = transactions
        }
    }

    constructor(dp: DependencyProvider, version: Int, timestamp: Int, previousBlockId: Long, totalAmountNQT: Long, totalFeeNQT: Long, payloadLength: Int, payloadHash: ByteArray, generatorPublicKey: ByteArray, generationSignature: ByteArray, blockSignature: ByteArray, previousBlockHash: ByteArray?, cumulativeDifficulty: BigInteger?, baseTarget: Long,
                nextBlockId: Long, height: Int, id: Long, nonce: Long, blockATs: ByteArray) : this(dp, version, timestamp, previousBlockId, totalAmountNQT, totalFeeNQT, payloadLength, payloadHash, generatorPublicKey, generationSignature, blockSignature, previousBlockHash, null, nonce, blockATs, height) {

        this.cumulativeDifficulty = cumulativeDifficulty ?: BigInteger.ZERO
        this.baseTarget = baseTarget
        this.nextBlockId = nextBlockId
        this.height = height
        this.id = id
    }

    private fun transactionDb(): TransactionDb {
        return dp.transactionDb
    }

    fun getNonce(): Long? {
        return nonce
    }

    override fun equals(other: Any?): Boolean {
        return other is Block && this.id == other.id
    }

    override fun hashCode(): Int {
        return (id xor id.ushr(32)).toInt()
    }

    internal fun sign(secretPhrase: String) {
        check(blockSignature == null) { "Block already signed" }
        blockSignature = ByteArray(64)
        val data = bytes
        val data2 = ByteArray(data.size - 64)
        System.arraycopy(data, 0, data2, 0, data2.size)
        blockSignature = data2.signUsing(secretPhrase)
    }

    companion object {

        private val logger = LoggerFactory.getLogger(Block::class.java)

        internal fun parseBlock(dp: DependencyProvider, blockData: JsonObject, height: Int): Block {
            try {
                val version = JSON.getAsInt(blockData.get("version"))
                val timestamp = JSON.getAsInt(blockData.get("timestamp"))
                val previousBlock = JSON.getAsString(blockData.get("previousBlock")).parseUnsignedLong()
                val totalAmountNQT = JSON.getAsLong(blockData.get("totalAmountNQT"))
                val totalFeeNQT = JSON.getAsLong(blockData.get("totalFeeNQT"))
                val payloadLength = JSON.getAsInt(blockData.get("payloadLength"))
                val payloadHash = JSON.getAsString(blockData.get("payloadHash")).parseHexString()
                val generatorPublicKey = JSON.getAsString(blockData.get("generatorPublicKey")).parseHexString()
                val generationSignature = JSON.getAsString(blockData.get("generationSignature")).parseHexString()
                val blockSignature = JSON.getAsString(blockData.get("blockSignature")).parseHexString()
                val previousBlockHash = if (version == 1) null else JSON.getAsString(blockData.get("previousBlockHash")).parseHexString()
                val nonce = JSON.getAsString(blockData.get("nonce")).parseUnsignedLong()

                val blockTransactions = TreeMap<Long, Transaction>()
                val transactionsData = JSON.getAsJsonArray(blockData.get("transactions"))

                for (transactionData in transactionsData) {
                    val transaction = Transaction.parseTransaction(dp, JSON.getAsJsonObject(transactionData), height)
                    if (transaction.signature != null && blockTransactions.put(transaction.id, transaction) != null) {
                        throw BurstException.NotValidException("Block contains duplicate transactions: " + transaction.stringId)
                    }
                }

                val blockATs = JSON.getAsString(blockData.get("blockATs")).parseHexString()
                return Block(dp, version, timestamp, previousBlock, totalAmountNQT, totalFeeNQT,
                        payloadLength, payloadHash, generatorPublicKey, generationSignature, blockSignature,
                        previousBlockHash, blockTransactions.values, nonce, blockATs, height)
            } catch (e: BurstException.ValidationException) {
                if (true) {
                    logger.safeDebug { "Failed to parse block: ${blockData.toJsonString()}" }
                }
                throw e
            } catch (e: RuntimeException) {
                if (true) {
                    logger.safeDebug { "Failed to parse block: ${blockData.toJsonString()}" }
                }
                throw e
            }

        }
    }
}
