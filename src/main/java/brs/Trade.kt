package brs

import brs.db.BurstKey
import brs.util.convert.toUnsignedString

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
    val quantityQNT: Long
    val priceNQT: Long
    val isBuy: Boolean

    enum class Event {
        TRADE
    }

    protected constructor(timestamp: Int, assetId: Long, blockId: Long, height: Int,
                          askOrderId: Long, bidOrderId: Long, askOrderHeight: Int, bidOrderHeight: Int,
                          sellerId: Long, buyerId: Long, dbKey: BurstKey, quantityQNT: Long, priceNQT: Long) {
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
        this.quantityQNT = quantityQNT
        this.priceNQT = priceNQT
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
        this.quantityQNT = Math.min(askOrder.quantityQNT, bidOrder.quantityQNT)
        this.isBuy = askOrderHeight < bidOrderHeight || askOrderHeight == bidOrderHeight && askOrderId < bidOrderId
        this.priceNQT = if (isBuy) askOrder.priceNQT else bidOrder.priceNQT
    }

    override fun toString(): String {
        return "Trade asset: ${assetId.toUnsignedString()} ask: ${askOrderId.toUnsignedString()} bid: ${bidOrderId.toUnsignedString()} price: $priceNQT quantity: $quantityQNT height: $height"
    }

}
