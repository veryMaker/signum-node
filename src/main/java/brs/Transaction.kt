package brs

import brs.Appendix.AbstractAppendix
import brs.TransactionType.Payment
import brs.crypto.Crypto
import brs.fluxcapacitor.FluxValues
import brs.transactionduplicates.TransactionDuplicationKey
import brs.util.Atomic
import brs.util.AtomicLazy
import brs.util.Convert
import brs.util.JSON
import com.google.gson.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.ArrayList
import java.util.Collections
import java.util.Optional
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.experimental.and
import kotlin.experimental.or

class Transaction @Throws(BurstException.NotValidException::class)
private constructor(private val dp: DependencyProvider, builder: Builder) : Comparable<Transaction> {

    val deadline: Short
    lateinit var senderPublicKey: ByteArray
    val recipientId: Long
    val amountNQT: Long
    val feeNQT: Long
    val referencedTransactionFullHash: String?
    lateinit var type: TransactionType
    val ecBlockHeight: Int
    val ecBlockId: Long
    val version: Byte
    val timestamp: Int
    val attachment: Attachment.AbstractAttachment?
    val message: Appendix.Message?
    val encryptedMessage: Appendix.EncryptedMessage?
    val encryptToSelfMessage: Appendix.EncryptToSelfMessage?
    val publicKeyAnnouncement: Appendix.PublicKeyAnnouncement?

    val appendages: List<AbstractAppendix>
    val appendagesSize: Int

    val height = AtomicInteger()
    val blockId = AtomicLong()
    val block = AtomicReference<Block>()
    val signature = AtomicReference<ByteArray>()
    val blockTimestamp = AtomicInteger()
    var id by AtomicLazy {
        check(!(signature.get() == null && type.isSigned)) { "Transaction is not signed yet" }
        val hash = if (useNQT()) {
            val data = zeroSignature(bytes)
            val signatureHash = Crypto.sha256().digest(if (signature.get() != null) signature.get() else ByteArray(64))
            val digest = Crypto.sha256()
            digest.update(data)
            digest.digest(signatureHash)
        } else {
            Crypto.sha256().digest(bytes)
        }
        val longId = Convert.fullHashToId(hash)
        stringId.set(Convert.toUnsignedLong(longId))
        fullHash.set(Convert.toHexString(hash))
        longId
    }
    val stringId = AtomicReference<String>()
    var senderId by AtomicLazy {
        if (!this::type.isInitialized || type.isSigned) {
            Account.getId(senderPublicKey)
        } else 0
    }
    
    val fullHash = AtomicReference<String>()

    val expiration: Int
        get() = timestamp + deadline * 60

    val bytes: ByteArray
        get() {
            try {
                val buffer = ByteBuffer.allocate(size)
                buffer.order(ByteOrder.LITTLE_ENDIAN)
                buffer.put(type!!.type)
                buffer.put((version.toInt() shl 4).toByte() or (type.subtype and 0xff.toByte()))
                buffer.putInt(timestamp)
                buffer.putShort(deadline)
                if (type.isSigned || !dp.fluxCapacitor.getValue(FluxValues.AT_FIX_BLOCK_4)) {
                    buffer.put(senderPublicKey)
                } else {
                    buffer.putLong(senderId)
                    buffer.put(ByteArray(24))
                }
                buffer.putLong(if (type.hasRecipient()) recipientId else Genesis.CREATOR_ID)
                if (useNQT()) {
                    buffer.putLong(amountNQT)
                    buffer.putLong(feeNQT)
                    if (referencedTransactionFullHash != null) {
                        buffer.put(Convert.parseHexString(referencedTransactionFullHash)!!)
                    } else {
                        buffer.put(ByteArray(32))
                    }
                } else {
                    buffer.putInt((amountNQT / Constants.ONE_BURST).toInt())
                    buffer.putInt((feeNQT / Constants.ONE_BURST).toInt())
                    if (referencedTransactionFullHash != null) {
                        buffer.putLong(Convert.fullHashToId(Convert.parseHexString(referencedTransactionFullHash)))
                    } else {
                        buffer.putLong(0L)
                    }
                }
                buffer.put(if (signature.get() != null) signature.get() else ByteArray(64))
                if (version > 0) {
                    buffer.putInt(flags)
                    buffer.putInt(ecBlockHeight)
                    buffer.putLong(ecBlockId)
                }
                appendages.forEach { appendage -> appendage.putBytes(buffer) }
                return buffer.array()
            } catch (e: RuntimeException) {
                if (logger.isDebugEnabled) {
                    logger.debug("Failed to get transaction bytes for transaction: {}", JSON.toJsonString(jsonObject))
                }
                throw e
            }

        }

    val unsignedBytes: ByteArray
        get() = zeroSignature(bytes)

    val jsonObject: JsonObject
        get() {
            val json = JsonObject()
            json.addProperty("type", type!!.type)
            json.addProperty("subtype", type.subtype)
            json.addProperty("timestamp", timestamp)
            json.addProperty("deadline", deadline)
            json.addProperty("senderPublicKey", Convert.toHexString(senderPublicKey))
            if (type.hasRecipient()) {
                json.addProperty("recipient", Convert.toUnsignedLong(recipientId))
            }
            json.addProperty("amountNQT", amountNQT)
            json.addProperty("feeNQT", feeNQT)
            if (referencedTransactionFullHash != null) {
                json.addProperty("referencedTransactionFullHash", referencedTransactionFullHash)
            }
            json.addProperty("ecBlockHeight", ecBlockHeight)
            json.addProperty("ecBlockId", Convert.toUnsignedLong(ecBlockId))
            json.addProperty("signature", Convert.toHexString(signature.get()))
            val attachmentJSON = JsonObject()
            appendages.forEach { appendage -> JSON.addAll(attachmentJSON, appendage.jsonObject) }
            json.add("attachment", attachmentJSON)
            json.addProperty("version", version)
            return json
        }

    val size: Int
        get() = signatureOffset() + 64 + (if (version > 0) 4 + 4 + 8 else 0) + appendagesSize

    private val flags: Int
        get() {
            var flags = 0
            var position = 1
            if (message != null) {
                flags = flags or position
            }
            position = position shl 1
            if (encryptedMessage != null) {
                flags = flags or position
            }
            position = position shl 1
            if (publicKeyAnnouncement != null) {
                flags = flags or position
            }
            position = position shl 1
            if (encryptToSelfMessage != null) {
                flags = flags or position
            }
            return flags
        }

    val duplicationKey: TransactionDuplicationKey
        get() = type!!.getDuplicationKey(this)

    class Builder(internal val dp: DependencyProvider, internal val version: Byte, internal val senderPublicKey: ByteArray, internal val amountNQT: Long, internal val feeNQT: Long, internal val timestamp: Int, internal val deadline: Short,
                  internal val attachment: Attachment.AbstractAttachment) {
        internal val type: TransactionType

        internal var recipientId: Long = 0
        internal var referencedTransactionFullHash: String? = null
        internal var signature: ByteArray? = null
        internal var message: Appendix.Message? = null
        internal var encryptedMessage: Appendix.EncryptedMessage? = null
        internal var encryptToSelfMessage: Appendix.EncryptToSelfMessage? = null
        internal var publicKeyAnnouncement: Appendix.PublicKeyAnnouncement? = null
        internal var blockId: Long = 0
        internal var height = Integer.MAX_VALUE
        internal var id: Long = 0
        internal var senderId: Long = 0
        internal var blockTimestamp = -1
        internal var fullHash: String? = null
        internal var ecBlockHeight: Int = 0
        internal var ecBlockId: Long = 0

        init {
            this.type = attachment.transactionType
        }

        @Throws(BurstException.NotValidException::class)
        fun build(): Transaction {
            return Transaction(dp, this)
        }

        fun recipientId(recipientId: Long): Builder {
            this.recipientId = recipientId
            return this
        }

        fun referencedTransactionFullHash(referencedTransactionFullHash: String?): Builder {
            this.referencedTransactionFullHash = referencedTransactionFullHash
            return this
        }

        fun referencedTransactionFullHash(referencedTransactionFullHash: ByteArray?): Builder {
            if (referencedTransactionFullHash != null) {
                this.referencedTransactionFullHash = Convert.toHexString(referencedTransactionFullHash)
            }
            return this
        }

        fun message(message: Appendix.Message?): Builder {
            this.message = message
            return this
        }

        fun encryptedMessage(encryptedMessage: Appendix.EncryptedMessage?): Builder {
            this.encryptedMessage = encryptedMessage
            return this
        }

        fun encryptToSelfMessage(encryptToSelfMessage: Appendix.EncryptToSelfMessage?): Builder {
            this.encryptToSelfMessage = encryptToSelfMessage
            return this
        }

        fun publicKeyAnnouncement(publicKeyAnnouncement: Appendix.PublicKeyAnnouncement?): Builder {
            this.publicKeyAnnouncement = publicKeyAnnouncement
            return this
        }

        fun id(id: Long): Builder {
            this.id = id
            return this
        }

        fun signature(signature: ByteArray?): Builder {
            this.signature = signature
            return this
        }

        fun blockId(blockId: Long): Builder {
            this.blockId = blockId
            return this
        }

        fun height(height: Int): Builder {
            this.height = height
            return this
        }

        fun senderId(senderId: Long): Builder {
            this.senderId = senderId
            return this
        }

        internal fun fullHash(fullHash: String): Builder {
            this.fullHash = fullHash
            return this
        }

        fun fullHash(fullHash: ByteArray?): Builder {
            if (fullHash != null) {
                this.fullHash = Convert.toHexString(fullHash)
            }
            return this
        }

        fun blockTimestamp(blockTimestamp: Int): Builder {
            this.blockTimestamp = blockTimestamp
            return this
        }

        fun ecBlockHeight(height: Int): Builder {
            this.ecBlockHeight = height
            return this
        }

        fun ecBlockId(blockId: Long): Builder {
            this.ecBlockId = blockId
            return this
        }

    }

    init {
        this.timestamp = builder.timestamp
        this.deadline = builder.deadline
        this.senderPublicKey = builder.senderPublicKey
        this.recipientId = Optional.ofNullable(builder.recipientId).orElse(0L)
        this.amountNQT = builder.amountNQT
        this.referencedTransactionFullHash = builder.referencedTransactionFullHash
        this.signature.set(builder.signature)
        this.type = builder.type
        this.version = builder.version
        this.blockId.set(builder.blockId)
        this.height.set(builder.height)
        this.id = builder.id
        this.senderId = builder.senderId
        this.blockTimestamp.set(builder.blockTimestamp)
        this.fullHash.set(builder.fullHash)
        this.ecBlockHeight = builder.ecBlockHeight
        this.ecBlockId = builder.ecBlockId

        val list = ArrayList<Appendix.AbstractAppendix>()
        this.attachment = builder.attachment
        if (attachment != null) {
            list.add(this.attachment)
        }
        this.message = builder.message
        if (message != null) {
            list.add(this.message)
        }
        this.encryptedMessage = builder.encryptedMessage
        if (encryptedMessage != null) {
            list.add(this.encryptedMessage)
        }
        this.publicKeyAnnouncement = builder.publicKeyAnnouncement
        if (publicKeyAnnouncement != null) {
            list.add(this.publicKeyAnnouncement)
        }
        this.encryptToSelfMessage = builder.encryptToSelfMessage
        if (encryptToSelfMessage != null) {
            list.add(this.encryptToSelfMessage)
        }
        this.appendages = Collections.unmodifiableList(list)
        var countAppendeges = 0
        for (appendage in appendages) {
            countAppendeges += appendage.size
        }
        this.appendagesSize = countAppendeges
        val effectiveHeight = if (height.get() < Integer.MAX_VALUE) height.get() else dp.blockchain.height
        val minimumFeeNQT = type.minimumFeeNQT(effectiveHeight, countAppendeges)
        if (type == null || type.isSigned) {
            if (builder.feeNQT > 0 && builder.feeNQT < minimumFeeNQT) {
                throw BurstException.NotValidException(String.format("Requested fee %d less than the minimum fee %d",
                        builder.feeNQT, minimumFeeNQT))
            }
            if (builder.feeNQT <= 0) {
                feeNQT = minimumFeeNQT
            } else {
                feeNQT = builder.feeNQT
            }
        } else {
            feeNQT = builder.feeNQT
        }

        if ((type == null || type.isSigned) && (deadline < 1
                        || feeNQT > Constants.MAX_BALANCE_NQT
                        || amountNQT < 0
                        || amountNQT > Constants.MAX_BALANCE_NQT
                        || type == null)) {
            throw BurstException.NotValidException("Invalid transaction parameters:\n type: " + type + ", timestamp: " + timestamp
                    + ", deadline: " + deadline + ", fee: " + feeNQT + ", amount: " + amountNQT)
        }

        if (attachment == null || type !== attachment.transactionType) {
            throw BurstException.NotValidException("Invalid attachment $attachment for transaction of type $type")
        }

        if (!type.hasRecipient() && attachment.transactionType !== Payment.MULTI_OUT && attachment.transactionType !== Payment.MULTI_SAME_OUT && (recipientId != 0L || amountNQT != 0L)) {
            throw BurstException.NotValidException("Transactions of this type must have recipient == Genesis, amount == 0")
        }

        for (appendage in appendages) {
            if (!appendage.verifyVersion(this.version)) {
                throw BurstException.NotValidException("Invalid attachment version " + appendage.version
                        + " for transaction version " + this.version)
            }
        }

    }

    fun getHeight(): Int {
        return height.get()
    }

    fun setHeight(height: Int) {
        this.height.set(height)
    }

    fun getSignature(): ByteArray {
        return signature.get()
    }

    fun getBlockId(): Long {
        return blockId.get()
    }

    fun setBlock(block: Block) {
        this.block.set(block)
        this.blockId.set(block.id)
        this.height.set(block.height)
        this.blockTimestamp.set(block.timestamp)
    }

    internal fun unsetBlock() {
        this.block.set(null)
        this.blockId.set(0)
        this.blockTimestamp.set(-1)
        // must keep the height set, as transactions already having been included in a popped-off block before
        // get priority when sorted for inclusion in a new block
    }

    fun getBlockTimestamp(): Int {
        return blockTimestamp.get()
    }

    fun getAttachment(): Attachment? {
        return attachment
    }

    fun getStringId(): String {
        if (stringId.get() == null) {
            id
            if (stringId.get() == null) {
                stringId.set(Convert.toUnsignedLong(id))
            }
        }
        return stringId.get()
    }

    fun getFullHash(): String {
        if (fullHash.get() == null) {
            id
        }
        return fullHash.get()
    }

    fun sign(secretPhrase: String) {
        check(signature.get() == null) { "Transaction already signed" }
        signature.set(Crypto.sign(bytes, secretPhrase))
    }

    override fun equals(o: Any?): Boolean {
        return o is Transaction && this.id == o.id
    }

    override fun hashCode(): Int {
        return (id xor id.ushr(32)).toInt()
    }

    override fun compareTo(other: Transaction): Int {
        return java.lang.Long.compare(this.id, other.id)
    }

    fun verifySignature(): Boolean {
        val data = zeroSignature(bytes)
        return Crypto.verify(signature.get(), data, senderPublicKey, useNQT())
    }

    private fun signatureOffset(): Int {
        return 1 + 1 + 4 + 2 + 32 + 8 + if (useNQT()) 8 + 8 + 32 else 4 + 4 + 8
    }

    private fun useNQT(): Boolean {
        return this.height.get() > Constants.NQT_BLOCK && (this.height.get() < Integer.MAX_VALUE || dp.blockchain.height >= Constants.NQT_BLOCK)
    }

    private fun zeroSignature(data: ByteArray): ByteArray {
        val start = signatureOffset()
        for (i in start until start + 64) {
            data[i] = 0
        }
        return data
    }

    companion object {

        private val logger = LoggerFactory.getLogger(Transaction::class.java)

        @Throws(BurstException.ValidationException::class)
        fun parseTransaction(dp: DependencyProvider, bytes: ByteArray): Transaction {
            try {
                val buffer = ByteBuffer.wrap(bytes)
                buffer.order(ByteOrder.LITTLE_ENDIAN)
                val type = buffer.get()
                var subtype = buffer.get()
                val version = ((subtype.toInt() and 0xF0) shr 4).toByte()
                subtype = (subtype and 0x0F).toByte()
                val timestamp = buffer.get().toInt()
                val deadline = buffer.short
                val senderPublicKey = ByteArray(32)
                buffer.get(senderPublicKey)
                val recipientId = buffer.long
                val amountNQT = buffer.long
                val feeNQT = buffer.long
                var referencedTransactionFullHash: String? = null
                val referencedTransactionFullHashBytes = ByteArray(32)
                buffer.get(referencedTransactionFullHashBytes)
                if (Convert.emptyToNull(referencedTransactionFullHashBytes) != null) {
                    referencedTransactionFullHash = Convert.toHexString(referencedTransactionFullHashBytes)
                }
                var signature: ByteArray? = ByteArray(64)
                buffer.get(signature!!)
                signature = Convert.emptyToNull(signature)
                var flags = 0
                var ecBlockHeight = 0
                var ecBlockId: Long = 0
                if (version > 0) {
                    flags = buffer.get().toInt()
                    ecBlockHeight = buffer.get().toInt()
                    ecBlockId = buffer.long
                }
                val transactionType = TransactionType.findTransactionType(type, subtype)
                val builder = Transaction.Builder(dp, version, senderPublicKey, amountNQT, feeNQT, timestamp, deadline, transactionType!!.parseAttachment(buffer, version))
                        .referencedTransactionFullHash(referencedTransactionFullHash)
                        .signature(signature)
                        .ecBlockHeight(ecBlockHeight)
                        .ecBlockId(ecBlockId)
                if (transactionType.hasRecipient()) {
                    builder.recipientId(recipientId)
                }

                transactionType.parseAppendices(builder, flags, version, buffer)

                return builder.build()
            } catch (e: BurstException.NotValidException) {
                if (logger.isDebugEnabled) {
                    logger.debug("Failed to parse transaction bytes: {}", Convert.toHexString(bytes))
                }
                throw e
            } catch (e: RuntimeException) {
                if (logger.isDebugEnabled) {
                    logger.debug("Failed to parse transaction bytes: {}", Convert.toHexString(bytes))
                }
                throw e
            }

        }

        @Throws(BurstException.NotValidException::class)
        internal fun parseTransaction(dp: DependencyProvider, transactionData: JsonObject, height: Int): Transaction {
            try {
                val type = JSON.getAsByte(transactionData.get("type"))
                val subtype = JSON.getAsByte(transactionData.get("subtype"))
                val timestamp = JSON.getAsInt(transactionData.get("timestamp"))
                val deadline = JSON.getAsShort(transactionData.get("deadline"))
                val senderPublicKey = Convert.parseHexString(JSON.getAsString(transactionData.get("senderPublicKey")))
                val amountNQT = JSON.getAsLong(transactionData.get("amountNQT"))
                val feeNQT = JSON.getAsLong(transactionData.get("feeNQT"))
                val referencedTransactionFullHash = JSON.getAsString(transactionData.get("referencedTransactionFullHash"))
                val signature = Convert.parseHexString(JSON.getAsString(transactionData.get("signature")))
                val version = JSON.getAsByte(transactionData.get("version"))
                val attachmentData = JSON.getAsJsonObject(transactionData.get("attachment"))

                val transactionType = TransactionType.findTransactionType(type, subtype)
                        ?: throw BurstException.NotValidException("Invalid transaction type: $type, $subtype")
                val builder = Builder(dp, version, senderPublicKey, amountNQT, feeNQT, timestamp, deadline, transactionType.parseAttachment(attachmentData))
                        .referencedTransactionFullHash(referencedTransactionFullHash)
                        .signature(signature)
                        .height(height)
                if (transactionType.hasRecipient()) {
                    val recipientId = Convert.parseUnsignedLong(JSON.getAsString(transactionData.get("recipient")))
                    builder.recipientId(recipientId)
                }

                transactionType.parseAppendices(builder, attachmentData)

                if (version > 0) {
                    builder.ecBlockHeight(JSON.getAsInt(transactionData.get("ecBlockHeight")))
                    builder.ecBlockId(Convert.parseUnsignedLong(JSON.getAsString(transactionData.get("ecBlockId"))))
                }
                return builder.build()
            } catch (e: BurstException.NotValidException) {
                if (logger.isDebugEnabled) {
                    logger.debug("Failed to parse transaction: {}", JSON.toJsonString(transactionData))
                }
                throw e
            } catch (e: RuntimeException) {
                if (logger.isDebugEnabled) {
                    logger.debug("Failed to parse transaction: {}", JSON.toJsonString(transactionData))
                }
                throw e
            }

        }
    }
}
