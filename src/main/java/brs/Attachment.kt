package brs

import brs.Appendix.Companion.dp
import brs.TransactionType.Payment
import brs.at.AtConstants
import brs.crypto.EncryptedData
import brs.grpc.proto.BrsApi
import brs.grpc.proto.ProtoBuilder
import brs.util.Convert
import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.protobuf.Any
import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException

import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.Map.Entry

import brs.http.common.Parameters.ALIAS_PARAMETER
import brs.http.common.Parameters.AMOUNT_NQT_PARAMETER
import brs.http.common.Parameters.ASSET_PARAMETER
import brs.http.common.Parameters.COMMENT_PARAMETER
import brs.http.common.Parameters.CREATION_BYTES_PARAMETER
import brs.http.common.Parameters.DEADLINE_ACTION_PARAMETER
import brs.http.common.Parameters.DEADLINE_PARAMETER
import brs.http.common.Parameters.DECIMALS_PARAMETER
import brs.http.common.Parameters.DECISION_PARAMETER
import brs.http.common.Parameters.DELIVERY_DEADLINE_TIMESTAMP_PARAMETER
import brs.http.common.Parameters.DELTA_QUANTITY_PARAMETER
import brs.http.common.Parameters.DESCRIPTION_PARAMETER
import brs.http.common.Parameters.DISCOUNT_NQT_PARAMETER
import brs.http.common.Parameters.ESCROW_ID_PARAMETER
import brs.http.common.Parameters.FREQUENCY_PARAMETER
import brs.http.common.Parameters.GOODS_DATA_PARAMETER
import brs.http.common.Parameters.GOODS_IS_TEXT_PARAMETER
import brs.http.common.Parameters.GOODS_NONCE_PARAMETER
import brs.http.common.Parameters.GOODS_PARAMETER
import brs.http.common.Parameters.NAME_PARAMETER
import brs.http.common.Parameters.ORDER_PARAMETER
import brs.http.common.Parameters.PERIOD_PARAMETER
import brs.http.common.Parameters.PRICE_NQT_PARAMETER
import brs.http.common.Parameters.PURCHASE_PARAMETER
import brs.http.common.Parameters.QUANTITY_PARAMETER
import brs.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.http.common.Parameters.RECIPIENTS_PARAMETER
import brs.http.common.Parameters.RECIPIENTS_RESPONSE
import brs.http.common.Parameters.REFUND_NQT_PARAMETER
import brs.http.common.Parameters.REQUIRED_SIGNERS_PARAMETER
import brs.http.common.Parameters.SIGNERS_PARAMETER
import brs.http.common.Parameters.SUBSCRIPTION_ID_PARAMETER
import brs.http.common.Parameters.URI_PARAMETER
import brs.http.common.ResultFields.ALIAS_RESPONSE
import brs.http.common.ResultFields.AMOUNT_NQT_RESPONSE
import brs.http.common.ResultFields.ASSET_RESPONSE
import brs.http.common.ResultFields.COMMENT_RESPONSE
import brs.http.common.ResultFields.CREATION_BYTES_RESPONSE
import brs.http.common.ResultFields.DEADLINE_ACTION_RESPONSE
import brs.http.common.ResultFields.DEADLINE_RESPONSE
import brs.http.common.ResultFields.DECIMALS_RESPONSE
import brs.http.common.ResultFields.DECISION_RESPONSE
import brs.http.common.ResultFields.DELIVERY_DEADLINE_TIMESTAMP_RESPONSE
import brs.http.common.ResultFields.DELTA_QUANTITY_RESPONSE
import brs.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.http.common.ResultFields.DISCOUNT_NQT_RESPONSE
import brs.http.common.ResultFields.ESCROW_ID_RESPONSE
import brs.http.common.ResultFields.FREQUENCY_RESPONSE
import brs.http.common.ResultFields.GOODS_DATA_RESPONSE
import brs.http.common.ResultFields.GOODS_IS_TEXT_RESPONSE
import brs.http.common.ResultFields.GOODS_NONCE_RESPONSE
import brs.http.common.ResultFields.GOODS_RESPONSE
import brs.http.common.ResultFields.NAME_RESPONSE
import brs.http.common.ResultFields.ORDER_RESPONSE
import brs.http.common.ResultFields.PERIOD_RESPONSE
import brs.http.common.ResultFields.PRICE_NQT_RESPONSE
import brs.http.common.ResultFields.PURCHASE_RESPONSE
import brs.http.common.ResultFields.QUANTITY_QNT_RESPONSE
import brs.http.common.ResultFields.QUANTITY_RESPONSE
import brs.http.common.ResultFields.REFUND_NQT_RESPONSE
import brs.http.common.ResultFields.REQUIRED_SIGNERS_RESPONSE
import brs.http.common.ResultFields.SIGNERS_RESPONSE
import brs.http.common.ResultFields.SUBSCRIPTION_ID_RESPONSE
import brs.http.common.ResultFields.TAGS_RESPONSE
import brs.http.common.ResultFields.URI_RESPONSE
import java.util.function.Consumer

interface Attachment : Appendix {

    val transactionType: TransactionType

    abstract class AbstractAttachment : Appendix.AbstractAppendix, Attachment {

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion)

        internal constructor(attachmentData: JsonObject) : super(attachmentData)

        internal constructor(version: Byte) : super(version)

        internal constructor(blockchainHeight: Int) : super(blockchainHeight)

        @Throws(BurstException.ValidationException::class)
        override fun validate(transaction: Transaction) {
            transactionType.validateAttachment(transaction)
        }

        override fun apply(transaction: Transaction, senderAccount: Account, recipientAccount: Account) {
            transactionType.apply(transaction, senderAccount, recipientAccount)
        }

        companion object {

            @Throws(InvalidProtocolBufferException::class, BurstException.NotValidException::class)
            fun parseProtobufMessage(attachment: Any): AbstractAttachment {
                // Yes, this is fairly horrible. I wish there was a better way to do this but any does not let us switch on its contained class.
                if (attachment.`is`(BrsApi.OrdinaryPaymentAttachment::class.java)) {
                    return ORDINARY_PAYMENT
                } else if (attachment.`is`(BrsApi.ArbitraryMessageAttachment::class.java)) {
                    return ARBITRARY_MESSAGE
                } else if (attachment.`is`(BrsApi.ATPaymentAttachment::class.java)) {
                    return AT_PAYMENT
                } else if (attachment.`is`(BrsApi.MultiOutAttachment::class.java)) {
                    return PaymentMultiOutCreation(attachment.unpack(BrsApi.MultiOutAttachment::class.java))
                } else if (attachment.`is`(BrsApi.MultiOutSameAttachment::class.java)) {
                    return PaymentMultiSameOutCreation(attachment.unpack(BrsApi.MultiOutSameAttachment::class.java))
                } else if (attachment.`is`(BrsApi.AliasAssignmentAttachment::class.java)) {
                    return MessagingAliasAssignment(attachment.unpack(BrsApi.AliasAssignmentAttachment::class.java))
                } else if (attachment.`is`(BrsApi.AliasSellAttachment::class.java)) {
                    return MessagingAliasSell(attachment.unpack(BrsApi.AliasSellAttachment::class.java))
                } else if (attachment.`is`(BrsApi.AliasBuyAttachment::class.java)) {
                    return MessagingAliasBuy(attachment.unpack(BrsApi.AliasBuyAttachment::class.java))
                } else if (attachment.`is`(BrsApi.AccountInfoAttachment::class.java)) {
                    return MessagingAccountInfo(attachment.unpack(BrsApi.AccountInfoAttachment::class.java))
                } else if (attachment.`is`(BrsApi.AssetIssuanceAttachment::class.java)) {
                    return ColoredCoinsAssetIssuance(attachment.unpack(BrsApi.AssetIssuanceAttachment::class.java))
                } else if (attachment.`is`(BrsApi.AssetTransferAttachment::class.java)) {
                    return ColoredCoinsAssetTransfer(attachment.unpack(BrsApi.AssetTransferAttachment::class.java))
                } else if (attachment.`is`(BrsApi.AssetOrderPlacementAttachment::class.java)) {
                    val placementAttachment = attachment.unpack(BrsApi.AssetOrderPlacementAttachment::class.java)
                    if (placementAttachment.type == BrsApi.OrderType.ASK) {
                        return ColoredCoinsAskOrderPlacement(placementAttachment)
                    } else if (placementAttachment.type == BrsApi.OrderType.BID) {
                        return ColoredCoinsBidOrderPlacement(placementAttachment)
                    }
                } else if (attachment.`is`(BrsApi.AssetOrderCancellationAttachment::class.java)) {
                    val placementAttachment = attachment.unpack(BrsApi.AssetOrderCancellationAttachment::class.java)
                    if (placementAttachment.type == BrsApi.OrderType.ASK) {
                        return ColoredCoinsAskOrderCancellation(placementAttachment)
                    } else if (placementAttachment.type == BrsApi.OrderType.BID) {
                        return ColoredCoinsBidOrderCancellation(placementAttachment)
                    }
                } else if (attachment.`is`(BrsApi.DigitalGoodsListingAttachment::class.java)) {
                    return DigitalGoodsListing(attachment.unpack(BrsApi.DigitalGoodsListingAttachment::class.java))
                } else if (attachment.`is`(BrsApi.DigitalGoodsDelistingAttachment::class.java)) {
                    return DigitalGoodsDelisting(attachment.unpack(BrsApi.DigitalGoodsDelistingAttachment::class.java))
                } else if (attachment.`is`(BrsApi.DigitalGoodsPriceChangeAttachment::class.java)) {
                    return DigitalGoodsPriceChange(attachment.unpack(BrsApi.DigitalGoodsPriceChangeAttachment::class.java))
                } else if (attachment.`is`(BrsApi.DigitalGoodsQuantityChangeAttachment::class.java)) {
                    return DigitalGoodsQuantityChange(attachment.unpack(BrsApi.DigitalGoodsQuantityChangeAttachment::class.java))
                } else if (attachment.`is`(BrsApi.DigitalGoodsPurchaseAttachment::class.java)) {
                    return DigitalGoodsPurchase(attachment.unpack(BrsApi.DigitalGoodsPurchaseAttachment::class.java))
                } else if (attachment.`is`(BrsApi.DigitalGoodsDeliveryAttachment::class.java)) {
                    return DigitalGoodsDelivery(attachment.unpack(BrsApi.DigitalGoodsDeliveryAttachment::class.java))
                } else if (attachment.`is`(BrsApi.DigitalGoodsFeedbackAttachment::class.java)) {
                    return DigitalGoodsFeedback(attachment.unpack(BrsApi.DigitalGoodsFeedbackAttachment::class.java))
                } else if (attachment.`is`(BrsApi.DigitalGoodsRefundAttachment::class.java)) {
                    return DigitalGoodsRefund(attachment.unpack(BrsApi.DigitalGoodsRefundAttachment::class.java))
                } else if (attachment.`is`(BrsApi.EffectiveBalanceLeasingAttachment::class.java)) {
                    return AccountControlEffectiveBalanceLeasing(attachment.unpack(BrsApi.EffectiveBalanceLeasingAttachment::class.java))
                } else if (attachment.`is`(BrsApi.RewardRecipientAssignmentAttachment::class.java)) {
                    return BurstMiningRewardRecipientAssignment(attachment.unpack(BrsApi.RewardRecipientAssignmentAttachment::class.java))
                } else if (attachment.`is`(BrsApi.EscrowCreationAttachment::class.java)) {
                    return AdvancedPaymentEscrowCreation(attachment.unpack(BrsApi.EscrowCreationAttachment::class.java))
                } else if (attachment.`is`(BrsApi.EscrowSignAttachment::class.java)) {
                    return AdvancedPaymentEscrowSign(attachment.unpack(BrsApi.EscrowSignAttachment::class.java))
                } else if (attachment.`is`(BrsApi.EscrowResultAttachment::class.java)) {
                    return AdvancedPaymentEscrowResult(attachment.unpack(BrsApi.EscrowResultAttachment::class.java))
                } else if (attachment.`is`(BrsApi.SubscriptionSubscribeAttachment::class.java)) {
                    return AdvancedPaymentSubscriptionSubscribe(attachment.unpack(BrsApi.SubscriptionSubscribeAttachment::class.java))
                } else if (attachment.`is`(BrsApi.SubscriptionCancelAttachment::class.java)) {
                    return AdvancedPaymentSubscriptionCancel(attachment.unpack(BrsApi.SubscriptionCancelAttachment::class.java))
                } else if (attachment.`is`(BrsApi.SubscriptionPaymentAttachment::class.java)) {
                    return AdvancedPaymentSubscriptionPayment(attachment.unpack(BrsApi.SubscriptionPaymentAttachment::class.java))
                } else if (attachment.`is`(BrsApi.ATCreationAttachment::class.java)) {
                    return AutomatedTransactionsCreation(attachment.unpack(BrsApi.ATCreationAttachment::class.java))
                }
                // Default to ordinary payment
                return ORDINARY_PAYMENT
            }
        }
    }

    abstract class EmptyAttachment internal constructor() : AbstractAttachment(0.toByte()) {

        override val mySize: Int
            get() = 0

        override fun putMyBytes(buffer: ByteBuffer) {}

        override fun putMyJSON(attachment: JsonObject) {}

        override fun verifyVersion(transactionVersion: Byte): Boolean {
            return true
        }

    }

    class PaymentMultiOutCreation : AbstractAttachment {
        private val recipients = mutableListOf<List<Long>>()

        override val appendixName: String
            get() = "MultiOutCreation"

        override val mySize: Int
            get() = 1 + recipients.size * 16

        override val transactionType: TransactionType
            get() = Payment.MULTI_OUT

        val amountNQT: Long
            get() {
                var amountNQT: Long = 0
                for (recipient in recipients) {
                    amountNQT = Convert.safeAdd(amountNQT, recipient[1])
                }
                return amountNQT
            }

        override val protobufMessage: Any
            get() {
                val builder = BrsApi.MultiOutAttachment.newBuilder()
                        .setVersion(version.toInt())
                for (recipient in recipients) {
                    builder.addRecipients(BrsApi.MultiOutAttachment.MultiOutRecipient.newBuilder()
                            .setRecipient(recipient[0])
                            .setAmount(recipient[1]))
                }
                return Any.pack(builder.build())
            }

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {

            val numberOfRecipients = java.lang.Byte.toUnsignedInt(buffer.get())
            val recipientOf = mutableMapOf<Long, Boolean>>(numberOfRecipients)

            for (i in 0 until numberOfRecipients) {
                val recipientId = buffer.long
                val amountNQT = buffer.long

                if (recipientOf.containsKey(recipientId))
                    throw BurstException.NotValidException("Duplicate recipient on multi out transaction")

                if (amountNQT <= 0)
                    throw BurstException.NotValidException("Insufficient amountNQT on multi out transaction")

                recipientOf[recipientId] = true
                this.recipients.add(listOf(recipientId, amountNQT))
            }
            if (recipients.size > Constants.MAX_MULTI_OUT_RECIPIENTS || recipients.size <= 1) {
                throw BurstException.NotValidException(
                        "Invalid number of recipients listed on multi out transaction")
            }
        }

        @Throws(BurstException.NotValidException::class)
        internal constructor(attachmentData: JsonObject) : super(attachmentData) {

            val receipientsJson = JSON.getAsJsonArray(attachmentData.get(RECIPIENTS_PARAMETER))
            val recipientOf = mutableMapOf<Long, Boolean>>()

            for (recipientObject in receipientsJson) {
                val recipient = JSON.getAsJsonArray(recipientObject)

                val recipientId = BigInteger(JSON.getAsString(recipient.get(0))!!).toLong()
                val amountNQT = JSON.getAsLong(recipient.get(1))
                if (recipientOf.containsKey(recipientId))
                    throw BurstException.NotValidException("Duplicate recipient on multi out transaction")

                if (amountNQT <= 0)
                    throw BurstException.NotValidException("Insufficient amountNQT on multi out transaction")

                recipientOf[recipientId] = true
                this.recipients.add(listOf(recipientId, amountNQT))
            }
            if (receipientsJson.size() > Constants.MAX_MULTI_OUT_RECIPIENTS || receipientsJson.size() <= 1) {
                throw BurstException.NotValidException("Invalid number of recipients listed on multi out transaction")
            }
        }

        @Throws(BurstException.NotValidException::class)
        constructor(recipients: Collection<Entry<String, Long>>, blockchainHeight: Int) : super(blockchainHeight) {

            val recipientOf = mutableMapOf<Long, Boolean>>()
            for ((key, amountNQT) in recipients) {
                val recipientId = BigInteger(key).toLong()
                if (recipientOf.containsKey(recipientId))
                    throw BurstException.NotValidException("Duplicate recipient on multi out transaction")

                if (amountNQT <= 0)
                    throw BurstException.NotValidException("Insufficient amountNQT on multi out transaction")

                recipientOf[recipientId] = true
                this.recipients.add(listOf(recipientId, amountNQT))
            }
            if (recipients.size > Constants.MAX_MULTI_OUT_RECIPIENTS || recipients.size <= 1) {
                throw BurstException.NotValidException("Invalid number of recipients listed on multi out transaction")
            }
        }

        @Throws(BurstException.NotValidException::class)
        internal constructor(attachment: BrsApi.MultiOutAttachment) : super(attachment.version.toByte()) {
            val recipientOf = mutableMapOf<Long, Boolean>>()
            for (recipient in attachment.recipientsList) {
                val recipientId = recipient.recipient
                val amountNQT = recipient.amount
                if (recipientOf.containsKey(recipientId))
                    throw BurstException.NotValidException("Duplicate recipient on multi out transaction")

                if (amountNQT <= 0)
                    throw BurstException.NotValidException("Insufficient amountNQT on multi out transaction")

                recipientOf[recipientId] = true
                this.recipients.add(listOf(recipientId, amountNQT))
            }
            if (recipients.size > Constants.MAX_MULTI_OUT_RECIPIENTS || recipients.size <= 1) {
                throw BurstException.NotValidException("Invalid number of recipients listed on multi out transaction")
            }
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.put(this.recipients.size.toByte())
            this.recipients.forEach { a ->
                buffer.putLong(a[0])
                buffer.putLong(a[1])
            }
        }

        override fun putMyJSON(attachment: JsonObject) {
            val recipientsJSON = JsonArray()

            this.recipients.map { recipient ->
                        val recipientJSON = JsonArray()
                        recipientJSON.add(Convert.toUnsignedLong(recipient[0]))
                        recipientJSON.add(recipient[1].toString())
                        recipientJSON
                    }.forEach { recipientsJSON.add(it) }

            attachment.add(RECIPIENTS_RESPONSE, recipientsJSON)
        }

        fun getRecipients(): Collection<List<Long>> {
            return recipients
        }
    }

    open class PaymentMultiSameOutCreation : AbstractAttachment {

        private val recipients = mutableListOf<Long>()

        override val appendixName: String
            get() = "MultiSameOutCreation"

        override val mySize: Int
            get() = 1 + recipients.size * 8

        override val transactionType: TransactionType
            get() = Payment.MULTI_SAME_OUT

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.MultiOutSameAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .addAllRecipients(recipients)
                    .build())

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {

            val numberOfRecipients = java.lang.Byte.toUnsignedInt(buffer.get())
            val recipientOf = mutableMapOf<Long, Boolean>>(numberOfRecipients)

            for (i in 0 until numberOfRecipients) {
                val recipientId = buffer.long

                if (recipientOf.containsKey(recipientId))
                    throw BurstException.NotValidException("Duplicate recipient on multi same out transaction")

                recipientOf[recipientId] = true
                this.recipients.add(recipientId)
            }
            if (recipients.size > Constants.MAX_MULTI_SAME_OUT_RECIPIENTS || recipients.size <= 1) {
                throw BurstException.NotValidException("Invalid number of recipients listed on multi same out transaction")
            }
        }

        @Throws(BurstException.NotValidException::class)
        internal constructor(attachmentData: JsonObject) : super(attachmentData) {

            val recipientsJson = JSON.getAsJsonArray(attachmentData.get(RECIPIENTS_PARAMETER))
            val recipientOf = mutableMapOf<Long, Boolean>>()

            for (recipient in recipientsJson) {
                val recipientId = BigInteger(JSON.getAsString(recipient)).toLong()
                if (recipientOf.containsKey(recipientId))
                    throw BurstException.NotValidException("Duplicate recipient on multi same out transaction")

                recipientOf[recipientId] = true
                this.recipients.add(recipientId)
            }
            if (recipientsJson.size() > Constants.MAX_MULTI_SAME_OUT_RECIPIENTS || recipientsJson.size() <= 1) {
                throw BurstException.NotValidException(
                        "Invalid number of recipients listed on multi same out transaction")
            }
        }

        @Throws(BurstException.NotValidException::class)
        constructor(recipients: Collection<Long>, blockchainHeight: Int) : super(blockchainHeight) {

            val recipientOf = mutableMapOf<Long, Boolean>>()
            for (recipientId in recipients) {
                if (recipientOf.containsKey(recipientId))
                    throw BurstException.NotValidException("Duplicate recipient on multi same out transaction")

                recipientOf[recipientId] = true
                this.recipients.add(recipientId)
            }
            if (recipients.size > Constants.MAX_MULTI_SAME_OUT_RECIPIENTS || recipients.size <= 1) {
                throw BurstException.NotValidException(
                        "Invalid number of recipients listed on multi same out transaction")
            }
        }

        @Throws(BurstException.NotValidException::class)
        internal constructor(attachment: BrsApi.MultiOutSameAttachment) : super(attachment.version.toByte()) {
            val recipientOf = mutableMapOf<Long, Boolean>>()
            for (recipientId in attachment.recipientsList) {
                if (recipientOf.containsKey(recipientId))
                    throw BurstException.NotValidException("Duplicate recipient on multi same out transaction")

                recipientOf[recipientId] = true
                this.recipients.add(recipientId)
            }
            if (recipients.size > Constants.MAX_MULTI_SAME_OUT_RECIPIENTS || recipients.size <= 1) {
                throw BurstException.NotValidException(
                        "Invalid number of recipients listed on multi same out transaction")
            }
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.put(this.recipients.size.toByte())
            this.recipients.forEach { buffer.putLong(it) }
        }

        override fun putMyJSON(attachment: JsonObject) {
            val recipients = JsonArray()
            this.recipients.forEach { a -> recipients.add(Convert.toUnsignedLong(a)) }
            attachment.add(RECIPIENTS_RESPONSE, recipients)
        }

        fun getRecipients(): Collection<Long> {
            return Collections.unmodifiableCollection(recipients)
        }
    }

    class MessagingAliasAssignment : AbstractAttachment {

        val aliasName: String
        val aliasURI: String

        override val appendixName: String
            get() = "AliasAssignment"

        override val mySize: Int
            get() = 1 + Convert.toBytes(aliasName).size + 2 + Convert.toBytes(aliasURI).size

        override val transactionType: TransactionType
            get() = TransactionType.Messaging.ALIAS_ASSIGNMENT

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.AliasAssignmentAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setName(aliasName)
                    .setUri(aliasURI)
                    .build())

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            aliasName = Convert.readString(buffer, buffer.get().toInt(), Constants.MAX_ALIAS_LENGTH).trim { it <= ' ' }
            aliasURI = Convert.readString(buffer, buffer.short.toInt(), Constants.MAX_ALIAS_URI_LENGTH).trim { it <= ' ' }
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            aliasName = Convert.nullToEmpty(JSON.getAsString(attachmentData.get(ALIAS_PARAMETER))).trim { it <= ' ' }
            aliasURI = Convert.nullToEmpty(JSON.getAsString(attachmentData.get(URI_PARAMETER))).trim { it <= ' ' }
        }

        constructor(aliasName: String, aliasURI: String, blockchainHeight: Int) : super(blockchainHeight) {
            this.aliasName = aliasName.trim { it <= ' ' }
            this.aliasURI = aliasURI.trim { it <= ' ' }
        }

        internal constructor(attachment: BrsApi.AliasAssignmentAttachment) : super(attachment.version.toByte()) {
            this.aliasName = attachment.name
            this.aliasURI = attachment.uri
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            val alias = Convert.toBytes(this.aliasName)
            val uri = Convert.toBytes(this.aliasURI)
            buffer.put(alias.size.toByte())
            buffer.put(alias)
            buffer.putShort(uri.size.toShort())
            buffer.put(uri)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(ALIAS_RESPONSE, aliasName)
            attachment.addProperty(URI_RESPONSE, aliasURI)
        }
    }

    class MessagingAliasSell : AbstractAttachment {

        val aliasName: String
        val priceNQT: Long

        override val appendixName: String
            get() = "AliasSell"

        override val transactionType: TransactionType
            get() = TransactionType.Messaging.ALIAS_SELL

        override val mySize: Int
            get() = 1 + Convert.toBytes(aliasName).size + 8

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.AliasSellAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setName(aliasName)
                    .setPrice(priceNQT)
                    .build())

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.aliasName = Convert.readString(buffer, buffer.get().toInt(), Constants.MAX_ALIAS_LENGTH)
            this.priceNQT = buffer.long
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.aliasName = Convert.nullToEmpty(JSON.getAsString(attachmentData.get(ALIAS_PARAMETER)))
            this.priceNQT = JSON.getAsLong(attachmentData.get(PRICE_NQT_PARAMETER))
        }

        constructor(aliasName: String, priceNQT: Long, blockchainHeight: Int) : super(blockchainHeight) {
            this.aliasName = aliasName
            this.priceNQT = priceNQT
        }

        internal constructor(attachment: BrsApi.AliasSellAttachment) : super(attachment.version.toByte()) {
            this.aliasName = attachment.name
            this.priceNQT = attachment.price
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            val aliasBytes = Convert.toBytes(aliasName)
            buffer.put(aliasBytes.size.toByte())
            buffer.put(aliasBytes)
            buffer.putLong(priceNQT)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(ALIAS_RESPONSE, aliasName)
            attachment.addProperty(PRICE_NQT_RESPONSE, priceNQT)
        }
    }

    class MessagingAliasBuy : AbstractAttachment {

        val aliasName: String

        override val appendixName: String
            get() = "AliasBuy"

        override val transactionType: TransactionType
            get() = TransactionType.Messaging.ALIAS_BUY

        override val mySize: Int
            get() = 1 + Convert.toBytes(aliasName).size

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.AliasBuyAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setName(aliasName)
                    .build())

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.aliasName = Convert.readString(buffer, buffer.get().toInt(), Constants.MAX_ALIAS_LENGTH)
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.aliasName = Convert.nullToEmpty(JSON.getAsString(attachmentData.get(ALIAS_PARAMETER)))
        }

        constructor(aliasName: String, blockchainHeight: Int) : super(blockchainHeight) {
            this.aliasName = aliasName
        }

        internal constructor(attachment: BrsApi.AliasBuyAttachment) : super(attachment.version.toByte()) {
            this.aliasName = attachment.name
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            val aliasBytes = Convert.toBytes(aliasName)
            buffer.put(aliasBytes.size.toByte())
            buffer.put(aliasBytes)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(ALIAS_RESPONSE, aliasName)
        }
    }

    class MessagingAccountInfo : AbstractAttachment {

        val name: String
        val description: String

        override val appendixName: String
            get() = "AccountInfo"

        override val mySize: Int
            get() = 1 + Convert.toBytes(name).size + 2 + Convert.toBytes(description).size

        override val transactionType: TransactionType
            get() = TransactionType.Messaging.ACCOUNT_INFO

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.AccountInfoAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setName(name)
                    .setDescription(description)
                    .build())

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.name = Convert.readString(buffer, buffer.get().toInt(), Constants.MAX_ACCOUNT_NAME_LENGTH)
            this.description = Convert.readString(buffer, buffer.short.toInt(), Constants.MAX_ACCOUNT_DESCRIPTION_LENGTH)
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.name = Convert.nullToEmpty(JSON.getAsString(attachmentData.get(NAME_PARAMETER)))
            this.description = Convert.nullToEmpty(JSON.getAsString(attachmentData.get(DESCRIPTION_PARAMETER)))
        }

        constructor(name: String, description: String, blockchainHeight: Int) : super(blockchainHeight) {
            this.name = name
            this.description = description
        }

        internal constructor(attachment: BrsApi.AccountInfoAttachment) : super(attachment.version.toByte()) {
            this.name = attachment.name
            this.description = attachment.description
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            val putName = Convert.toBytes(this.name)
            val putDescription = Convert.toBytes(this.description)
            buffer.put(putName.size.toByte())
            buffer.put(putName)
            buffer.putShort(putDescription.size.toShort())
            buffer.put(putDescription)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(NAME_RESPONSE, name)
            attachment.addProperty(DESCRIPTION_RESPONSE, description)
        }
    }

    class ColoredCoinsAssetIssuance : AbstractAttachment {

        val name: String?
        val description: String
        val quantityQNT: Long
        val decimals: Byte

        override val appendixName: String
            get() = "AssetIssuance"

        override val mySize: Int
            get() = 1 + Convert.toBytes(name).size + 2 + Convert.toBytes(description).size + 8 + 1

        override val transactionType: TransactionType
            get() = TransactionType.ColoredCoins.ASSET_ISSUANCE

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.AssetIssuanceAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setName(name)
                    .setDescription(description)
                    .setQuantity(quantityQNT)
                    .setDecimals(decimals.toInt())
                    .build())

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.name = Convert.readString(buffer, buffer.get().toInt(), Constants.MAX_ASSET_NAME_LENGTH)
            this.description = Convert.readString(buffer, buffer.short.toInt(), Constants.MAX_ASSET_DESCRIPTION_LENGTH)
            this.quantityQNT = buffer.long
            this.decimals = buffer.get()
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.name = JSON.getAsString(attachmentData.get(NAME_PARAMETER))
            this.description = Convert.nullToEmpty(JSON.getAsString(attachmentData.get(DESCRIPTION_PARAMETER)))
            this.quantityQNT = JSON.getAsLong(attachmentData.get(QUANTITY_QNT_PARAMETER))
            this.decimals = JSON.getAsByte(attachmentData.get(DECIMALS_PARAMETER))
        }

        constructor(name: String, description: String, quantityQNT: Long, decimals: Byte, blockchainHeight: Int) : super(blockchainHeight) {
            this.name = name
            this.description = Convert.nullToEmpty(description)
            this.quantityQNT = quantityQNT
            this.decimals = decimals
        }

        internal constructor(attachment: BrsApi.AssetIssuanceAttachment) : super(attachment.version.toByte()) {
            this.name = attachment.name
            this.description = attachment.description
            this.quantityQNT = attachment.quantity
            this.decimals = attachment.decimals.toByte()
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            val name = Convert.toBytes(this.name)
            val description = Convert.toBytes(this.description)
            buffer.put(name.size.toByte())
            buffer.put(name)
            buffer.putShort(description.size.toShort())
            buffer.put(description)
            buffer.putLong(quantityQNT)
            buffer.put(decimals)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(NAME_RESPONSE, name)
            attachment.addProperty(DESCRIPTION_RESPONSE, description)
            attachment.addProperty(QUANTITY_QNT_RESPONSE, quantityQNT)
            attachment.addProperty(DECIMALS_RESPONSE, decimals)
        }
    }

    class ColoredCoinsAssetTransfer : AbstractAttachment {

        val assetId: Long
        val quantityQNT: Long
        val comment: String?

        override val appendixName: String
            get() = "AssetTransfer"

        override val mySize: Int
            get() = 8 + 8 + if (version.toInt() == 0) 2 + Convert.toBytes(comment).size else 0

        override val transactionType: TransactionType
            get() = TransactionType.ColoredCoins.ASSET_TRANSFER

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.AssetTransferAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setAsset(assetId)
                    .setQuantity(quantityQNT)
                    .setComment(comment)
                    .build())

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.assetId = buffer.long
            this.quantityQNT = buffer.long
            this.comment = if (version.toInt() == 0) Convert.readString(buffer, buffer.short.toInt(), Constants.MAX_ASSET_TRANSFER_COMMENT_LENGTH) else null
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.assetId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(ASSET_PARAMETER)))
            this.quantityQNT = JSON.getAsLong(attachmentData.get(QUANTITY_QNT_PARAMETER))
            this.comment = if (version.toInt() == 0) Convert.nullToEmpty(JSON.getAsString(attachmentData.get(COMMENT_PARAMETER))) else null
        }

        constructor(assetId: Long, quantityQNT: Long, blockchainHeight: Int) : super(blockchainHeight) {
            this.assetId = assetId
            this.quantityQNT = quantityQNT
            this.comment = null
        }

        internal constructor(attachment: BrsApi.AssetTransferAttachment) : super(attachment.version.toByte()) {
            this.assetId = attachment.asset
            this.quantityQNT = attachment.quantity
            this.comment = attachment.comment
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(assetId)
            buffer.putLong(quantityQNT)
            if (version.toInt() == 0 && comment != null) {
                val commentBytes = Convert.toBytes(this.comment)
                buffer.putShort(commentBytes.size.toShort())
                buffer.put(commentBytes)
            }
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(assetId))
            attachment.addProperty(QUANTITY_QNT_RESPONSE, quantityQNT)
            if (version.toInt() == 0) {
                attachment.addProperty(COMMENT_RESPONSE, comment)
            }
        }
    }

    abstract class ColoredCoinsOrderPlacement : AbstractAttachment {

        val assetId: Long
        val quantityQNT: Long
        val priceNQT: Long

        override val mySize: Int
            get() = 8 + 8 + 8

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.AssetOrderPlacementAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setAsset(assetId)
                    .setQuantity(quantityQNT)
                    .setPrice(priceNQT)
                    .setType(type)
                    .build())

        protected abstract val type: BrsApi.OrderType

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.assetId = buffer.long
            this.quantityQNT = buffer.long
            this.priceNQT = buffer.long
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.assetId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(ASSET_PARAMETER)))
            this.quantityQNT = JSON.getAsLong(attachmentData.get(QUANTITY_QNT_PARAMETER))
            this.priceNQT = JSON.getAsLong(attachmentData.get(PRICE_NQT_PARAMETER))
        }

        constructor(assetId: Long, quantityQNT: Long, priceNQT: Long, blockchainHeight: Int) : super(blockchainHeight) {
            this.assetId = assetId
            this.quantityQNT = quantityQNT
            this.priceNQT = priceNQT
        }

        internal constructor(attachment: BrsApi.AssetOrderPlacementAttachment) : super(attachment.version.toByte()) {
            this.assetId = attachment.asset
            this.quantityQNT = attachment.quantity
            this.priceNQT = attachment.price
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(assetId)
            buffer.putLong(quantityQNT)
            buffer.putLong(priceNQT)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(assetId))
            attachment.addProperty(QUANTITY_QNT_RESPONSE, quantityQNT)
            attachment.addProperty(PRICE_NQT_RESPONSE, priceNQT)
        }
    }

    class ColoredCoinsAskOrderPlacement : ColoredCoinsOrderPlacement {

        override val type: BrsApi.OrderType
            get() = BrsApi.OrderType.ASK

        override val appendixName: String
            get() = "AskOrderPlacement"

        override val transactionType: TransactionType
            get() = TransactionType.ColoredCoins.ASK_ORDER_PLACEMENT

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion)

        internal constructor(attachmentData: JsonObject) : super(attachmentData)

        constructor(assetId: Long, quantityQNT: Long, priceNQT: Long, blockchainHeight: Int) : super(assetId, quantityQNT, priceNQT, blockchainHeight)

        internal constructor(attachment: BrsApi.AssetOrderPlacementAttachment) : super(attachment) {
            if (attachment.type != type) throw IllegalArgumentException("Type does not match")
        }

    }

    class ColoredCoinsBidOrderPlacement : ColoredCoinsOrderPlacement {

        override val type: BrsApi.OrderType
            get() = BrsApi.OrderType.BID

        override val appendixName: String
            get() = "BidOrderPlacement"

        override val transactionType: TransactionType
            get() = TransactionType.ColoredCoins.BID_ORDER_PLACEMENT

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion)

        internal constructor(attachmentData: JsonObject) : super(attachmentData)

        constructor(assetId: Long, quantityQNT: Long, priceNQT: Long, blockchainHeight: Int) : super(assetId, quantityQNT, priceNQT, blockchainHeight)

        internal constructor(attachment: BrsApi.AssetOrderPlacementAttachment) : super(attachment) {
            if (attachment.type != type) throw IllegalArgumentException("Type does not match")
        }

    }

    abstract class ColoredCoinsOrderCancellation : AbstractAttachment {

        val orderId: Long

        override val mySize: Int
            get() = 8

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.AssetOrderCancellationAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setOrder(orderId)
                    .setType(type)
                    .build())

        protected abstract val type: BrsApi.OrderType

        constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.orderId = buffer.long
        }

        constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.orderId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(ORDER_PARAMETER)))
        }

        constructor(orderId: Long, blockchainHeight: Int) : super(blockchainHeight) {
            this.orderId = orderId
        }

        constructor(attachment: BrsApi.AssetOrderCancellationAttachment) : super(attachment.version.toByte()) {
            this.orderId = attachment.order
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(orderId)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(ORDER_RESPONSE, Convert.toUnsignedLong(orderId))
        }
    }

    class ColoredCoinsAskOrderCancellation : ColoredCoinsOrderCancellation {

        override val type: BrsApi.OrderType
            get() = BrsApi.OrderType.ASK

        override val appendixName: String
            get() = "AskOrderCancellation"

        override val transactionType: TransactionType
            get() = TransactionType.ColoredCoins.ASK_ORDER_CANCELLATION

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion)

        internal constructor(attachmentData: JsonObject) : super(attachmentData)

        constructor(orderId: Long, blockchainHeight: Int) : super(orderId, blockchainHeight)

        internal constructor(attachment: BrsApi.AssetOrderCancellationAttachment) : super(attachment) {
            if (attachment.type != type) throw IllegalArgumentException("Type does not match")
        }

    }

    class ColoredCoinsBidOrderCancellation : ColoredCoinsOrderCancellation {

        override val type: BrsApi.OrderType
            get() = BrsApi.OrderType.BID

        override val appendixName: String
            get() = "BidOrderCancellation"

        override val transactionType: TransactionType
            get() = TransactionType.ColoredCoins.BID_ORDER_CANCELLATION

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion)

        internal constructor(attachmentData: JsonObject) : super(attachmentData)

        constructor(orderId: Long, blockchainHeight: Int) : super(orderId, blockchainHeight)

        internal constructor(attachment: BrsApi.AssetOrderCancellationAttachment) : super(attachment) {
            if (attachment.type != type) throw IllegalArgumentException("Type does not match")
        }

    }

    class DigitalGoodsListing : AbstractAttachment {

        val name: String?
        val description: String?
        val tags: String?
        val quantity: Int
        val priceNQT: Long

        override val appendixName: String
            get() = "DigitalGoodsListing"

        override val mySize: Int
            get() = (2 + Convert.toBytes(name).size + 2 + Convert.toBytes(description).size + 2
                    + Convert.toBytes(tags).size + 4 + 8)

        override val transactionType: TransactionType
            get() = TransactionType.DigitalGoods.LISTING

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.DigitalGoodsListingAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setName(name)
                    .setDescription(description)
                    .setTags(tags)
                    .setQuantity(quantity)
                    .setPrice(priceNQT)
                    .build())

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.name = Convert.readString(buffer, buffer.short.toInt(), Constants.MAX_DGS_LISTING_NAME_LENGTH)
            this.description = Convert.readString(buffer, buffer.short.toInt(), Constants.MAX_DGS_LISTING_DESCRIPTION_LENGTH)
            this.tags = Convert.readString(buffer, buffer.short.toInt(), Constants.MAX_DGS_LISTING_TAGS_LENGTH)
            this.quantity = buffer.int
            this.priceNQT = buffer.long
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.name = JSON.getAsString(attachmentData.get(NAME_RESPONSE))
            this.description = JSON.getAsString(attachmentData.get(DESCRIPTION_RESPONSE))
            this.tags = JSON.getAsString(attachmentData.get(TAGS_RESPONSE))
            this.quantity = JSON.getAsInt(attachmentData.get(QUANTITY_RESPONSE))
            this.priceNQT = JSON.getAsLong(attachmentData.get(PRICE_NQT_PARAMETER))
        }

        constructor(name: String, description: String, tags: String, quantity: Int, priceNQT: Long, blockchainHeight: Int) : super(blockchainHeight) {
            this.name = name
            this.description = description
            this.tags = tags
            this.quantity = quantity
            this.priceNQT = priceNQT
        }

        internal constructor(attachment: BrsApi.DigitalGoodsListingAttachment) : super(attachment.version.toByte()) {
            this.name = attachment.name
            this.description = attachment.description
            this.tags = attachment.tags
            this.quantity = attachment.quantity
            this.priceNQT = attachment.price
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            val nameBytes = Convert.toBytes(name)
            buffer.putShort(nameBytes.size.toShort())
            buffer.put(nameBytes)
            val descriptionBytes = Convert.toBytes(description)
            buffer.putShort(descriptionBytes.size.toShort())
            buffer.put(descriptionBytes)
            val tagsBytes = Convert.toBytes(tags)
            buffer.putShort(tagsBytes.size.toShort())
            buffer.put(tagsBytes)
            buffer.putInt(quantity)
            buffer.putLong(priceNQT)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(NAME_RESPONSE, name)
            attachment.addProperty(DESCRIPTION_RESPONSE, description)
            attachment.addProperty(TAGS_RESPONSE, tags)
            attachment.addProperty(QUANTITY_RESPONSE, quantity)
            attachment.addProperty(PRICE_NQT_RESPONSE, priceNQT)
        }
    }

    class DigitalGoodsDelisting : AbstractAttachment {

        val goodsId: Long

        override val appendixName: String
            get() = "DigitalGoodsDelisting"

        override val mySize: Int
            get() = 8

        override val transactionType: TransactionType
            get() = TransactionType.DigitalGoods.DELISTING

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.DigitalGoodsDelistingAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setGoods(goodsId)
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.goodsId = buffer.long
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.goodsId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(GOODS_PARAMETER)))
        }

        constructor(goodsId: Long, blockchainHeight: Int) : super(blockchainHeight) {
            this.goodsId = goodsId
        }

        internal constructor(attachment: BrsApi.DigitalGoodsDelistingAttachment) : super(attachment.version.toByte()) {
            this.goodsId = attachment.goods
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(goodsId)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(GOODS_RESPONSE, Convert.toUnsignedLong(goodsId))
        }
    }

    class DigitalGoodsPriceChange : AbstractAttachment {

        val goodsId: Long
        val priceNQT: Long

        override val appendixName: String
            get() = "DigitalGoodsPriceChange"

        override val mySize: Int
            get() = 8 + 8

        override val transactionType: TransactionType
            get() = TransactionType.DigitalGoods.PRICE_CHANGE

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.DigitalGoodsPriceChangeAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setGoods(goodsId)
                    .setPrice(priceNQT)
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.goodsId = buffer.long
            this.priceNQT = buffer.long
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.goodsId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(GOODS_PARAMETER)))
            this.priceNQT = JSON.getAsLong(attachmentData.get(PRICE_NQT_PARAMETER))
        }

        constructor(goodsId: Long, priceNQT: Long, blockchainHeight: Int) : super(blockchainHeight) {
            this.goodsId = goodsId
            this.priceNQT = priceNQT
        }

        internal constructor(attachment: BrsApi.DigitalGoodsPriceChangeAttachment) : super(attachment.version.toByte()) {
            this.goodsId = attachment.goods
            this.priceNQT = attachment.price
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(goodsId)
            buffer.putLong(priceNQT)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(GOODS_RESPONSE, Convert.toUnsignedLong(goodsId))
            attachment.addProperty(PRICE_NQT_RESPONSE, priceNQT)
        }
    }

    class DigitalGoodsQuantityChange : AbstractAttachment {

        val goodsId: Long
        val deltaQuantity: Int

        override val appendixName: String
            get() = "DigitalGoodsQuantityChange"

        override val mySize: Int
            get() = 8 + 4

        override val transactionType: TransactionType
            get() = TransactionType.DigitalGoods.QUANTITY_CHANGE

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.DigitalGoodsQuantityChangeAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setGoods(goodsId)
                    .setDeltaQuantity(deltaQuantity)
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.goodsId = buffer.long
            this.deltaQuantity = buffer.int
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.goodsId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(GOODS_PARAMETER)))
            this.deltaQuantity = JSON.getAsInt(attachmentData.get(DELTA_QUANTITY_PARAMETER))
        }

        constructor(goodsId: Long, deltaQuantity: Int, blockchainHeight: Int) : super(blockchainHeight) {
            this.goodsId = goodsId
            this.deltaQuantity = deltaQuantity
        }

        internal constructor(attachment: BrsApi.DigitalGoodsQuantityChangeAttachment) : super(attachment.version.toByte()) {
            this.goodsId = attachment.goods
            this.deltaQuantity = attachment.deltaQuantity
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(goodsId)
            buffer.putInt(deltaQuantity)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(GOODS_RESPONSE, Convert.toUnsignedLong(goodsId))
            attachment.addProperty(DELTA_QUANTITY_RESPONSE, deltaQuantity)
        }
    }

    class DigitalGoodsPurchase : AbstractAttachment {

        val goodsId: Long
        val quantity: Int
        val priceNQT: Long
        val deliveryDeadlineTimestamp: Int

        override val appendixName: String
            get() = "DigitalGoodsPurchase"

        override val mySize: Int
            get() = 8 + 4 + 8 + 4

        override val transactionType: TransactionType
            get() = TransactionType.DigitalGoods.PURCHASE

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.DigitalGoodsPurchaseAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setGoods(goodsId)
                    .setQuantity(quantity)
                    .setPrice(priceNQT)
                    .setDeliveryDeadlineTimestmap(deliveryDeadlineTimestamp)
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.goodsId = buffer.long
            this.quantity = buffer.int
            this.priceNQT = buffer.long
            this.deliveryDeadlineTimestamp = buffer.int
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.goodsId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(GOODS_PARAMETER)))
            this.quantity = JSON.getAsInt(attachmentData.get(QUANTITY_PARAMETER))
            this.priceNQT = JSON.getAsLong(attachmentData.get(PRICE_NQT_PARAMETER))
            this.deliveryDeadlineTimestamp = JSON.getAsInt(attachmentData.get(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER))
        }

        constructor(goodsId: Long, quantity: Int, priceNQT: Long, deliveryDeadlineTimestamp: Int, blockchainHeight: Int) : super(blockchainHeight) {
            this.goodsId = goodsId
            this.quantity = quantity
            this.priceNQT = priceNQT
            this.deliveryDeadlineTimestamp = deliveryDeadlineTimestamp
        }

        internal constructor(attachment: BrsApi.DigitalGoodsPurchaseAttachment) : super(attachment.version.toByte()) {
            this.goodsId = attachment.goods
            this.quantity = attachment.quantity
            this.priceNQT = attachment.price
            this.deliveryDeadlineTimestamp = attachment.deliveryDeadlineTimestmap
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(goodsId)
            buffer.putInt(quantity)
            buffer.putLong(priceNQT)
            buffer.putInt(deliveryDeadlineTimestamp)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(GOODS_RESPONSE, Convert.toUnsignedLong(goodsId))
            attachment.addProperty(QUANTITY_RESPONSE, quantity)
            attachment.addProperty(PRICE_NQT_RESPONSE, priceNQT)
            attachment.addProperty(DELIVERY_DEADLINE_TIMESTAMP_RESPONSE, deliveryDeadlineTimestamp)
        }
    }

    class DigitalGoodsDelivery : AbstractAttachment {

        val purchaseId: Long
        val goods: EncryptedData
        val discountNQT: Long
        private val goodsIsText: Boolean

        override val appendixName: String
            get() = "DigitalGoodsDelivery"

        override val mySize: Int
            get() = 8 + 4 + goods.size + 8

        override val transactionType: TransactionType
            get() = TransactionType.DigitalGoods.DELIVERY

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.DigitalGoodsDeliveryAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setPurchase(purchaseId)
                    .setDiscount(discountNQT)
                    .setGoods(ProtoBuilder.buildEncryptedData(goods))
                    .setIsText(goodsIsText)
                    .build())

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.purchaseId = buffer.long
            var length = buffer.int
            goodsIsText = length < 0
            if (length < 0) {
                length = length and Integer.MAX_VALUE
            }
            this.goods = EncryptedData.readEncryptedData(buffer, length, Constants.MAX_DGS_GOODS_LENGTH)
            this.discountNQT = buffer.long
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.purchaseId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(PURCHASE_PARAMETER)))
            this.goods = EncryptedData(Convert.parseHexString(JSON.getAsString(attachmentData.get(GOODS_DATA_PARAMETER))),
                    Convert.parseHexString(JSON.getAsString(attachmentData.get(GOODS_NONCE_PARAMETER))))
            this.discountNQT = JSON.getAsLong(attachmentData.get(DISCOUNT_NQT_PARAMETER))
            this.goodsIsText = java.lang.Boolean.TRUE == JSON.getAsBoolean(attachmentData.get(GOODS_IS_TEXT_PARAMETER))
        }

        constructor(purchaseId: Long, goods: EncryptedData, goodsIsText: Boolean, discountNQT: Long, blockchainHeight: Int) : super(blockchainHeight) {
            this.purchaseId = purchaseId
            this.goods = goods
            this.discountNQT = discountNQT
            this.goodsIsText = goodsIsText
        }

        internal constructor(attachment: BrsApi.DigitalGoodsDeliveryAttachment) : super(attachment.version.toByte()) {
            this.purchaseId = attachment.purchase
            this.goods = ProtoBuilder.parseEncryptedData(attachment.goods)
            this.goodsIsText = attachment.isText
            this.discountNQT = attachment.discount
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(purchaseId)
            buffer.putInt(if (goodsIsText) goods.data.size or Integer.MIN_VALUE else goods.data.size)
            buffer.put(goods.data)
            buffer.put(goods.nonce)
            buffer.putLong(discountNQT)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(PURCHASE_RESPONSE, Convert.toUnsignedLong(purchaseId))
            attachment.addProperty(GOODS_DATA_RESPONSE, Convert.toHexString(goods.data))
            attachment.addProperty(GOODS_NONCE_RESPONSE, Convert.toHexString(goods.nonce))
            attachment.addProperty(DISCOUNT_NQT_RESPONSE, discountNQT)
            attachment.addProperty(GOODS_IS_TEXT_RESPONSE, goodsIsText)
        }

        fun goodsIsText(): Boolean {
            return goodsIsText
        }
    }

    class DigitalGoodsFeedback : AbstractAttachment {

        val purchaseId: Long

        override val appendixName: String
            get() = "DigitalGoodsFeedback"

        override val mySize: Int
            get() = 8

        override val transactionType: TransactionType
            get() = TransactionType.DigitalGoods.FEEDBACK

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.DigitalGoodsFeedbackAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setPurchase(purchaseId)
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.purchaseId = buffer.long
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.purchaseId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(PURCHASE_PARAMETER)))
        }

        constructor(purchaseId: Long, blockchainHeight: Int) : super(blockchainHeight) {
            this.purchaseId = purchaseId
        }

        internal constructor(attachment: BrsApi.DigitalGoodsFeedbackAttachment) : super(attachment.version.toByte()) {
            this.purchaseId = attachment.purchase
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(purchaseId)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(PURCHASE_RESPONSE, Convert.toUnsignedLong(purchaseId))
        }
    }

    class DigitalGoodsRefund : AbstractAttachment {

        val purchaseId: Long
        val refundNQT: Long

        override val appendixName: String
            get() = "DigitalGoodsRefund"

        override val mySize: Int
            get() = 8 + 8

        override val transactionType: TransactionType
            get() = TransactionType.DigitalGoods.REFUND

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.DigitalGoodsRefundAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setPurchase(purchaseId)
                    .setRefund(refundNQT)
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.purchaseId = buffer.long
            this.refundNQT = buffer.long
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.purchaseId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(PURCHASE_PARAMETER)))
            this.refundNQT = JSON.getAsLong(attachmentData.get(REFUND_NQT_PARAMETER))
        }

        constructor(purchaseId: Long, refundNQT: Long, blockchainHeight: Int) : super(blockchainHeight) {
            this.purchaseId = purchaseId
            this.refundNQT = refundNQT
        }

        internal constructor(attachment: BrsApi.DigitalGoodsRefundAttachment) : super(attachment.version.toByte()) {
            this.purchaseId = attachment.purchase
            this.refundNQT = attachment.refund
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(purchaseId)
            buffer.putLong(refundNQT)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(PURCHASE_RESPONSE, Convert.toUnsignedLong(purchaseId))
            attachment.addProperty(REFUND_NQT_RESPONSE, refundNQT)
        }
    }

    class AccountControlEffectiveBalanceLeasing : AbstractAttachment {

        val period: Short

        override val appendixName: String
            get() = "EffectiveBalanceLeasing"

        override val mySize: Int
            get() = 2

        override val transactionType: TransactionType
            get() = TransactionType.AccountControl.EFFECTIVE_BALANCE_LEASING

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.EffectiveBalanceLeasingAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setPeriod(period.toInt())
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.period = buffer.short
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.period = JSON.getAsShort(attachmentData.get(PERIOD_PARAMETER))
        }

        constructor(period: Short, blockchainHeight: Int) : super(blockchainHeight) {
            this.period = period
        }

        internal constructor(attachment: BrsApi.EffectiveBalanceLeasingAttachment) : super(attachment.version.toByte()) {
            this.period = attachment.period.toShort()
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putShort(period)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(PERIOD_RESPONSE, period)
        }
    }

    class BurstMiningRewardRecipientAssignment : AbstractAttachment {

        override val appendixName: String
            get() = "RewardRecipientAssignment"

        override val mySize: Int
            get() = 0

        override val transactionType: TransactionType
            get() = TransactionType.BurstMining.REWARD_RECIPIENT_ASSIGNMENT

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.RewardRecipientAssignmentAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion)

        internal constructor(attachmentData: JsonObject) : super(attachmentData)

        constructor(blockchainHeight: Int) : super(blockchainHeight)

        internal constructor(attachment: BrsApi.RewardRecipientAssignmentAttachment) : super(attachment.version.toByte())

        override fun putMyBytes(buffer: ByteBuffer) {
            // Reward recipient does not have additional data.
        }

        override fun putMyJSON(attachment: JsonObject) {
            // Reward recipient does not have additional data.
        }
    }

    class AdvancedPaymentEscrowCreation : AbstractAttachment {

        val amountNQT: Long?
        private val requiredSigners: Byte
        private val signers = TreeSet<Long>()
        val deadline: Int
        val deadlineAction: Escrow.DecisionType?

        override val appendixName: String
            get() = "EscrowCreation"

        override val mySize: Int
            get() {
                var size = 8 + 4 + 1 + 1 + 1
                size += signers.size * 8
                return size
            }

        override val transactionType: TransactionType
            get() = TransactionType.AdvancedPayment.ESCROW_CREATION

        val totalSigners: Int
            get() = signers.size

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.EscrowCreationAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setAmount(amountNQT!!)
                    .setRequiredSigners(requiredSigners.toInt())
                    .addAllSigners(signers)
                    .setDeadline(deadline)
                    .setDeadlineAction(Escrow.decisionToProtobuf(deadlineAction!!))
                    .build())

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.amountNQT = buffer.long
            this.deadline = buffer.int
            this.deadlineAction = Escrow.byteToDecision(buffer.get())
            this.requiredSigners = buffer.get()
            val totalSigners = buffer.get()
            if (totalSigners > 10 || totalSigners <= 0) {
                throw BurstException.NotValidException("Invalid number of signers listed on create escrow transaction")
            }
            for (i in 0 until totalSigners) {
                if (!this.signers.add(buffer.long)) {
                    throw BurstException.NotValidException("Duplicate signer on escrow creation")
                }
            }
        }

        @Throws(BurstException.NotValidException::class)
        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.amountNQT = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(AMOUNT_NQT_PARAMETER)))
            this.deadline = JSON.getAsInt(attachmentData.get(DEADLINE_PARAMETER))
            this.deadlineAction = Escrow.stringToDecision(JSON.getAsString(attachmentData.get(DEADLINE_ACTION_PARAMETER))!!)
            this.requiredSigners = JSON.getAsByte(attachmentData.get(REQUIRED_SIGNERS_PARAMETER))
            val totalSigners = JSON.getAsJsonArray(attachmentData.get(SIGNERS_PARAMETER)).size()
            if (totalSigners > 10 || totalSigners <= 0) {
                throw BurstException.NotValidException("Invalid number of signers listed on create escrow transaction")
            }
            val signersJson = JSON.getAsJsonArray(attachmentData.get(SIGNERS_PARAMETER))
            for (aSignersJson in signersJson) {
                this.signers.add(Convert.parseUnsignedLong(JSON.getAsString(aSignersJson)))
            }
            if (this.signers.size != JSON.getAsJsonArray(attachmentData.get(SIGNERS_PARAMETER)).size()) {
                throw BurstException.NotValidException("Duplicate signer on escrow creation")
            }
        }

        @Throws(BurstException.NotValidException::class)
        constructor(amountNQT: Long?, deadline: Int, deadlineAction: Escrow.DecisionType,
                    requiredSigners: Int, signers: Collection<Long>, blockchainHeight: Int) : super(blockchainHeight) {
            this.amountNQT = amountNQT
            this.deadline = deadline
            this.deadlineAction = deadlineAction
            this.requiredSigners = requiredSigners.toByte()
            if (signers.size > 10 || signers.isEmpty()) {
                throw BurstException.NotValidException("Invalid number of signers listed on create escrow transaction")
            }
            this.signers.addAll(signers)
            if (this.signers.size != signers.size) {
                throw BurstException.NotValidException("Duplicate signer on escrow creation")
            }
        }

        @Throws(BurstException.NotValidException::class)
        internal constructor(attachment: BrsApi.EscrowCreationAttachment) : super(attachment.version.toByte()) {
            this.amountNQT = attachment.amount
            this.requiredSigners = attachment.requiredSigners.toByte()
            this.deadline = attachment.deadline
            this.deadlineAction = Escrow.protoBufToDecision(attachment.deadlineAction)
            this.signers.addAll(attachment.signersList)
            if (signers.size > 10 || signers.isEmpty()) {
                throw BurstException.NotValidException("Invalid number of signers listed on create escrow transaction")
            }
            if (this.signers.size != attachment.signersList.size) {
                throw BurstException.NotValidException("Duplicate signer on escrow creation")
            }
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(this.amountNQT!!)
            buffer.putInt(this.deadline)
            buffer.put(Escrow.decisionToByte(this.deadlineAction!!)!!)
            buffer.put(this.requiredSigners)
            val totalSigners = this.signers.size.toByte()
            buffer.put(totalSigners)
            this.signers.forEach { buffer.putLong(it) }
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(AMOUNT_NQT_RESPONSE, Convert.toUnsignedLong(this.amountNQT!!))
            attachment.addProperty(DEADLINE_RESPONSE, this.deadline)
            attachment.addProperty(DEADLINE_ACTION_RESPONSE, Escrow.decisionToString(this.deadlineAction!!))
            attachment.addProperty(REQUIRED_SIGNERS_RESPONSE, this.requiredSigners.toInt())
            val ids = JsonArray()
            for (signer in this.signers) {
                ids.add(Convert.toUnsignedLong(signer))
            }
            attachment.add(SIGNERS_RESPONSE, ids)
        }

        fun getRequiredSigners(): Int {
            return requiredSigners.toInt()
        }

        fun getSigners(): Collection<Long> {
            return Collections.unmodifiableCollection(signers)
        }
    }

    class AdvancedPaymentEscrowSign : AbstractAttachment {

        val escrowId: Long?
        val decision: Escrow.DecisionType?

        override val appendixName: String
            get() = "EscrowSign"

        override val mySize: Int
            get() = 8 + 1

        override val transactionType: TransactionType
            get() = TransactionType.AdvancedPayment.ESCROW_SIGN

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.EscrowSignAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setEscrow(escrowId!!)
                    .setDecision(Escrow.decisionToProtobuf(decision!!))
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.escrowId = buffer.long
            this.decision = Escrow.byteToDecision(buffer.get())
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.escrowId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(ESCROW_ID_PARAMETER)))
            this.decision = Escrow.stringToDecision(JSON.getAsString(attachmentData.get(DECISION_PARAMETER))!!)
        }

        constructor(escrowId: Long?, decision: Escrow.DecisionType, blockchainHeight: Int) : super(blockchainHeight) {
            this.escrowId = escrowId
            this.decision = decision
        }

        internal constructor(attachment: BrsApi.EscrowSignAttachment) : super(attachment.version.toByte()) {
            this.escrowId = attachment.escrow
            this.decision = Escrow.protoBufToDecision(attachment.decision)
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(this.escrowId!!)
            buffer.put(Escrow.decisionToByte(this.decision!!)!!)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(ESCROW_ID_RESPONSE, Convert.toUnsignedLong(this.escrowId!!))
            attachment.addProperty(DECISION_RESPONSE, Escrow.decisionToString(this.decision!!))
        }
    }

    class AdvancedPaymentEscrowResult : AbstractAttachment {

        private val escrowId: Long?
        private val decision: Escrow.DecisionType?

        override val appendixName: String
            get() = "EscrowResult"

        override val mySize: Int
            get() = 8 + 1

        override val transactionType: TransactionType
            get() = TransactionType.AdvancedPayment.ESCROW_RESULT

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.EscrowResultAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setEscrow(2)
                    .setDecision(Escrow.decisionToProtobuf(decision!!))
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.escrowId = buffer.long
            this.decision = Escrow.byteToDecision(buffer.get())
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.escrowId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(ESCROW_ID_PARAMETER)))
            this.decision = Escrow.stringToDecision(JSON.getAsString(attachmentData.get(DECISION_PARAMETER))!!)
        }

        constructor(escrowId: Long?, decision: Escrow.DecisionType, blockchainHeight: Int) : super(blockchainHeight) {
            this.escrowId = escrowId
            this.decision = decision
        }

        internal constructor(attachment: BrsApi.EscrowResultAttachment) : super(attachment.version.toByte()) {
            this.escrowId = attachment.escrow
            this.decision = Escrow.protoBufToDecision(attachment.decision)
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(this.escrowId!!)
            buffer.put(Escrow.decisionToByte(this.decision!!)!!)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(ESCROW_ID_RESPONSE, Convert.toUnsignedLong(this.escrowId!!))
            attachment.addProperty(DECISION_RESPONSE, Escrow.decisionToString(this.decision!!))
        }
    }

    class AdvancedPaymentSubscriptionSubscribe : AbstractAttachment {

        val frequency: Int?

        override val appendixName: String
            get() = "SubscriptionSubscribe"

        override val mySize: Int
            get() = 4

        override val transactionType: TransactionType
            get() = TransactionType.AdvancedPayment.SUBSCRIPTION_SUBSCRIBE

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.SubscriptionSubscribeAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setFrequency(frequency!!)
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.frequency = buffer.int
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.frequency = JSON.getAsInt(attachmentData.get(FREQUENCY_PARAMETER))
        }

        constructor(frequency: Int, blockchainHeight: Int) : super(blockchainHeight) {
            this.frequency = frequency
        }

        internal constructor(attachment: BrsApi.SubscriptionSubscribeAttachment) : super(attachment.version.toByte()) {
            this.frequency = attachment.frequency
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putInt(this.frequency!!)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(FREQUENCY_RESPONSE, this.frequency)
        }
    }

    class AdvancedPaymentSubscriptionCancel : AbstractAttachment {

        val subscriptionId: Long?

        override val appendixName: String
            get() = "SubscriptionCancel"

        override val mySize: Int
            get() = 8

        override val transactionType: TransactionType
            get() = TransactionType.AdvancedPayment.SUBSCRIPTION_CANCEL

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.SubscriptionCancelAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setSubscription(subscriptionId!!)
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.subscriptionId = buffer.long
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.subscriptionId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(SUBSCRIPTION_ID_PARAMETER)))
        }

        constructor(subscriptionId: Long?, blockchainHeight: Int) : super(blockchainHeight) {
            this.subscriptionId = subscriptionId
        }

        internal constructor(attachment: BrsApi.SubscriptionCancelAttachment) : super(attachment.version.toByte()) {
            this.subscriptionId = attachment.subscription
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(subscriptionId!!)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(SUBSCRIPTION_ID_RESPONSE, Convert.toUnsignedLong(this.subscriptionId!!))
        }
    }

    class AdvancedPaymentSubscriptionPayment : AbstractAttachment {

        private val subscriptionId: Long?

        override val appendixName: String
            get() = "SubscriptionPayment"

        override val mySize: Int
            get() = 8

        override val transactionType: TransactionType
            get() = TransactionType.AdvancedPayment.SUBSCRIPTION_PAYMENT

        override val protobufMessage: Any
            get() = Any.pack(BrsApi.SubscriptionPaymentAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setSubscription(subscriptionId!!)
                    .build())

        internal constructor(buffer: ByteBuffer, transactionVersion: Byte) : super(buffer, transactionVersion) {
            this.subscriptionId = buffer.long
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {
            this.subscriptionId = Convert.parseUnsignedLong(JSON.getAsString(attachmentData.get(SUBSCRIPTION_ID_PARAMETER)))
        }

        constructor(subscriptionId: Long?, blockchainHeight: Int) : super(blockchainHeight) {
            this.subscriptionId = subscriptionId
        }

        internal constructor(attachment: BrsApi.SubscriptionPaymentAttachment) : super(attachment.version.toByte()) {
            this.subscriptionId = attachment.subscription
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            buffer.putLong(this.subscriptionId!!)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(SUBSCRIPTION_ID_RESPONSE, Convert.toUnsignedLong(this.subscriptionId!!))
        }
    }

    class AutomatedTransactionsCreation : AbstractAttachment {

        val name: String?
        val description: String?
        val creationBytes: ByteArray

        override val transactionType: TransactionType
            get() = TransactionType.AutomatedTransactions.AUTOMATED_TRANSACTION_CREATION

        override val appendixName: String
            get() = "AutomatedTransactionsCreation"
        override val mySize: Int
            get() = 1 + Convert.toBytes(name).size + 2 + Convert.toBytes(description).size + creationBytes!!.size


        override val protobufMessage: Any
            get() = Any.pack(BrsApi.ATCreationAttachment.newBuilder()
                    .setVersion(version.toInt())
                    .setName(name)
                    .setDescription(description)
                    .setCreationBytes(ByteString.copyFrom(creationBytes!!))
                    .build())

        @Throws(BurstException.NotValidException::class)
        internal constructor(buffer: ByteBuffer,
                             transactionVersion: Byte) : super(buffer, transactionVersion) {

            this.name = Convert.readString(buffer, buffer.get().toInt(), Constants.MAX_AUTOMATED_TRANSACTION_NAME_LENGTH)
            this.description = Convert.readString(buffer, buffer.short.toInt(), Constants.MAX_AUTOMATED_TRANSACTION_DESCRIPTION_LENGTH)

            // rest of the parsing is at related; code comes from
            // public AtMachineState( byte[] atId, byte[] creator, byte[] creationBytes, int height ) {
            val startPosition = buffer.position()
            buffer.short

            buffer.short //future: reserved for future needs

            val pageSize = AtConstants.pageSize(dp.blockchain.height).toInt()
            val codePages = buffer.short
            val dataPages = buffer.short
            buffer.short
            buffer.short

            buffer.long

            var codeLen: Int
            if (codePages * pageSize < pageSize + 1) {
                codeLen = buffer.get().toInt()
                if (codeLen < 0)
                    codeLen += (java.lang.Byte.MAX_VALUE + 1) * 2
            } else if (codePages * pageSize < java.lang.Short.MAX_VALUE + 1) {
                codeLen = buffer.short.toInt()
                if (codeLen < 0)
                    codeLen += (java.lang.Short.MAX_VALUE + 1) * 2
            } else {
                codeLen = buffer.int
            }
            val code = ByteArray(codeLen)
            buffer.get(code, 0, codeLen)

            var dataLen: Int
            if (dataPages * pageSize < 257) {
                dataLen = buffer.get().toInt()
                if (dataLen < 0)
                    dataLen += (java.lang.Byte.MAX_VALUE + 1) * 2
            } else if (dataPages * pageSize < java.lang.Short.MAX_VALUE + 1) {
                dataLen = buffer.short.toInt()
                if (dataLen < 0)
                    dataLen += (java.lang.Short.MAX_VALUE + 1) * 2
            } else {
                dataLen = buffer.int
            }
            val data = ByteArray(dataLen)
            buffer.get(data, 0, dataLen)

            val endPosition = buffer.position()
            buffer.position(startPosition)
            val dst = ByteArray(endPosition - startPosition)
            buffer.get(dst, 0, endPosition - startPosition)
            this.creationBytes = dst
        }

        internal constructor(attachmentData: JsonObject) : super(attachmentData) {

            this.name = JSON.getAsString(attachmentData.get(NAME_PARAMETER))
            this.description = JSON.getAsString(attachmentData.get(DESCRIPTION_PARAMETER))

            this.creationBytes = Convert.parseHexString(JSON.getAsString(attachmentData.get(CREATION_BYTES_PARAMETER)))

        }

        constructor(name: String, description: String, creationBytes: ByteArray, blockchainHeight: Int) : super(blockchainHeight) {
            this.name = name
            this.description = description
            this.creationBytes = creationBytes
        }

        internal constructor(attachment: BrsApi.ATCreationAttachment) : super(attachment.version.toByte()) {
            this.name = attachment.name
            this.description = attachment.description
            this.creationBytes = attachment.creationBytes.toByteArray()
        }

        override fun putMyBytes(buffer: ByteBuffer) {
            val nameBytes = Convert.toBytes(name)
            buffer.put(nameBytes.size.toByte())
            buffer.put(nameBytes)
            val descriptionBytes = Convert.toBytes(description)
            buffer.putShort(descriptionBytes.size.toShort())
            buffer.put(descriptionBytes)

            buffer.put(creationBytes!!)
        }

        override fun putMyJSON(attachment: JsonObject) {
            attachment.addProperty(NAME_RESPONSE, name)
            attachment.addProperty(DESCRIPTION_RESPONSE, description)
            attachment.addProperty(CREATION_BYTES_RESPONSE, Convert.toHexString(creationBytes))
        }
    }

    companion object {

        val ORDINARY_PAYMENT: EmptyAttachment = object : EmptyAttachment() {

            override val protobufMessage: Any
                get() = Any.pack(BrsApi.OrdinaryPaymentAttachment.getDefaultInstance())

            override val appendixName: String
                get() = "OrdinaryPayment"

            override val transactionType: TransactionType
                get() = Payment.ORDINARY

        }

        // the message payload is in the Appendix
        val ARBITRARY_MESSAGE: EmptyAttachment = object : EmptyAttachment() {

            override val protobufMessage = Any.pack(BrsApi.ArbitraryMessageAttachment.getDefaultInstance())

            override val appendixName = "ArbitraryMessage"

            override val transactionType = TransactionType.Messaging.ARBITRARY_MESSAGE

        }

        val AT_PAYMENT: EmptyAttachment = object : EmptyAttachment() {

            override val protobufMessage: Any
                get() = Any.pack(BrsApi.ATPaymentAttachment.getDefaultInstance())

            override val transactionType: TransactionType
                get() = TransactionType.AutomatedTransactions.AT_PAYMENT

            override val appendixName: String
                get() = "AT Payment"
            
        }
    }

}
