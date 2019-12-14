package brs.entity

import brs.objects.Constants
import brs.objects.FluxValues
import brs.objects.Genesis
import brs.transaction.appendix.Appendix
import brs.transaction.appendix.Attachment
import brs.transaction.type.TransactionType
import brs.transaction.type.payment.MultiOutPayment
import brs.transaction.type.payment.MultiOutSamePayment
import brs.util.BurstException
import brs.util.byteArray.isZero
import brs.util.byteArray.skip
import brs.util.convert.*
import brs.util.crypto.Crypto
import brs.util.crypto.signUsing
import brs.util.crypto.verifySignature
import brs.util.delegates.Atomic
import brs.util.delegates.AtomicLateinit
import brs.util.delegates.AtomicLazy
import brs.util.json.*
import brs.util.logging.safeDebug
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or

class Transaction private constructor(private val dp: DependencyProvider, builder: Builder) : Comparable<Transaction> {
    val deadline: Short
    val senderPublicKey: ByteArray
    val recipientId: Long
    val amountPlanck: Long
    val feePlanck: Long
    val referencedTransactionFullHash: ByteArray?
    val type: TransactionType
    val ecBlockHeight: Int
    val ecBlockId: Long
    val version: Byte
    val timestamp: Int
    val attachment: Attachment.AbstractAttachment
    val message: Appendix.Message?
    val encryptedMessage: Appendix.EncryptedMessage?
    val encryptToSelfMessage: Appendix.EncryptToSelfMessage?
    val publicKeyAnnouncement: Appendix.PublicKeyAnnouncement?
    val appendages: List<Appendix>
    val appendagesSize: Int
    var height by AtomicLateinit<Int>()
    var blockId by AtomicLateinit<Long>()
    internal var block by Atomic<Block?>(null)
    var signature by Atomic<ByteArray?>(null)
    var blockTimestamp by AtomicLateinit<Int>()

    val expiration: Int
        get() = timestamp + deadline * 60

    init {
        this.timestamp = builder.timestamp
        this.deadline = builder.deadline
        this.senderPublicKey = builder.senderPublicKey
        this.recipientId = builder.recipientId
        this.amountPlanck = builder.amountPlanck
        this.referencedTransactionFullHash = builder.referencedTransactionFullHash
        this.signature = builder.signature
        this.type = builder.type
        this.version = builder.version
        builder.blockId?.let { this.blockId = it }
        this.height = builder.height
    }

    var id by AtomicLazy { fullHash.fullHashToId() }
    val stringId by lazy { id.toUnsignedString() }
    var senderId by AtomicLazy {
        if (type.isSigned) {
            senderPublicKey.publicKeyToId()
        } else 0
    }
    var fullHash: ByteArray by AtomicLazy {
        check(signature != null || !type.isSigned) { "Transaction is not signed yet" }
        val data = toBytes(false)
        val signatureHash = if (signature != null) Crypto.sha256().digest(signature) else Constants.SHA256_64_ZEROS
        val digest = Crypto.sha256()
        digest.update(data)
        digest.digest(signatureHash)
    }

    init {
        builder.id?.let { this.id = it }
        builder.senderId?.let { this.senderId = it }
        builder.blockTimestamp?.let { this.blockTimestamp = it }
        builder.fullHash?.let { this.fullHash = it }
        this.ecBlockHeight = builder.ecBlockHeight
        this.ecBlockId = builder.ecBlockId

        val appendixList = mutableListOf<Appendix>()
        this.attachment = builder.attachment
        appendixList.add(this.attachment)
        this.message = builder.message
        if (message != null) {
            appendixList.add(this.message)
        }
        this.encryptedMessage = builder.encryptedMessage
        if (encryptedMessage != null) appendixList.add(this.encryptedMessage)
        this.publicKeyAnnouncement = builder.publicKeyAnnouncement
        if (publicKeyAnnouncement != null) appendixList.add(this.publicKeyAnnouncement)
        this.encryptToSelfMessage = builder.encryptToSelfMessage
        if (encryptToSelfMessage != null) appendixList.add(this.encryptToSelfMessage)
        this.appendages = appendixList
        var countAppendeges = 0
        for (appendage in appendages) {
            countAppendeges += appendage.size
        }
        this.appendagesSize = countAppendeges
        val effectiveHeight = if (height < Integer.MAX_VALUE) height else dp.blockchainService.height
        val minimumFeePlanck = type.minimumFeePlanck(effectiveHeight, countAppendeges)
        feePlanck = if (type.isSigned) {
            if (builder.feePlanck in 1 until minimumFeePlanck) {
                throw BurstException.NotValidException(
                    String.format(
                        "Requested fee %d less than the minimum fee %d",
                        builder.feePlanck, minimumFeePlanck
                    )
                )
            }
            if (builder.feePlanck <= 0) {
                minimumFeePlanck
            } else {
                builder.feePlanck
            }
        } else {
            builder.feePlanck
        }

        if ((type.isSigned) && (deadline < 1
                    || feePlanck > Constants.MAX_BALANCE_PLANCK
                    || amountPlanck < 0
                    || amountPlanck > Constants.MAX_BALANCE_PLANCK)
        ) {
            throw BurstException.NotValidException(
                "Invalid transaction parameters:\n type: " + type + ", timestamp: " + timestamp
                        + ", deadline: " + deadline + ", fee: " + feePlanck + ", amount: " + amountPlanck
            )
        }

        if (type !== attachment.transactionType) {
            throw BurstException.NotValidException("Invalid attachment $attachment for transaction of type $type")
        }

        if (!type.hasRecipient() && attachment.transactionType !is MultiOutPayment && attachment.transactionType !is MultiOutSamePayment && (recipientId != 0L || amountPlanck != 0L)) {
            throw BurstException.NotValidException("Transactions of this type must have recipient == Genesis, amount == 0")
        }

        for (appendage in appendages) {
            if (!appendage.verifyVersion(this.version)) {
                throw BurstException.NotValidException("Invalid attachment version ${appendage.version} for transaction version ${this.version}")
            }
        }
    }

    fun toBytes(includeSignature: Boolean = true): ByteArray {
        try {
            val buffer = ByteBuffer.allocate(size)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.put(type.type)
            buffer.put((version.toInt() shl 4).toByte() or (type.subtype and 0xff.toByte()))
            buffer.putInt(timestamp)
            buffer.putShort(deadline)
            if (type.isSigned || !dp.fluxCapacitorService.getValue(FluxValues.AT_FIX_BLOCK_4)) {
                buffer.put(senderPublicKey)
            } else {
                buffer.putLong(senderId)
                buffer.skip(24)
            }
            buffer.putLong(if (type.hasRecipient()) recipientId else Genesis.CREATOR_ID)
            buffer.putLong(amountPlanck)
            buffer.putLong(feePlanck)
            if (referencedTransactionFullHash != null) {
                buffer.put(referencedTransactionFullHash)
            } else {
                buffer.skip(32)
            }
            if (includeSignature && signature != null) buffer.put(signature) else buffer.skip(64)
            if (version > 0) {
                buffer.putInt(flags)
                buffer.putInt(ecBlockHeight)
                buffer.putLong(ecBlockId)
            }
            appendages.forEach { appendage -> appendage.putBytes(buffer) }
            return buffer.array()
        } catch (e: Exception) {
            logger.safeDebug { "Failed to get transaction bytes for transaction: ${toJsonObject().toJsonString()}" }
            throw e
        }
    }

    fun toUnsignedBytes(): ByteArray = toBytes(false)

    fun toJsonObject(): JsonObject {
        val json = JsonObject()
        json.addProperty("type", type.type)
        json.addProperty("subtype", type.subtype)
        json.addProperty("timestamp", timestamp)
        json.addProperty("deadline", deadline)
        json.addProperty("senderPublicKey", senderPublicKey.toHexString())
        if (type.hasRecipient()) {
            json.addProperty("recipient", recipientId.toUnsignedString())
        }
        json.addProperty("amountNQT", amountPlanck)
        json.addProperty("feeNQT", feePlanck)
        if (referencedTransactionFullHash != null) {
            json.addProperty("referencedTransactionFullHash", referencedTransactionFullHash.toHexString())
        }
        json.addProperty("ecBlockHeight", ecBlockHeight)
        json.addProperty("ecBlockId", ecBlockId.toUnsignedString())
        json.addProperty("signature", signature?.toHexString() ?: "")
        val attachmentJSON = JsonObject()
        appendages.forEach { appendage -> attachmentJSON.addAll(appendage.jsonObject) }
        json.add("attachment", attachmentJSON)
        json.addProperty("version", version)
        return json
    }

    val size: Int
        get() = SIGNATURE_OFFSET + 64 + (if (version > 0) 4 + 4 + 8 else 0) + appendagesSize

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
        get() = type.getDuplicationKey(this)

    class Builder(
        internal val dp: DependencyProvider,
        internal val version: Byte,
        internal val senderPublicKey: ByteArray,
        internal val amountPlanck: Long,
        internal val feePlanck: Long,
        internal val timestamp: Int,
        internal val deadline: Short,
        internal val attachment: Attachment.AbstractAttachment
    ) {
        internal val type = attachment.transactionType

        internal var recipientId: Long = 0L
        internal var referencedTransactionFullHash: ByteArray? = null
        internal var signature: ByteArray? = null
        internal var message: Appendix.Message? = null
        internal var encryptedMessage: Appendix.EncryptedMessage? = null
        internal var encryptToSelfMessage: Appendix.EncryptToSelfMessage? = null
        internal var publicKeyAnnouncement: Appendix.PublicKeyAnnouncement? = null
        internal var blockId: Long? = null
        internal var height = Integer.MAX_VALUE
        internal var id: Long? = null
        internal var senderId: Long? = null
        internal var blockTimestamp: Int? = null
        internal var fullHash: ByteArray? = null
        internal var ecBlockHeight: Int = 0
        internal var ecBlockId: Long = 0

        fun build(): Transaction {
            return Transaction(dp, this)
        }

        fun recipientId(recipientId: Long): Builder {
            this.recipientId = recipientId
            return this
        }

        fun referencedTransactionFullHash(referencedTransactionFullHash: ByteArray): Builder {
            this.referencedTransactionFullHash = referencedTransactionFullHash
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

        fun fullHash(fullHash: ByteArray): Builder {
            this.fullHash = fullHash
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

    fun setBlock(block: Block) {
        this.block = block
        this.blockId = block.id
        this.height = block.height
        this.blockTimestamp = block.timestamp
    }

    internal fun unsetBlock() {
        this.block = null
        this.blockId = 0
        this.blockTimestamp = -1
        // must keep the height set, as transactions already having been included in a popped-off block before
        // get priority when sorted for inclusion in a new block
    }

    fun sign(secretPhrase: String) {
        check(signature == null) { "Transaction already signed" }
        signature = toBytes().signUsing(secretPhrase)
    }

    override fun equals(other: Any?): Boolean {
        return other is Transaction && this.id == other.id
    }

    override fun hashCode(): Int {
        return (id xor id.ushr(32)).toInt()
    }

    override fun compareTo(other: Transaction): Int {
        return this.id.compareTo(other.id)
    }

    fun verifySignature(): Boolean {
        val data = toBytes(false)
        return data.verifySignature(signature ?: return false, senderPublicKey, true)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Transaction::class.java)

        fun parseTransaction(dp: DependencyProvider, bytes: ByteArray): Transaction {
            try {
                val buffer = ByteBuffer.wrap(bytes)
                buffer.order(ByteOrder.LITTLE_ENDIAN)
                val type = buffer.get()
                var subtype = buffer.get()
                val version = ((subtype.toInt() and 0xF0) shr 4).toByte()
                subtype = (subtype and 0x0F)
                val timestamp = buffer.get().toInt()
                val deadline = buffer.short
                val senderPublicKey = ByteArray(32)
                buffer.get(senderPublicKey)
                val recipientId = buffer.long
                val amountPlanck = buffer.long
                val feePlanck = buffer.long
                val referencedTransactionFullHash = ByteArray(32)
                buffer.get(referencedTransactionFullHash)
                val signature = ByteArray(64)
                buffer.get(signature)
                var flags = 0
                var ecBlockHeight = 0
                var ecBlockId: Long = 0
                if (version > 0) {
                    flags = buffer.get().toInt()
                    ecBlockHeight = buffer.get().toInt()
                    ecBlockId = buffer.long
                }
                val transactionType = TransactionType.findTransactionType(dp, type, subtype) ?: error("Could not find transaction with type $type and subtype $subtype")
                val builder = Builder(
                    dp,
                    version,
                    senderPublicKey,
                    amountPlanck,
                    feePlanck,
                    timestamp,
                    deadline,
                    transactionType.parseAttachment(buffer, version)
                )
                    .signature(signature.emptyToNull())
                    .ecBlockHeight(ecBlockHeight)
                    .ecBlockId(ecBlockId)
                if (!referencedTransactionFullHash.isZero()) {
                    builder.referencedTransactionFullHash(referencedTransactionFullHash)
                }
                if (transactionType.hasRecipient()) {
                    builder.recipientId(recipientId)
                }
                transactionType.parseAppendices(builder, flags, version, buffer)
                return builder.build()
            } catch (e: BurstException.NotValidException) {
                logger.safeDebug(e) { "Failed to parse transaction bytes: ${bytes.toHexString()}" }
                throw e
            } catch (e: Exception) {
                logger.safeDebug(e) { "Failed to parse transaction bytes: ${bytes.toHexString()}" }
                throw e
            }
        }

        internal fun parseTransaction(dp: DependencyProvider, transactionData: JsonObject, height: Int = dp.blockchainService.height): Transaction {
            try {
                val type = transactionData.get("type").mustGetAsByte("type")
                val subtype = transactionData.get("subtype").mustGetAsByte("subtype")
                val timestamp = transactionData.get("timestamp").mustGetAsInt("timestamp")
                val deadline = transactionData.get("deadline").mustGetAsShort("deadline")
                val senderPublicKey =
                    transactionData.get("senderPublicKey").mustGetAsString("senderPublicKey").parseHexString()
                val amountPlanck = transactionData.get("amountNQT").mustGetAsLong("amountNQT")
                val feePlanck = transactionData.get("feeNQT").mustGetAsLong("feeNQT")
                val referencedTransactionFullHash =
                    transactionData.get("referencedTransactionFullHash").safeGetAsString()
                val signature = transactionData.get("signature").mustGetAsString("signature").parseHexString()
                val version = transactionData.get("version").mustGetAsByte("version")
                val attachmentData = transactionData.get("attachment").mustGetAsJsonObject("attachment")

                val transactionType =
                    TransactionType.findTransactionType(dp, type, subtype) ?: throw BurstException.NotValidException(
                        "Invalid transaction type: $type, $subtype"
                    )
                val builder = Builder(
                    dp,
                    version,
                    senderPublicKey,
                    amountPlanck,
                    feePlanck,
                    timestamp,
                    deadline,
                    transactionType.parseAttachment(attachmentData)
                )
                    .signature(signature)
                    .height(height)
                if (!referencedTransactionFullHash.isNullOrEmpty()) {
                    builder.referencedTransactionFullHash(referencedTransactionFullHash.parseHexString())
                }
                if (transactionType.hasRecipient()) {
                    val recipientId = transactionData.get("recipient").safeGetAsString().parseUnsignedLong()
                    builder.recipientId(recipientId)
                }

                transactionType.parseAppendices(builder, attachmentData)

                if (version > 0) {
                    builder.ecBlockHeight(transactionData.get("ecBlockHeight").mustGetAsInt("ecBlockHeight"))
                    builder.ecBlockId(transactionData.get("ecBlockId").safeGetAsString().parseUnsignedLong())
                }
                return builder.build()
            } catch (e: BurstException.NotValidException) {
                logger.safeDebug(e) { "Failed to parse transaction: ${transactionData.toJsonString()}" }
                throw e
            } catch (e: Exception) {
                logger.safeDebug(e) { "Failed to parse transaction: ${transactionData.toJsonString()}" }
                throw e
            }
        }

        private const val SIGNATURE_OFFSET = 1 + 1 + 4 + 2 + 32 + 8 + 8 + 8 + 32
    }
}
