package brs
import brs.util.convert.safeMultiply
import brs.util.convert.toUnsignedString
import org.slf4j.LoggerFactory
import java.io.*
import java.math.BigInteger

class DebugTrace internal constructor(private val dp: DependencyProvider, private val quote: String, private val separator: String, private val logUnconfirmed: Boolean, private val columns: Array<String>, private val headers: Map<String, String>, private val accountIds: Set<Long>, private val logName: String) {
    private var log: PrintWriter? = null

    init {
        resetLog()
    }

    internal fun resetLog() {
        if (log != null) {
            log!!.close()
        }
        try {
            log = PrintWriter(BufferedWriter(OutputStreamWriter(FileOutputStream(logName))), true)
        } catch (e: IOException) {
            logger.debug("Debug tracing to $logName not possible", e)
            throw RuntimeException(e)
        }

        this.log(headers)
    }

    private fun include(accountId: Long): Boolean {
        return accountId != 0L && (accountIds.isEmpty() || accountIds.contains(accountId))
    }

    private fun include(attachment: Attachment): Boolean {
        return when (attachment) {
            is Attachment.DigitalGoodsPurchase -> include(dp.digitalGoodsStoreService.getGoods(attachment.goodsId)!!.sellerId)
            is Attachment.DigitalGoodsDelivery -> include(dp.digitalGoodsStoreService.getPurchase(attachment.purchaseId)!!.buyerId)
            is Attachment.DigitalGoodsRefund -> include(dp.digitalGoodsStoreService.getPurchase(attachment.purchaseId)!!.buyerId)
            else -> false
        }
    }

    // Note: Trade events occur before the change in account balances
    internal fun trace(trade: Trade) {
        val askAccountId = dp.assetExchange.getAskOrder(trade.askOrderId)!!.accountId
        val bidAccountId = dp.assetExchange.getBidOrder(trade.bidOrderId)!!.accountId
        if (include(askAccountId)) {
            log(getValues(askAccountId, trade, true))
        }
        if (include(bidAccountId)) {
            log(getValues(bidAccountId, trade, false))
        }
    }

    internal fun trace(account: Account, unconfirmed: Boolean) {
        if (include(account.id)) {
            log(getValues(account.id, unconfirmed))
        }
    }

    internal fun trace(accountAsset: Account.AccountAsset, unconfirmed: Boolean) {
        if (!include(accountAsset.accountId)) {
            return
        }
        log(getValues(accountAsset.accountId, accountAsset, unconfirmed))
    }


    internal fun traceBeforeAccept(block: Block) {
        val generatorId = block.generatorId
        if (include(generatorId)) {
            log(getValues(generatorId, block))
        }
    }

    internal fun trace(block: Block) {
        for (transaction in block.transactions) {
            val senderId = transaction.senderId
            if (include(senderId)) {
                log(getValues(senderId, transaction, false))
                log(getValues(senderId, transaction, transaction.attachment, false))
            }
            val recipientId = transaction.recipientId
            if (include(recipientId)) {
                log(getValues(recipientId, transaction, true))
                log(getValues(recipientId, transaction, transaction.attachment, true))
            } else {
                val attachment = transaction.attachment
                if (include(attachment)) {
                    log(getValues(recipientId, transaction, transaction.attachment, true))
                }
            }
        }
    }

    internal fun lessorGuaranteedBalance(account: Account, lesseeId: Long): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map["account"] = account.id.toUnsignedString()
        map["lessor guaranteed balance"] = account.balanceNQT.toString()
        map["lessee"] = lesseeId.toUnsignedString()
        map["timestamp"] = dp.blockchain.lastBlock.timestamp.toString()
        map["height"] = dp.blockchain.height.toString()
        map["event"] = "lessor guaranteed balance"
        return map
    }

    internal fun getValues(accountId: Long, unconfirmed: Boolean): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        map["account"] = accountId.toUnsignedString()
        val account = Account.getAccount(dp, accountId)
        map["balance"] = (account?.balanceNQT ?: 0).toString()
        map["unconfirmed balance"] = (account?.unconfirmedBalanceNQT ?: 0).toString()
        map["timestamp"] = dp.blockchain.lastBlock.timestamp.toString()
        map["height"] = dp.blockchain.height.toString()
        map["event"] = if (unconfirmed) "unconfirmed balance" else "balance"
        return map
    }

    internal fun getValues(accountId: Long, trade: Trade, isAsk: Boolean): Map<String, String> {
        val map = getValues(accountId, false)
        map["asset"] = trade.assetId.toUnsignedString()
        map["trade quantity"] = (if (isAsk) -trade.quantityQNT else trade.quantityQNT).toString()
        map["trade price"] = trade.priceNQT.toString()
        val tradeCost = trade.quantityQNT.safeMultiply(trade.priceNQT)
        map["trade cost"] = (if (isAsk) tradeCost else -tradeCost).toString()
        map["event"] = "trade"
        return map
    }

    internal fun getValues(accountId: Long, transaction: Transaction, isRecipient: Boolean): Map<String, String> {
        var amount = transaction.amountNQT
        var fee = transaction.feeNQT
        if (isRecipient) {
            fee = 0 // fee doesn't affect recipient account
        } else {
            // for sender the amounts are subtracted
            amount = -amount
            fee = -fee
        }
        if (fee == 0L && amount == 0L) {
            return emptyMap()
        }
        val map = getValues(accountId, false)
        map["transaction amount"] = amount.toString()
        map["transaction fee"] = fee.toString()
        map["transaction"] = transaction.stringId
        if (isRecipient) {
            map["sender"] = transaction.senderId.toUnsignedString()
        } else {
            map["recipient"] = transaction.recipientId.toUnsignedString()
        }
        map["event"] = "transaction"
        return map
    }

    internal fun getValues(accountId: Long, block: Block): Map<String, String> {
        val fee = block.totalFeeNQT
        if (fee == 0L) {
            return emptyMap()
        }
        val map = getValues(accountId, false)
        map["generation fee"] = fee.toString()
        map["block"] = block.stringId
        map["event"] = "block"
        map["timestamp"] = block.timestamp.toString()
        map["height"] = block.height.toString()
        return map
    }

    internal fun getValues(accountId: Long, accountAsset: Account.AccountAsset, unconfirmed: Boolean): Map<String, String> {
        val map = mutableMapOf<String, String>()
        map["account"] = accountId.toUnsignedString()
        map["asset"] = accountAsset.assetId.toUnsignedString()
        if (unconfirmed) {
            map["unconfirmed asset balance"] = accountAsset.unconfirmedQuantityQNT.toString()
        } else {
            map["asset balance"] = accountAsset.quantityQNT.toString()
        }
        map["timestamp"] = dp.blockchain.lastBlock.timestamp.toString()
        map["height"] = dp.blockchain.height.toString()
        map["event"] = "asset balance"
        return map
    }

    private fun getValues(accountId: Long, transaction: Transaction, attachment: Attachment, isRecipient: Boolean): Map<String, String> {
        var map: MutableMap<String, String> = getValues(accountId, false)
        if (attachment is Attachment.ColoredCoinsOrderPlacement) {
            if (isRecipient) {
                return emptyMap()
            }
            val isAsk = attachment is Attachment.ColoredCoinsAskOrderPlacement
            map["asset"] = attachment.assetId.toUnsignedString()
            map["order"] = transaction.stringId
            map["order price"] = attachment.priceNQT.toString()
            var quantity = attachment.quantityQNT
            if (isAsk) {
                quantity = -quantity
            }
            map["order quantity"] = quantity.toString()
            var orderCost = BigInteger.valueOf(attachment.priceNQT).multiply(BigInteger.valueOf(attachment.quantityQNT))
            if (!isAsk) {
                orderCost = orderCost.negate()
            }
            map["order cost"] = orderCost.toString()
            val event = (if (isAsk) "ask" else "bid") + " order"
            map["event"] = event
        } else if (attachment is Attachment.ColoredCoinsAssetIssuance) {
            if (isRecipient) {
                return emptyMap()
            }
            map["asset"] = transaction.stringId
            map["asset quantity"] = attachment.quantityQNT.toString()
            map["event"] = "asset issuance"
        } else if (attachment is Attachment.ColoredCoinsAssetTransfer) {
            map["asset"] = attachment.assetId.toUnsignedString()
            var quantity = attachment.quantityQNT
            if (!isRecipient) {
                quantity = -quantity
            }
            map["asset quantity"] = quantity.toString()
            map["event"] = "asset transfer"
        } else if (attachment is Attachment.ColoredCoinsOrderCancellation) {
            map["order"] = attachment.orderId.toUnsignedString()
            map["event"] = "order cancel"
        } else if (attachment is Attachment.DigitalGoodsPurchase) {
            val purchase = transaction.attachment as Attachment.DigitalGoodsPurchase
            if (isRecipient) {
                map = getValues(dp.digitalGoodsStoreService.getGoods(purchase.goodsId)!!.sellerId, false)
            }
            map["event"] = "purchase"
            map["purchase"] = transaction.stringId
        } else if (attachment is Attachment.DigitalGoodsDelivery) {
            val delivery = transaction.attachment as Attachment.DigitalGoodsDelivery
            val purchase = dp.digitalGoodsStoreService.getPurchase(delivery.purchaseId)!!
            if (isRecipient) {
                map = getValues(purchase.buyerId, false)
            }
            map["event"] = "delivery"
            map["purchase"] = delivery.purchaseId.toUnsignedString()
            var discount = delivery.discountNQT
            map["purchase price"] = purchase.priceNQT.toString()
            map["purchase quantity"] = purchase.quantity.toString()
            var cost = purchase.priceNQT.safeMultiply(purchase.quantity.toLong())
            if (isRecipient) {
                cost = -cost
            }
            map["purchase cost"] = cost.toString()
            if (!isRecipient) {
                discount = -discount
            }
            map["discount"] = discount.toString()
        } else if (attachment is Attachment.DigitalGoodsRefund) {
            val refund = transaction.attachment as Attachment.DigitalGoodsRefund
            if (isRecipient) {
                map = getValues(dp.digitalGoodsStoreService.getPurchase(refund.purchaseId)!!.buyerId, false)
            }
            map["event"] = "refund"
            map["purchase"] = refund.purchaseId.toUnsignedString()
            var refundNQT = refund.refundNQT
            if (!isRecipient) {
                refundNQT = -refundNQT
            }
            map["refund"] = refundNQT.toString()
        } else if (attachment is Attachment.ArbitraryMessage) {
            map = mutableMapOf()
            map["account"] = accountId.toUnsignedString()
            map["timestamp"] = dp.blockchain.lastBlock.timestamp.toString()
            map["height"] = dp.blockchain.height.toString()
            map["event"] = "message"
            if (isRecipient) {
                map["sender"] = transaction.senderId.toUnsignedString()
            } else {
                map["recipient"] = transaction.recipientId.toUnsignedString()
            }
        } else {
            return emptyMap()
        }
        return map
    }

    internal fun log(map: Map<String, String>) {
        if (map.isEmpty()) {
            return
        }
        val buf = StringBuilder()
        for (column in columns) {
            if (!logUnconfirmed && column.startsWith("unconfirmed")) {
                continue
            }
            val value = map[column]
            if (value != null) {
                buf.append(quote).append(value).append(quote)
            }
            buf.append(separator)
        }
        log!!.println(buf.toString())
    }
    
    companion object {
        private val logger = LoggerFactory.getLogger(DebugTrace::class.java)
    }
}
