package brs

import brs.db.BurstKey
import brs.util.convert.toUnsignedString
import kotlin.math.min

open class Trade {

    val timestamp: Int
    val assetId: Long
    val blockId: Long
    val height: Int
    val askOrderId: Long
    val bidOrderId: Long
    val askOrderHeight: Int
    val bidOrderHeight: Int
    val sellerId: Long
    val buyerId: Long
    val dbKey: BurstKey
    val quantity: Long
    val pricePlanck: Long
    val isBuy: Boolean

    enum class Event {
        TRADE
    }

    protected constructor(timestamp: Int, assetId: Long, blockId: Long, height: Int,
                          askOrderId: Long, bidOrderId: Long, askOrderHeight: Int, bidOrderHeight: Int,
                          sellerId: Long, buyerId: Long, dbKey: BurstKey, quantity: Long, pricePlanck: Long) {
        this.timestamp = timestamp
        this.assetId = assetId
        this.blockId = blockId
        this.height = height
        this.askOrderId = askOrderId
        this.bidOrderId = bidOrderId
        this.askOrderHeight = askOrderHeight
        this.bidOrderHeight = bidOrderHeight
        this.sellerId = sellerId
        this.buyerId = buyerId
        this.dbKey = dbKey
        this.quantity = quantity
        this.pricePlanck = pricePlanck
        this.isBuy = askOrderHeight < bidOrderHeight || askOrderHeight == bidOrderHeight && askOrderId < bidOrderId
    }

    constructor(dbKey: BurstKey, assetId: Long, block: Block, askOrder: Order.Ask, bidOrder: Order.Bid) {
        this.dbKey = dbKey
        this.blockId = block.id
        this.height = block.height
        this.assetId = assetId
        this.timestamp = block.timestamp
        this.askOrderId = askOrder.id
        this.bidOrderId = bidOrder.id
        this.askOrderHeight = askOrder.height
        this.bidOrderHeight = bidOrder.height
        this.sellerId = askOrder.accountId
        this.buyerId = bidOrder.accountId
        this.quantity = min(askOrder.quantity, bidOrder.quantity)
        this.isBuy = askOrderHeight < bidOrderHeight || askOrderHeight == bidOrderHeight && askOrderId < bidOrderId
        this.pricePlanck = if (isBuy) askOrder.pricePlanck else bidOrder.pricePlanck
    }

    override fun toString(): String {
        return "Trade asset: ${assetId.toUnsignedString()} ask: ${askOrderId.toUnsignedString()} bid: ${bidOrderId.toUnsignedString()} price: $pricePlanck quantity: $quantity height: $height"
    }

}
