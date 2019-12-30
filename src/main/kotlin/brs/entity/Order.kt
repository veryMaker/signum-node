package brs.entity

import brs.api.grpc.proto.BrsApi
import brs.db.BurstKey
import brs.transaction.appendix.Attachment
import brs.util.convert.toUnsignedString

abstract class Order {

    val id: Long
    val accountId: Long
    val assetId: Long
    val pricePlanck: Long
    val height: Int

    var quantity: Long = 0

    abstract val protobufType: BrsApi.AssetOrderType

    private constructor(transaction: Transaction, attachment: Attachment.ColoredCoinsOrderPlacement) {
        this.id = transaction.id
        this.accountId = transaction.senderId
        this.assetId = attachment.assetId
        this.quantity = attachment.quantity
        this.pricePlanck = attachment.pricePlanck
        this.height = transaction.height
    }

    internal constructor(
        id: Long,
        accountId: Long,
        assetId: Long,
        pricePlanck: Long,
        creationHeight: Int,
        quantity: Long
    ) {
        this.id = id
        this.accountId = accountId
        this.assetId = assetId
        this.pricePlanck = pricePlanck
        this.height = creationHeight
        this.quantity = quantity
    }

    override fun toString(): String {
        return "${javaClass.simpleName} id: ${id.toUnsignedString()} account: ${accountId.toUnsignedString()} asset: ${assetId.toUnsignedString()} price: $pricePlanck quantity: $quantity height: $height"
    }

    class Ask : Order {

        val dbKey: BurstKey

        override val protobufType: BrsApi.AssetOrderType
            get() = BrsApi.AssetOrderType.ASK

        constructor(
            dbKey: BurstKey,
            transaction: Transaction,
            attachment: Attachment.ColoredCoinsAskOrderPlacement
        ) : super(transaction, attachment) {
            this.dbKey = dbKey
        }

        constructor(
            id: Long,
            accountId: Long,
            assetId: Long,
            pricePlanck: Long,
            creationHeight: Int,
            quantity: Long,
            dbKey: BurstKey
        ) : super(id, accountId, assetId, pricePlanck, creationHeight, quantity) {
            this.dbKey = dbKey
        }
    }

    class Bid : Order {

        val dbKey: BurstKey

        override val protobufType: BrsApi.AssetOrderType
            get() = BrsApi.AssetOrderType.BID

        constructor(
            dbKey: BurstKey,
            transaction: Transaction,
            attachment: Attachment.ColoredCoinsBidOrderPlacement
        ) : super(transaction, attachment) {
            this.dbKey = dbKey
        }

        constructor(
            id: Long,
            accountId: Long,
            assetId: Long,
            pricePlanck: Long,
            creationHeight: Int,
            quantity: Long,
            dbKey: BurstKey
        ) : super(id, accountId, assetId, pricePlanck, creationHeight, quantity) {
            this.dbKey = dbKey
        }
    }
}
