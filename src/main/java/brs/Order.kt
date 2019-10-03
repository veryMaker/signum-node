package brs

import brs.db.BurstKey
import brs.grpc.proto.BrsApi
import brs.util.convert.toUnsignedString

abstract class Order {

    val id: Long
    val accountId: Long
    val assetId: Long
    val priceNQT: Long
    val height: Int

    var quantityQNT: Long = 0

    abstract val protobufType: BrsApi.OrderType

    private constructor(transaction: Transaction, attachment: Attachment.ColoredCoinsOrderPlacement) {
        this.id = transaction.id
        this.accountId = transaction.senderId
        this.assetId = attachment.assetId
        this.quantityQNT = attachment.quantityQNT
        this.priceNQT = attachment.priceNQT
        this.height = transaction.height
    }

    internal constructor(id: Long, accountId: Long, assetId: Long, priceNQT: Long, creationHeight: Int, quantityQNT: Long) {
        this.id = id
        this.accountId = accountId
        this.assetId = assetId
        this.priceNQT = priceNQT
        this.height = creationHeight
        this.quantityQNT = quantityQNT
    }

    override fun toString(): String {
        return "${javaClass.simpleName} id: ${id.toUnsignedString()} account: ${accountId.toUnsignedString()} asset: ${assetId.toUnsignedString()} price: $priceNQT quantity: $quantityQNT height: $height"
    }

    open class Ask : Order {

        val dbKey: BurstKey

        override val protobufType: BrsApi.OrderType
            get() = BrsApi.OrderType.ASK

        constructor(dbKey: BurstKey, transaction: Transaction, attachment: Attachment.ColoredCoinsAskOrderPlacement) : super(transaction, attachment) {
            this.dbKey = dbKey
        }

        protected constructor(id: Long, accountId: Long, assetId: Long, priceNQT: Long, creationHeight: Int, quantityQNT: Long, dbKey: BurstKey) : super(id, accountId, assetId, priceNQT, creationHeight, quantityQNT) {
            this.dbKey = dbKey
        }
    }

    open class Bid : Order {

        val dbKey: BurstKey

        override val protobufType: BrsApi.OrderType
            get() = BrsApi.OrderType.BID

        constructor(dbKey: BurstKey, transaction: Transaction, attachment: Attachment.ColoredCoinsBidOrderPlacement) : super(transaction, attachment) {
            this.dbKey = dbKey
        }

        protected constructor(id: Long, accountId: Long, assetId: Long, priceNQT: Long, creationHeight: Int, quantityQNT: Long, dbKey: BurstKey) : super(id, accountId, assetId, priceNQT, creationHeight, quantityQNT) {
            this.dbKey = dbKey
        }
    }
}
