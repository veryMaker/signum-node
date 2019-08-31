package brs

import brs.props.Props

import java.util.Calendar
import java.util.TimeZone

object Constants {

    val BURST_DIFF_ADJUST_CHANGE_BLOCK = 2700

    val BURST_REWARD_RECIPIENT_ASSIGNMENT_WAIT_TIME: Long = 4

    // not sure when these were enabled, but they each do an alias lookup every block if greater than the current height
    val BURST_ESCROW_START_BLOCK: Long = 0
    val BURST_SUBSCRIPTION_START_BLOCK: Long = 0
    val BURST_SUBSCRIPTION_MIN_FREQ = 3600
    val BURST_SUBSCRIPTION_MAX_FREQ = 31536000

    val BLOCK_HEADER_LENGTH = 232

    val MAX_BALANCE_BURST = 2158812800L

    val FEE_QUANT: Long = 735000
    val ONE_BURST: Long = 100000000

    val MAX_BALANCE_NQT = MAX_BALANCE_BURST * ONE_BURST
    val INITIAL_BASE_TARGET = 18325193796L
    val MAX_BASE_TARGET = 18325193796L

    val MAX_ALIAS_URI_LENGTH = 1000
    val MAX_ALIAS_LENGTH = 100

    val MAX_ARBITRARY_MESSAGE_LENGTH = 1000
    val MAX_ENCRYPTED_MESSAGE_LENGTH = 1000

    val MAX_MULTI_OUT_RECIPIENTS = 64
    val MAX_MULTI_SAME_OUT_RECIPIENTS = 128

    val MAX_ACCOUNT_NAME_LENGTH = 100
    val MAX_ACCOUNT_DESCRIPTION_LENGTH = 1000

    val MAX_ASSET_QUANTITY_QNT = 1000000000L * 100000000L
    val ASSET_ISSUANCE_FEE_NQT = 1000 * ONE_BURST
    val MIN_ASSET_NAME_LENGTH = 3
    val MAX_ASSET_NAME_LENGTH = 10
    val MAX_ASSET_DESCRIPTION_LENGTH = 1000
    val MAX_ASSET_TRANSFER_COMMENT_LENGTH = 1000

    val MAX_POLL_NAME_LENGTH = 100
    val MAX_POLL_DESCRIPTION_LENGTH = 1000
    val MAX_POLL_OPTION_LENGTH = 100
    val MAX_POLL_OPTION_COUNT = 100

    val MAX_DGS_LISTING_QUANTITY = 1000000000
    val MAX_DGS_LISTING_NAME_LENGTH = 100
    val MAX_DGS_LISTING_DESCRIPTION_LENGTH = 1000
    val MAX_DGS_LISTING_TAGS_LENGTH = 100
    val MAX_DGS_GOODS_LENGTH = 10240

    val NQT_BLOCK = 0 // TODO remove
    val REFERENCED_TRANSACTION_FULL_HASH_BLOCK = 0 // TODO remove
    val REFERENCED_TRANSACTION_FULL_HASH_BLOCK_TIMESTAMP = 0 // TODO remove

    val MAX_AUTOMATED_TRANSACTION_NAME_LENGTH = 30
    val MAX_AUTOMATED_TRANSACTION_DESCRIPTION_LENGTH = 1000

    val FEE_SUGGESTION_MAX_HISTORY_LENGTH = 10

    val HTTP = "http://"

    val MIN_VERSION = Version.parse("v2.3.0")
    internal var UNCONFIRMED_POOL_DEPOSIT_NQT: Long = 0

    // TODO burstkit4j integration
    val EPOCH_BEGINNING: Long

    val ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz"

    val EC_RULE_TERMINATOR = 2400 /* cfb: This constant defines a straight edge when "longest chain"
                                                        rule is outweighed by "economic majority" rule; the terminator
                                                        is set as number of seconds before the current time. */

    val EC_BLOCK_DISTANCE_LIMIT = 60
    val EC_CHANGE_BLOCK_1 = 67000

    val RESPONSE = "response"
    val TOKEN = "token"
    val WEBSITE = "website"
    val PROTOCOL = "protocol"

    val BLOCK_PROCESS_THREAD_DELAY = 500 // Milliseconds

    init {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(Calendar.YEAR, 2014)
        calendar.set(Calendar.MONTH, Calendar.AUGUST)
        calendar.set(Calendar.DAY_OF_MONTH, 11)
        calendar.set(Calendar.HOUR_OF_DAY, 2)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        EPOCH_BEGINNING = calendar.timeInMillis
    }

    fun init(dp: DependencyProvider) {
        val dbRollback = dp.propertyService.get(Props.DB_MAX_ROLLBACK).toLong()
        require(dbRollback >= 1440) { "brs.maxRollback must be at least 1440" }
        UNCONFIRMED_POOL_DEPOSIT_NQT = (if (dp.propertyService.get(Props.DEV_TESTNET)) 50 else 100).toLong()
    }

}// never
