package brs

import brs.util.Convert

import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.util.*

object VerifyTrace {
    private val balanceHeaders = listOf("balance", "unconfirmed balance")
    private val deltaHeaders = listOf("transaction amount", "transaction fee",
            "generation fee", "trade cost", "purchase cost", "discount", "refund")
    private val assetQuantityHeaders = listOf("asset balance", "unconfirmed asset balance")
    private val deltaAssetQuantityHeaders = listOf("asset quantity", "trade quantity")

    private val BEGIN_QUOTE = "^" + DebugTrace.QUOTE
    private val END_QUOTE = DebugTrace.QUOTE + "$"

    private fun isBalance(header: String): Boolean {
        return balanceHeaders.contains(header)
    }

    private fun isDelta(header: String): Boolean {
        return deltaHeaders.contains(header)
    }

    private fun isAssetQuantity(header: String): Boolean {
        return assetQuantityHeaders.contains(header)
    }

    private fun isDeltaAssetQuantity(header: String): Boolean {
        return deltaAssetQuantityHeaders.contains(header)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val fileName = if (args.size == 1) args[0] else "nxt-trace.csv"
        try {
            BufferedReader(FileReader(fileName)).use { reader ->
                val firstLine = reader.readLine()
                val headers = unquote(firstLine.split(DebugTrace.SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

                val totals = mutableMapOf<String, MutableMap<String, Long>>()
                val accountAssetTotals = mutableMapOf<String, MutableMap<String, MutableMap<String, Long>>>()
                val issuedAssetQuantities = mutableMapOf<String, Long>()
                val accountAssetQuantities = mutableMapOf<String, Long>()

                reader.readLines().forEach { line ->
                    val values = unquote(line.split(DebugTrace.SEPARATOR.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                    val valueMap = mutableMapOf<String, String>()
                    for (i in headers.indices) {
                        valueMap[headers[i]] = values[i]
                    }
                    val accountId = valueMap["account"]!!
                    val accountTotals = totals.computeIfAbsent(accountId) { mutableMapOf() }
                    val accountAssetMap = accountAssetTotals.computeIfAbsent(accountId) { mutableMapOf() }
                    if ("asset issuance" == valueMap["event"]) {
                        val assetId = valueMap["asset"]!!
                        issuedAssetQuantities[assetId] = valueMap["asset quantity"]!!.toLong()
                    }
                    for ((header, value) in valueMap) {
                        if (value.trim { it <= ' ' }.isEmpty()) {
                            continue
                        }
                        when {
                            isBalance(header) -> accountTotals[header] = java.lang.Long.parseLong(value)
                            isDelta(header) -> {
                                accountTotals[header] = Convert.safeAdd(nullToZero(accountTotals[header]), value.toLong())
                            }
                            isAssetQuantity(header) -> {
                                val assetId = valueMap["asset"]!!
                                val assetTotals = accountAssetMap.computeIfAbsent(assetId) { mutableMapOf() }
                                assetTotals[header] = java.lang.Long.parseLong(value)
                            }
                            isDeltaAssetQuantity(header) -> {
                                val assetId = valueMap["asset"]!!
                                val assetTotals = accountAssetMap.computeIfAbsent(assetId) { mutableMapOf() }
                                val previousValue = nullToZero(assetTotals[header])
                                assetTotals[header] = Convert.safeAdd(previousValue, java.lang.Long.parseLong(value))
                            }
                        }
                    }
                }

                val failed = HashSet<String>()
                for ((accountId, accountValues) in totals) {
                    println("account: $accountId")
                    balanceHeaders.forEach { balanceHeader -> println(balanceHeader + ": " + nullToZero(accountValues[balanceHeader])) }
                    println("totals:")
                    var totalDelta: Long = 0
                    for (header in deltaHeaders) {
                        val delta = nullToZero(accountValues[header])
                        totalDelta = Convert.safeAdd(totalDelta, delta)
                        println("$header: $delta")
                    }
                    println("total confirmed balance change: $totalDelta")
                    val balance = nullToZero(accountValues["balance"])
                    if (balance != totalDelta) {
                        println("ERROR: balance doesn't match total change!!!")
                        failed.add(accountId)
                    }
                    val accountAssetMap = accountAssetTotals[accountId]!!
                    accountAssetMap.forEach { (assetId, assetValues) ->
                        println("asset: $assetId")
                        assetValues.forEach { (key, value) -> println("$key: $value") }
                        var totalAssetDelta: Long = 0
                        for (header in deltaAssetQuantityHeaders) {
                            val delta = nullToZero(assetValues[header])
                            totalAssetDelta = Convert.safeAdd(totalAssetDelta, delta)
                        }
                        println("total confirmed asset quantity change: $totalAssetDelta")
                        val assetBalance = assetValues["asset balance"]!!
                        if (assetBalance != totalAssetDelta) {
                            println("ERROR: asset balance doesn't match total asset quantity change!!!")
                            failed.add(accountId)
                        }
                        val previousAssetQuantity = nullToZero(accountAssetQuantities[assetId])
                        accountAssetQuantities[assetId] = Convert.safeAdd(previousAssetQuantity, assetBalance)
                    }
                    println()
                }
                val failedAssets = HashSet<String>()
                issuedAssetQuantities.forEach { (assetId, value) ->
                    if (value != nullToZero(accountAssetQuantities[assetId])) {
                        println("ERROR: asset " + assetId + " balances don't match, issued: "
                                + value
                                + ", total of account balances: " + accountAssetQuantities[assetId])
                        failedAssets.add(assetId)
                    }
                }
                if (failed.size > 0) {
                    println("ERROR: " + failed.size + " accounts have incorrect balances")
                    println(failed)
                } else {
                    println("SUCCESS: all " + totals.size + " account balances and asset balances match the transaction and trade totals!")
                }
                if (failedAssets.size > 0) {
                    println("ERROR: " + failedAssets.size + " assets have incorrect balances")
                    println(failedAssets)
                } else {
                    println("SUCCESS: all " + issuedAssetQuantities.size + " assets quantities are correct!")
                }

            }
        } catch (e: IOException) {
            println(e.toString())
            throw RuntimeException(e)
        }

    }

    private fun unquote(values: Array<String>): Array<String> {
        return values
                .map { it.replaceFirst(BEGIN_QUOTE.toRegex(), "").replaceFirst(END_QUOTE.toRegex(), "") }
                .toTypedArray()
    }

    private fun nullToZero(l: Long?): Long {
        return l ?: 0
    }

}
