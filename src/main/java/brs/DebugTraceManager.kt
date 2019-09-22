package brs

import brs.props.Props
import brs.util.parseUnsignedLong
import org.slf4j.LoggerFactory
import java.util.*

class DebugTraceManager(dp: DependencyProvider) {
    private var QUOTE: String
    private var SEPARATOR: String
    private var LOG_UNCONFIRMED: Boolean
    private val columns = arrayOf("height", "event", "account", "asset", "balance", "unconfirmed balance", "asset balance", "unconfirmed asset balance", "transaction amount", "transaction fee", "generation fee", "effective balance", "order", "order price", "order quantity", "order cost", "trade price", "trade quantity", "trade cost", "asset quantity", "transaction", "lessee", "lessor guaranteed balance", "purchase", "purchase price", "purchase quantity", "purchase cost", "discount", "refund", "sender", "recipient", "block", "timestamp")
    private val headers = mutableMapOf<String, String>()

    // TODO remove static DP
    private lateinit var dp: DependencyProvider

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
            val accountIds = HashSet<Long>()
            for (accountId in accountIdStrings) {
                if ("*" == accountId) {
                    accountIds.clear()
                    break
                }
                accountIds.add(accountId.parseUnsignedLong())
            }
            val debugTrace = addDebugTrace(accountIds, logName)
            dp.blockchainProcessor.addListener({ debugTrace.resetLog() }, BlockchainProcessor.Event.RESCAN_BEGIN)
            logger.debug("Debug tracing of " + (if (accountIdStrings.contains("*")) "ALL" else accountIds.size.toString()) + " accounts enabled")
        }
    }

    private fun addDebugTrace(accountIds: Set<Long>, logName: String): DebugTrace {
        val debugTrace = DebugTrace(dp, QUOTE, SEPARATOR, LOG_UNCONFIRMED, columns, headers, accountIds, logName)
        dp.assetExchange.addTradeListener({ debugTrace.trace(it) }, Trade.Event.TRADE)
        dp.accountService.addListener({ account -> debugTrace.trace(account, false) }, Account.Event.BALANCE)
        if (LOG_UNCONFIRMED) {
            dp.accountService.addListener({ account -> debugTrace.trace(account, true) }, Account.Event.UNCONFIRMED_BALANCE)
        }
        dp.accountService.addAssetListener({ accountAsset -> debugTrace.trace(accountAsset, false) }, Account.Event.ASSET_BALANCE)
        if (LOG_UNCONFIRMED) {
            dp.accountService.addAssetListener({ accountAsset -> debugTrace.trace(accountAsset, true) }, Account.Event.UNCONFIRMED_ASSET_BALANCE)
        }
        dp.blockchainProcessor.addListener({ debugTrace.traceBeforeAccept(it) }, BlockchainProcessor.Event.BEFORE_BLOCK_ACCEPT)
        dp.blockchainProcessor.addListener({ debugTrace.trace(it) }, BlockchainProcessor.Event.BEFORE_BLOCK_APPLY)
        return debugTrace
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DebugTraceManager::class.java)
    }
}