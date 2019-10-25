package brs.entity

import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.objects.Constants
import brs.transaction.appendix.Attachment

open class Goods {

    val id: Long
    val dbKey: BurstKey
    val sellerId: Long
    val name: String?
    val description: String?
    val tags: String?
    val timestamp: Int
    var quantity: Int = 0
        private set
    var pricePlanck: Long = 0
        private set
    var isDelisted: Boolean = false

    protected constructor(id: Long, dbKey: BurstKey, sellerId: Long, name: String, description: String, tags: String, timestamp: Int,
                          quantity: Int, pricePlanck: Long, delisted: Boolean) {
        this.id = id
        this.dbKey = dbKey
        this.sellerId = sellerId
        this.name = name
        this.description = description
        this.tags = tags
        this.timestamp = timestamp
        this.quantity = quantity
        this.pricePlanck = pricePlanck
        this.isDelisted = delisted
    }

    constructor(dbKey: BurstKey, transaction: Transaction, attachment: Attachment.DigitalGoodsListing) {
        this.dbKey = dbKey
        this.id = transaction.id
        this.sellerId = transaction.senderId
        this.name = attachment.name
        this.description = attachment.description
        this.tags = attachment.tags
        this.quantity = attachment.quantity
        this.pricePlanck = attachment.pricePlanck
        this.isDelisted = false
        this.timestamp = transaction.timestamp
    }

    fun changeQuantity(deltaQuantity: Int) {
        quantity += deltaQuantity
        if (quantity < 0) {
            quantity = 0
        } else if (quantity > Constants.MAX_DGS_LISTING_QUANTITY) {
            quantity = Constants.MAX_DGS_LISTING_QUANTITY
        }
    }

    fun changePrice(pricePlanck: Long) {
        this.pricePlanck = pricePlanck
    }

    companion object {
        // TODO remove these getters
        fun goodsDbKeyFactory(dp: DependencyProvider): BurstKey.LongKeyFactory<Goods> {
            return dp.digitalGoodsStoreStore.goodsDbKeyFactory
        }

        // TODO remove these getters
        fun goodsTable(dp: DependencyProvider): VersionedEntityTable<Goods> {
            return dp.digitalGoodsStoreStore.goodsTable
        }
    }
}
