package brs

import brs.crypto.EncryptedData
import brs.fluxcapacitor.FluxValues
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.grpc.proto.toByteString
import brs.util.Convert
import brs.util.JSON
import brs.util.parseHexString
import brs.util.toHexString
import com.google.gson.JsonObject
import com.google.protobuf.Any
import java.nio.ByteBuffer
import java.util.*

interface Appendix {

    val size: Int
    val jsonObject: JsonObject
    val version: Byte
    val protobufMessage: Any
    fun putBytes(buffer: ByteBuffer)

    abstract class AbstractAppendix : Appendix {

        final override val version: Byte

        internal abstract val appendixName: String

        override val size: Int
            get() = mySize + if (version > 0) 1 else 0

        internal abstract val mySize: Int

        override val jsonObject: JsonObject
            get() {
                val json = JsonObject()
                if (version > 0) {
                    json.addProperty("version.$appendixName", version)
                }
                putMyJSON(json)
                return json
            }

        internal constructor(attachmentData: JsonObject) {
            version = JSON.getAsByte(attachmentData.get("version.$appendixName"))
        }

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) {
            version = if (transactionVersion.toInt() == 0) 0 else buffer.get()
        }

        internal constructor(version: Byte) {
            this.version = version
        }

        internal constructor(dp: DependencyProvider, blockchainHeight: Int) {
            this.version = (if (dp.fluxCapacitor.getValue(FluxValues.DIGITAL_GOODS_STORE, blockchainHeight)) 1 else 0).toByte()
        }

        override fun putBytes(buffer: ByteBuffer) {
            if (version > 0) {
                buffer.put(version)
            }
            putMyBytes(buffer)
        }

        internal abstract fun putMyBytes(buffer: ByteBuffer)

        internal abstract fun putMyJSON(attachment: JsonObject)

        open fun verifyVersion(transactionVersion: Byte): Boolean {
            return if (transactionVersion.toInt() == 0) version.toInt() == 0 else version > 0
        }

        @Throws(BurstException.ValidationException::class)
        abstract fun validate(transaction: Transaction)

        abstract fun apply(transaction: Transaction, senderAccount: Account, recipientAccount: Account)
    }

    class Message : AbstractAppendix {

        val messageBytes: ByteArray
        val isText: Boolean

        override val appendixName: String
            get() = "Message"

        override val mySize: Int
            get() = 4 + messageBytes!!.size

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.MessageAppendix.newBuilder()
                    .setVersion(super.version.toInt())
                    .setMessage(messageBytes.toByteString())
                    .setIsText(isText)
                    .build())

        @Throws(BurstException.NotValidException::class)
        constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            var messageLength = buffer.int
            this.isText = messageLength < 0 // ugly hack
            if (messageLength < 0) {
                messageLength = messageLength and Integer.MAX_VALUE
            }
            if (messageLength > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
                throw BurstException.NotValidException("Invalid arbitrary message length: $messageLength")
            }
            this.messageBytes = ByteArray(messageLength)
            buffer.get(this.messageBytes)
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            val messageString = JSON.getAsString(attachmentData.get("message"))
            this.isText = JSON.getAsBoolean(attachmentData.get("messageIsText"))
            this.messageBytes = if (isText) Convert.toBytes(messageString) else messageString.parseHexString()
        }

        constructor(dp: DependencyProvider, message: ByteArray, blockchainHeight: Int) : super(dp, blockchainHeight) {
            this.messageBytes = message
            this.isText = false
        }

        constructor(dp: DependencyProvider, string: String, blockchainHeight: Int) : super(dp, blockchainHeight) {
            this.messageBytes = Convert.toBytes(string)
            this.isText = true
        }

        constructor(dp: DependencyProvider, messageAppendix: BrsApi.MessageAppendix, blockchainHeight: Int) : super(dp, blockchainHeight) {
            this.messageBytes = messageAppendix.message.toByteArray()
            this.isText = messageAppendix.isText
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putInt(if (isText) messageBytes!!.size or Integer.MIN_VALUE else messageBytes!!.size)
            buffer.put(messageBytes)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty("message", if (isText) Convert.toString(messageBytes!!) else messageBytes.toHexString())
            attachment.addProperty("messageIsText", isText)
        }

        @Throws(BurstException.ValidationException::class)
        override fun validate(transaction: Transaction) {
            if (this.isText && transaction.version.toInt() == 0) {
                throw BurstException.NotValidException("Text messages not yet enabled")
            }
            if (transaction.version.toInt() == 0 && transaction.attachment !== Attachment.ARBITRARY_MESSAGE) {
                throw BurstException.NotValidException("Message attachments not enabled for version 0 transactions")
            }
            if (messageBytes!!.size > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
                throw BurstException.NotValidException("Invalid arbitrary message length: " + messageBytes.size)
            }
        }

        override fun apply(transaction: Transaction, senderAccount: Account, recipientAccount: Account) {
            // Do nothing by default
        }

        companion object {
            fun parse(attachmentData: JsonObject): Message? {
                return if (attachmentData.get("message") == null) {
                    null
                } else Message(attachmentData)
            }
        }
    }

    abstract class AbstractEncryptedMessage : AbstractAppendix {

        val encryptedData: EncryptedData
        val isText: Boolean

        override val mySize: Int
            get() = 4 + encryptedData.size

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.EncryptedMessageAppendix.newBuilder()
                    .setVersion(super.version.toInt())
                    .setEncryptedData(ProtoBuilder.buildEncryptedData(encryptedData))
                    .setType(type)
                    .build())

        protected abstract val type: BrsApi.EncryptedMessageAppendix.Type

        @Throws(BurstException.NotValidException::class)
        constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            var length = buffer.int
            this.isText = length < 0
            if (length < 0) {
                length = length and Integer.MAX_VALUE
            }
            this.encryptedData = EncryptedData.readEncryptedData(buffer, length, Constants.MAX_ENCRYPTED_MESSAGE_LENGTH)
        }

        constructor(attachmentJSON: JsonObject, encryptedMessageJSON: JsonObject) : super(attachmentJSON) {
            val data = JSON.getAsString(encryptedMessageJSON.get("data")).parseHexString()
            val nonce = JSON.getAsString(encryptedMessageJSON.get("nonce")).parseHexString()
            this.encryptedData = EncryptedData(data, nonce)
            this.isText = JSON.getAsBoolean(encryptedMessageJSON.get("isText"))
        }

        constructor(dp: DependencyProvider, encryptedData: EncryptedData, isText: Boolean, blockchainHeight: Int) : super(dp, blockchainHeight) {
            this.encryptedData = encryptedData
            this.isText = isText
        }

        constructor(dp: DependencyProvider, encryptedMessageAppendix: BrsApi.EncryptedMessageAppendix, blockchainHeight: Int) : super(dp, blockchainHeight) {
            this.encryptedData = ProtoBuilder.parseEncryptedData(encryptedMessageAppendix.encryptedData)
            this.isText = encryptedMessageAppendix.isText
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putInt(if (isText) encryptedData.data.size or Integer.MIN_VALUE else encryptedData.data.size)
            buffer.put(encryptedData.data)
            buffer.put(encryptedData.nonce)
        }

        override fun putMyJSON(json: JsonObject) {
            json.addProperty("data", encryptedData.data.toHexString())
            json.addProperty("nonce", encryptedData.nonce.toHexString())
            json.addProperty("isText", isText)
        }

        @Throws(BurstException.ValidationException::class)
        override fun validate(transaction: Transaction) {
            if (encryptedData.data.size > Constants.MAX_ENCRYPTED_MESSAGE_LENGTH) {
                throw BurstException.NotValidException("Max encrypted message length exceeded")
            }
            if (encryptedData.nonce.size != 32 && encryptedData.data.size > 0 || encryptedData.nonce.size != 0 && encryptedData.data.size == 0) {
                throw BurstException.NotValidException("Invalid nonce length " + encryptedData.nonce.size)
            }
        }

        override fun apply(transaction: Transaction, senderAccount: Account, recipientAccount: Account) {
        }
    }

    class EncryptedMessage : AbstractEncryptedMessage {

        override val appendixName: String
            get() = "EncryptedMessage"

        override val type: BrsApi.EncryptedMessageAppendix.Type
            get() = BrsApi.EncryptedMessageAppendix.Type.TO_RECIPIENT

        @Throws(BurstException.ValidationException::class)
        constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData, JSON.getAsJsonObject(attachmentData.get("encryptedMessage"))) {}

        constructor(dp: DependencyProvider, encryptedData: EncryptedData, isText: Boolean, blockchainHeight: Int) : super(dp, encryptedData, isText, blockchainHeight) {}

        constructor(dp: DependencyProvider, encryptedMessageAppendix: BrsApi.EncryptedMessageAppendix, blockchainHeight: Int) : super(dp, encryptedMessageAppendix, blockchainHeight) {
            require(encryptedMessageAppendix.type == BrsApi.EncryptedMessageAppendix.Type.TO_RECIPIENT)
        }

        override fun putMyJSON(json: JsonObject) {
            val encryptedMessageJSON = JsonObject()
            super.putMyJSON(encryptedMessageJSON)
            json.add("encryptedMessage", encryptedMessageJSON)
        }

        @Throws(BurstException.ValidationException::class)
        override fun validate(transaction: Transaction) {
            super.validate(transaction)
            if (!transaction.type.hasRecipient()) {
                throw BurstException.NotValidException("Encrypted messages cannot be attached to transactions with no recipient")
            }
            if (transaction.version.toInt() == 0) {
                throw BurstException.NotValidException("Encrypted message attachments not enabled for version 0 transactions")
            }
        }

        companion object {

            fun parse(attachmentData: JsonObject): EncryptedMessage? {
                return if (attachmentData.get("encryptedMessage") == null) {
                    null
                } else EncryptedMessage(attachmentData)
            }
        }

    }

    class EncryptToSelfMessage : AbstractEncryptedMessage {

        override val appendixName: String
            get() = "EncryptToSelfMessage"

        override val type: BrsApi.EncryptedMessageAppendix.Type
            get() = BrsApi.EncryptedMessageAppendix.Type.TO_SELF

        @Throws(BurstException.ValidationException::class)
        constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData, JSON.getAsJsonObject(attachmentData.get("encryptToSelfMessage"))) {}

        constructor(dp: DependencyProvider, encryptedData: EncryptedData, isText: Boolean, blockchainHeight: Int) : super(dp, encryptedData, isText, blockchainHeight) {}

        constructor(dp: DependencyProvider, encryptedMessageAppendix: BrsApi.EncryptedMessageAppendix, blockchainHeight: Int) : super(dp, encryptedMessageAppendix, blockchainHeight) {
            require(encryptedMessageAppendix.type == BrsApi.EncryptedMessageAppendix.Type.TO_SELF)
        }

        override fun putMyJSON(json: JsonObject) {
            val encryptToSelfMessageJSON = JsonObject()
            super.putMyJSON(encryptToSelfMessageJSON)
            json.add("encryptToSelfMessage", encryptToSelfMessageJSON)
        }

        @Throws(BurstException.ValidationException::class)
        override fun validate(transaction: Transaction) {
            super.validate(transaction)
            if (transaction.version.toInt() == 0) {
                throw BurstException.NotValidException("Encrypt-to-self message attachments not enabled for version 0 transactions")
            }
        }

        companion object {
            fun parse(attachmentData: JsonObject): EncryptToSelfMessage? {
                return if (attachmentData.get("encryptToSelfMessage") == null) {
                    null
                } else EncryptToSelfMessage(attachmentData)
            }
        }

    }

    class PublicKeyAnnouncement : AbstractAppendix {

        private val dp: DependencyProvider
        val publicKey: ByteArray

        override val appendixName: String
            get() = "PublicKeyAnnouncement"

        override val mySize: Int
            get() = 32

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.PublicKeyAnnouncementAppendix.newBuilder()
                    .setVersion(super.version.toInt())
                    .setRecipientPublicKey(publicKey.toByteString())
                    .build())

        constructor(dp: DependencyProvider, buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.publicKey = ByteArray(32)
            this.dp = dp
            buffer.get(this.publicKey)
        }

        internal constructor(dp: DependencyProvider, attachmentData: JsonObject) : super(attachmentData) {
            this.publicKey = JSON.getAsString(attachmentData.get("recipientPublicKey")).parseHexString()
            this.dp = dp
        }

        constructor(dp: DependencyProvider, publicKey: ByteArray, blockchainHeight: Int) : super(dp, blockchainHeight) {
            this.publicKey = publicKey
            this.dp = dp
        }

        constructor(dp: DependencyProvider, publicKeyAnnouncementAppendix: BrsApi.PublicKeyAnnouncementAppendix, blockchainHeight: Int) : super(dp, blockchainHeight) {
            this.publicKey = publicKeyAnnouncementAppendix.recipientPublicKey.toByteArray()
            this.dp = dp
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.put(publicKey!!)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty("recipientPublicKey", publicKey.toHexString())
        }

        @Throws(BurstException.ValidationException::class)
        override fun validate(transaction: Transaction) {
            if (!transaction.type.hasRecipient()) {
                throw BurstException.NotValidException("PublicKeyAnnouncement cannot be attached to transactions with no recipient")
            }
            if (publicKey!!.size != 32) {
                throw BurstException.NotValidException("Invalid recipient public key length: " + publicKey.toHexString()!!)
            }
            val recipientId = transaction.recipientId
            if (Account.getId(this.publicKey) != recipientId) {
                throw BurstException.NotValidException("Announced public key does not match recipient accountId")
            }
            if (transaction.version.toInt() == 0) {
                throw BurstException.NotValidException("Public key announcements not enabled for version 0 transactions")
            }
            val recipientAccount = Account.getAccount(dp, recipientId)
            if (recipientAccount != null && recipientAccount.publicKey != null && !Arrays.equals(publicKey, recipientAccount.publicKey)) {
                throw BurstException.NotCurrentlyValidException("A different public key for this account has already been announced")
            }
        }

        override fun apply(transaction: Transaction, senderAccount: Account, recipientAccount: Account) {
            if (recipientAccount.setOrVerify(dp, publicKey, transaction.height)) {
                recipientAccount.apply(dp, this.publicKey, transaction.height)
            }
        }

        companion object {
            fun parse(dp: DependencyProvider, attachmentData: JsonObject): PublicKeyAnnouncement? {
                return if (attachmentData.get("recipientPublicKey") == null) {
                    null
                } else PublicKeyAnnouncement(dp, attachmentData)
            }
        }
    }
}
