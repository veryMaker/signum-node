package brs

import brs.assetexchange.AssetExchange
import brs.props.PropertyService
import brs.props.Props
import brs.services.AccountService
import brs.services.DGSGoodsStoreService
import brs.util.Convert
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.*
import java.math.BigInteger
import java.util.*
import java.util.function.Consumer
import kotlin.properties.Delegates

class DebugTrace private constructor(private val accountIds: Set<Long>, private val logName: String) {
    private var log: PrintWriter? = null

    init {
        resetLog()
    }

    private fun resetLog() {
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
            is Attachment.DigitalGoodsPurchase -> include(dp.digitalGoodsStoreService.getGoods(attachment.goodsId).sellerId)
            is Attachment.DigitalGoodsDelivery -> include(dp.digitalGoodsStoreService.getPurchase(attachment.purchaseId).buyerId)
            is Attachment.DigitalGoodsRefund -> include(dp.digitalGoodsStoreService.getPurchase(attachment.purchaseId).buyerId)
            else -> false
        }
    }

    // Note: Trade events occur before the change in account balances
    private fun trace(trade: Trade) {
        val askAccountId = dp.assetExchange.getAskOrder(trade.askOrderId).accountId
        val bidAccountId = dp.assetExchange.getBidOrder(trade.bidOrderId).accountId
        if (include(askAccountId)) {
            log(getValues(askAccountId, trade, true))
        }
        if (include(bidAccountId)) {
            log(getValues(bidAccountId, trade, false))
        }
    }

    private fun trace(account: Account, unconfirmed: Boolean) {
        if (include(account.getId())) {
            log(getValues(account.getId(), unconfirmed))
        }
    }

    private fun trace(accountAsset: Account.AccountAsset, unconfirmed: Boolean) {
        if (!include(accountAsset.getAccountId())) {
            return
        }
        log(getValues(accountAsset.getAccountId(), accountAsset, unconfirmed))
    }


    private fun traceBeforeAccept(block: Block) {
        val generatorId = block.generatorId
        if (include(generatorId)) {
            log(getValues(generatorId, block))
        }
    }

    private fun trace(block: Block) {
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

    private fun lessorGuaranteedBalance(account: Account, lesseeId: Long): Map<String, String> {
        val map = HashMap<String, String>()
        map["account"] = Convert.toUnsignedLong(account.getId())
        map["lessor guaranteed balance"] = account.getBalanceNQT().toString()
        map["lessee"] = Convert.toUnsignedLong(lesseeId)
        map["timestamp"] = dp.blockchain.lastBlock.timestamp.toString()
        map["height"] = dp.blockchain.height.toString()
        map["event"] = "lessor guaranteed balance"
        return map
    }

    private fun getValues(accountId: Long, unconfirmed: Boolean): MutableMap<String, String> {
        val map = HashMap<String, String>()
        map["account"] = Convert.toUnsignedLong(accountId)
        val account = Account.getAccount(dp, accountId)
        map["balance"] = (account?.getBalanceNQT() ?: 0).toString()
        map["unconfirmed balance"] = (account?.getUnconfirmedBalanceNQT() ?: 0).toString()
        map["timestamp"] = dp.blockchain.lastBlock.timestamp.toString()
        map["height"] = dp.blockchain.height.toString()
        map["event"] = if (unconfirmed) "unconfirmed balance" else "balance"
        return map
    }

    private fun getValues(accountId: Long, trade: Trade, isAsk: Boolean): Map<String, String> {
        val map = getValues(accountId, false)
        map["asset"] = Convert.toUnsignedLong(trade.assetId)
        map["trade quantity"] = (if (isAsk) -trade.quantityQNT else trade.quantityQNT).toString()
        map["trade price"] = trade.priceNQT.toString()
        val tradeCost = Convert.safeMultiply(trade.quantityQNT, trade.priceNQT)
        map["trade cost"] = (if (isAsk) tradeCost else -tradeCost).toString()
        map["event"] = "trade"
        return map
    }

    private fun getValues(accountId: Long, transaction: Transaction, isRecipient: Boolean): Map<String, String> {
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
            map["sender"] = Convert.toUnsignedLong(transaction.senderId)
        } else {
            map["recipient"] = Convert.toUnsignedLong(transaction.recipientId)
        }
        map["event"] = "transaction"
        return map
    }

    private fun getValues(accountId: Long, block: Block): Map<String, String> {
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

    private fun getValues(accountId: Long, accountAsset: Account.AccountAsset, unconfirmed: Boolean): Map<String, String> {
        val map = HashMap<String, String>()
        map["account"] = Convert.toUnsignedLong(accountId)
        map["asset"] = Convert.toUnsignedLong(accountAsset.getAssetId())
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
            map["asset"] = Convert.toUnsignedLong(attachment.assetId)
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
            map["asset"] = Convert.toUnsignedLong(attachment.assetId)
            var quantity = attachment.quantityQNT
            if (!isRecipient) {
                quantity = -quantity
            }
            map["asset quantity"] = quantity.toString()
            map["event"] = "asset transfer"
        } else if (attachment is Attachment.ColoredCoinsOrderCancellation) {
            map["order"] = Convert.toUnsignedLong(attachment.orderId)
            map["event"] = "order cancel"
        } else if (attachment is Attachment.DigitalGoodsPurchase) {
            val purchase = transaction.attachment as Attachment.DigitalGoodsPurchase
            if (isRecipient) {
                map = getValues(dp.digitalGoodsStoreService.getGoods(purchase.goodsId).sellerId, false)
            }
            map["event"] = "purchase"
            map["purchase"] = transaction.stringId
        } else if (attachment is Attachment.DigitalGoodsDelivery) {
            val delivery = transaction.attachment as Attachment.DigitalGoodsDelivery
            val purchase = dp.digitalGoodsStoreService.getPurchase(delivery.purchaseId)
            if (isRecipient) {
                map = getValues(purchase.buyerId, false)
            }
            map["event"] = "delivery"
            map["purchase"] = Convert.toUnsignedLong(delivery.purchaseId)
            var discount = delivery.discountNQT
            map["purchase price"] = purchase.priceNQT.toString()
            map["purchase quantity"] = purchase.quantity.toString()
            var cost = Convert.safeMultiply(purchase.priceNQT, purchase.quantity.toLong())
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
                map = getValues(dp.digitalGoodsStoreService.getPurchase(refund.purchaseId).buyerId, false)
            }
            map["event"] = "refund"
            map["purchase"] = Convert.toUnsignedLong(refund.purchaseId)
            var refundNQT = refund.refundNQT
            if (!isRecipient) {
                refundNQT = -refundNQT
            }
            map["refund"] = refundNQT.toString()
        } else if (attachment === Attachment.ARBITRARY_MESSAGE) {
            map = HashMap()
            map["account"] = Convert.toUnsignedLong(accountId)
            map["timestamp"] = dp.blockchain.lastBlock.timestamp.toString()
            map["height"] = dp.blockchain.height.toString()
            map["event"] = "message"
            if (isRecipient) {
                map["sender"] = Convert.toUnsignedLong(transaction.senderId)
            } else {
                map["recipient"] = Convert.toUnsignedLong(transaction.recipientId)
            }
        } else {
            return emptyMap()
        }
        return map
    }

    private fun log(map: Map<String, String>) {
        if (map.isEmpty()) {
            return
        }
        val buf = StringBuilder()
        for (column in columns) {
            if (!LOG_UNCONFIRMED && column.startsWith("unconfirmed")) {
                continue
            }
            val value = map[column]
            if (value != null) {
                buf.append(QUOTE).append(value).append(QUOTE)
            }
            buf.append(SEPARATOR)
        }
        log!!.println(buf.toString())
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DebugTrace::class.java)

        internal lateinit var QUOTE: String
        internal lateinit var SEPARATOR: String
        private var LOG_UNCONFIRMED: Boolean by Delegates.notNull() // Cannot use lateinit on primitives

        // TODO remove static DP
        private lateinit var dp: DependencyProvider

        internal fun init(dp: DependencyProvider) {
            this.dp = dp

            QUOTE = dp.propertyService.get(Props.BRS_DEBUG_TRACE_QUOTE)
            SEPARATOR = dp.propertyService.get(Props.BRS_DEBUG_TRACE_SEPARATOR)
            LOG_UNCONFIRMED = dp.propertyService.get(Props.BRS_DEBUG_LOG_CONFIRMED)

            val accountIdStrings = dp.propertyService.get(Props.BRS_DEBUG_TRACE_ACCOUNTS)
            val logName = dp.propertyService.get(Props.BRS_DEBUG_TRACE_LOG)
            if (accountIdStrings.isEmpty() || logName == null) {
                return
            }
            val accountIds = HashSet<Long>()
            for (accountId in accountIdStrings) {
                if ("*" == accountId) {
                    accountIds.clear()
                    break
                }
                accountIds.add(Convert.parseUnsignedLong(accountId))
            }
            val debugTrace = addDebugTrace(accountIds, logName)
            dp.blockchainProcessor.addListener(Consumer { debugTrace.resetLog() }, BlockchainProcessor.Event.RESCAN_BEGIN)
            logger.debug("Debug tracing of " + (if (accountIdStrings.contains("*"))
                "ALL"
            else
                accountIds.size.toString()) + " accounts enabled")
        }

        private fun addDebugTrace(accountIds: Set<Long>, logName: String): DebugTrace {
            val debugTrace = DebugTrace(accountIds, logName)
            dp.assetExchange.addTradeListener({ debugTrace.trace(it) }, Trade.Event.TRADE)
            dp.accountService.addListener(Consumer { account -> debugTrace.trace(account, false) }, Account.Event.BALANCE)
            if (LOG_UNCONFIRMED) {
                dp.accountService.addListener(Consumer { account -> debugTrace.trace(account, true) }, Account.Event.UNCONFIRMED_BALANCE)
            }
            dp.accountService.addAssetListener({ accountAsset -> debugTrace.trace(accountAsset, false) }, Account.Event.ASSET_BALANCE)
            if (LOG_UNCONFIRMED) {
                dp.accountService.addAssetListener({ accountAsset -> debugTrace.trace(accountAsset, true) }, Account.Event.UNCONFIRMED_ASSET_BALANCE)
            }
            dp.blockchainProcessor.addListener(Consumer { debugTrace.traceBeforeAccept(it) }, BlockchainProcessor.Event.BEFORE_BLOCK_ACCEPT)
            dp.blockchainProcessor.addListener(Consumer { debugTrace.trace(it) }, BlockchainProcessor.Event.BEFORE_BLOCK_APPLY)
            return debugTrace
        }

        private val columns = arrayOf("height", "event", "account", "asset", "balance", "unconfirmed balance", "asset balance", "unconfirmed asset balance", "transaction amount", "transaction fee", "generation fee", "effective balance", "order", "order price", "order quantity", "order cost", "trade price", "trade quantity", "trade cost", "asset quantity", "transaction", "lessee", "lessor guaranteed balance", "purchase", "purchase price", "purchase quantity", "purchase cost", "discount", "refund", "sender", "recipient", "block", "timestamp")

        private val headers = HashMap<String, String>()

        init {
            for (entry in columns) {
                headers[entry] = entry
            }
        }
    }
}
