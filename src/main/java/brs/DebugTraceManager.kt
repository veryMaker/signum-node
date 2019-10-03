package brs

import brs.props.Props
import brs.util.parseUnsignedLong
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.util.*

class DebugTraceManager(private val dp: DependencyProvider) {
    private var QUOTE: String
    private var SEPARATOR: String
    private var LOG_UNCONFIRMED: Boolean
    private val columns = arrayOf("height", "event", "account", "asset", "balance", "unconfirmed balance", "asset balance", "unconfirmed asset balance", "transaction amount", "transaction fee", "generation fee", "effective balance", "order", "order price", "order quantity", "order cost", "trade price", "trade quantity", "trade cost", "asset quantity", "transaction", "lessee", "lessor guaranteed balance", "purchase", "purchase price", "purchase quantity", "purchase cost", "discount", "refund", "sender", "recipient", "block", "timestamp")
    private val headers = mutableMapOf<String, String>()

    init {
        for (entry in columns) {
            headers[entry] = entry
        }
        QUOTE = dp.propertyService.get(Props.BRS_DEBUG_TRACE_QUOTE)
        SEPARATOR = dp.propertyService.get(Props.BRS_DEBUG_TRACE_SEPARATOR)
        LOG_UNCONFIRMED = dp.propertyService.get(Props.BRS_DEBUG_LOG_CONFIRMED)

        val accountIdStrings = dp.propertyService.get(Props.BRS_DEBUG_TRACE_ACCOUNTS)
        val logName = dp.propertyService.get(Props.BRS_DEBUG_TRACE_LOG)
        if (accountIdStrings.isNotEmpty()) {
            val accountIds = mutableSetOf<Long>()
            for (accountId in accountIdStrings) {
                if ("*" == accountId) {
                    accountIds.clear()
                    break
                }
                accountIds.add(accountId.parseUnsignedLong())
            }
            runBlocking {
                val debugTrace = addDebugTrace(accountIds, logName)
                dp.blockchainProcessor.addListener(BlockchainProcessor.Event.RESCAN_BEGIN, { debugTrace.resetLog() })
            }
            logger.debug("Debug tracing of " + (if (accountIdStrings.contains("*")) "ALL" else accountIds.size.toString()) + " accounts enabled")
        }
    }

    private suspend fun addDebugTrace(accountIds: Set<Long>, logName: String): DebugTrace {
        val debugTrace = DebugTrace(dp, QUOTE, SEPARATOR, LOG_UNCONFIRMED, columns, headers, accountIds, logName)
        dp.assetExchange.addTradeListener(Trade.Event.TRADE, { debugTrace.trace(it) })
        dp.accountService.addListener(Account.Event.BALANCE, { account -> debugTrace.trace(account, false) })
        if (LOG_UNCONFIRMED) {
            dp.accountService.addListener(Account.Event.UNCONFIRMED_BALANCE, { account -> debugTrace.trace(account, true) })
        }
        dp.accountService.addAssetListener(Account.Event.ASSET_BALANCE, { accountAsset -> debugTrace.trace(accountAsset, false) })
        if (LOG_UNCONFIRMED) {
            dp.accountService.addAssetListener(Account.Event.UNCONFIRMED_ASSET_BALANCE, { accountAsset -> debugTrace.trace(accountAsset, true) })
        }
        dp.blockchainProcessor.addListener(BlockchainProcessor.Event.BEFORE_BLOCK_ACCEPT, { debugTrace.traceBeforeAccept(it) })
        dp.blockchainProcessor.addListener(BlockchainProcessor.Event.BEFORE_BLOCK_APPLY, { debugTrace.trace(it) })
        return debugTrace
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DebugTraceManager::class.java)
    }
}