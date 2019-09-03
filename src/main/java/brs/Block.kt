package brs

import brs.crypto.Crypto
import brs.db.TransactionDb
import brs.fluxcapacitor.FluxValues
import brs.peer.Peer
import brs.util.atomic.AtomicLazy
import brs.util.Convert
import brs.util.JSON
import brs.util.atomic.Atomic
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory

import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class Block @Throws(BurstException.ValidationException::class)
internal constructor(private val dp: DependencyProvider, val version: Int, val timestamp: Int, val previousBlockId: Long, val totalAmountNQT: Long, val totalFeeNQT: Long,
                     val payloadLength: Int, val payloadHash: ByteArray, val generatorPublicKey: ByteArray, val generationSignature: ByteArray,
                     blockSignature: ByteArray?, val previousBlockHash: ByteArray?, transactions: Collection<Transaction>?,
                     val nonce: Long, val blockATs: ByteArray?, height: Int) {
    private var blockTransactions by Atomic<Collection<Transaction>?>()

    var blockSignature: ByteArray

    var cumulativeDifficulty: BigInteger = BigInteger.ZERO

    var baseTarget = Constants.INITIAL_BASE_TARGET
    var nextBlockId by Atomic<Long>()
    var height = -1
    var id by AtomicLazy {
        Convert.fullHashToId(hash)
    }
    var stringId by AtomicLazy {
        Convert.toUnsignedLong(id)
    }
    var generatorId by Atomic<Long>()
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

    val transactions: List<Transaction>
        get() {
            if (blockTransactions == null) {
                this.blockTransactions = Collections.unmodifiableList(transactionDb().findBlockTransactions(id))
                this.blockTransactions!!.forEach { transaction -> transaction.setBlock(this) }
            }
            return blockTransactions!!
        }

    val jsonObject: JsonObject
        get() {
            val json = JsonObject()
            json.addProperty("version", version)
            json.addProperty("timestamp", timestamp)
            json.addProperty("previousBlock", Convert.toUnsignedLong(previousBlockId))
            json.addProperty("totalAmountNQT", totalAmountNQT)
            json.addProperty("totalFeeNQT", totalFeeNQT)
            json.addProperty("payloadLength", payloadLength)
            json.addProperty("payloadHash", Convert.toHexString(payloadHash))
            json.addProperty("generatorPublicKey", Convert.toHexString(generatorPublicKey))
            json.addProperty("generationSignature", Convert.toHexString(generationSignature))
            if (version > 1) {
                json.addProperty("previousBlockHash", Convert.toHexString(previousBlockHash!!))
            }
            json.addProperty("blockSignature", Convert.toHexString(blockSignature))
            val transactionsData = JsonArray()
            transactions.forEach { transaction -> transactionsData.add(transaction.jsonObject) }
            json.add("transactions", transactionsData)
            json.addProperty("nonce", Convert.toUnsignedLong(nonce))
            json.addProperty("blockATs", Convert.toHexString(blockATs))
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
                logger.error("Something is too large here - buffer should have {} bytes left but only has {}",
                        blockSignature!!.size,
                        buffer.limit() - buffer.position())
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
            this.blockTransactions = transactions
        }
    }

    @Throws(BurstException.ValidationException::class)
    constructor(dp: DependencyProvider, version: Int, timestamp: Int, previousBlockId: Long, totalAmountNQT: Long, totalFeeNQT: Long, payloadLength: Int, payloadHash: ByteArray, generatorPublicKey: ByteArray, generationSignature: ByteArray, blockSignature: ByteArray, previousBlockHash: ByteArray, cumulativeDifficulty: BigInteger?, baseTarget: Long,
                nextBlockId: Long, height: Int, id: Long, nonce: Long, blockATs: ByteArray) : this(dp, version, timestamp, previousBlockId, totalAmountNQT, totalFeeNQT, payloadLength, payloadHash, generatorPublicKey, generationSignature, blockSignature, previousBlockHash, null, nonce, blockATs, height) {

        this.cumulativeDifficulty = cumulativeDifficulty ?: BigInteger.ZERO
        this.baseTarget = baseTarget
        this.nextBlockId = nextBlockId
        this.height = height
        this.id = id
    }

    private fun transactionDb(): TransactionDb {
        return dp.dbs.transactionDb
    }

    fun getStringId(): String {
        if (stringId == null) {
            id
            if (stringId == null) {
                stringId = Convert.toUnsignedLong(id)
            }
        }
        return stringId
    }

    fun getGeneratorId(): Long {
        if (generatorId == 0L) {
            generatorId = Account.getId(generatorPublicKey)
        }
        return generatorId
    }

    fun getNonce(): Long? {
        return nonce
    }

    override fun equals(o: Any?): Boolean {
        return o is Block && this.id == o.id
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
        blockSignature = Crypto.sign(data2, secretPhrase)
    }

    companion object {

        private val logger = LoggerFactory.getLogger(Block::class.java)

        @Throws(BurstException.ValidationException::class)
        internal fun parseBlock(dp: DependencyProvider, blockData: JsonObject, height: Int): Block {
            try {
                val version = JSON.getAsInt(blockData.get("version"))
                val timestamp = JSON.getAsInt(blockData.get("timestamp"))
                val previousBlock = Convert.parseUnsignedLong(JSON.getAsString(blockData.get("previousBlock")))
                val totalAmountNQT = JSON.getAsLong(blockData.get("totalAmountNQT"))
                val totalFeeNQT = JSON.getAsLong(blockData.get("totalFeeNQT"))
                val payloadLength = JSON.getAsInt(blockData.get("payloadLength"))
                val payloadHash = Convert.parseHexString(JSON.getAsString(blockData.get("payloadHash")))
                val generatorPublicKey = Convert.parseHexString(JSON.getAsString(blockData.get("generatorPublicKey")))
                val generationSignature = Convert.parseHexString(JSON.getAsString(blockData.get("generationSignature")))
                val blockSignature = Convert.parseHexString(JSON.getAsString(blockData.get("blockSignature")))
                val previousBlockHash = if (version == 1) null else Convert.parseHexString(JSON.getAsString(blockData.get("previousBlockHash")))
                val nonce = Convert.parseUnsignedLong(JSON.getAsString(blockData.get("nonce")))

                val blockTransactions = TreeMap<Long, Transaction>()
                val transactionsData = JSON.getAsJsonArray(blockData.get("transactions"))

                for (transactionData in transactionsData) {
                    val transaction = Transaction.parseTransaction(dp, JSON.getAsJsonObject(transactionData), height)
                    if (transaction.signature != null && blockTransactions.put(transaction.id, transaction) != null) {
                        throw BurstException.NotValidException("Block contains duplicate transactions: " + transaction.stringId)
                    }
                }

                val blockATs = Convert.parseHexString(JSON.getAsString(blockData.get("blockATs")))
                return Block(dp, version, timestamp, previousBlock, totalAmountNQT, totalFeeNQT,
                        payloadLength, payloadHash, generatorPublicKey, generationSignature, blockSignature,
                        previousBlockHash, mutableListOf(blockTransactions.values), nonce, blockATs, height)
            } catch (e: BurstException.ValidationException) {
                if (logger.isDebugEnabled) {
                    logger.debug("Failed to parse block: {}", JSON.toJsonString(blockData))
                }
                throw e
            } catch (e: RuntimeException) {
                if (logger.isDebugEnabled) {
                    logger.debug("Failed to parse block: {}", JSON.toJsonString(blockData))
                }
                throw e
            }

        }
    }
}
