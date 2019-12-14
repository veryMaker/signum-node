/*
 * Some portion .. Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file LICENSE.txt
*/

package brs.at

import brs.db.BurstKey
import brs.entity.Block
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.objects.Genesis
import brs.transaction.appendix.Appendix
import brs.transaction.appendix.Attachment
import brs.util.BurstException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class AT : AtMachineState {
    val dbKey: BurstKey
    val name: String
    val description: String
    private val nextHeight: Int

    private constructor(
        dp: DependencyProvider,
        atId: Long,
        creator: Long,
        name: String,
        description: String,
        creationBytes: ByteArray,
        height: Int
    ) : super(dp, atId, creator, creationBytes, height) {
        this.name = name
        this.description = description
        dbKey = dp.atStore.atDbKeyFactory.newKey(atId)
        this.nextHeight = dp.blockchainService.height
    }

    constructor(
        dp: DependencyProvider, atId: Long, creator: Long, name: String, description: String, version: Short,
        stateBytes: ByteArray, csize: Int, dsize: Int, cUserStackBytes: Int, cCallStackBytes: Int,
        creationBlockHeight: Int, sleepBetween: Int, nextHeight: Int,
        freezeWhenSameBalance: Boolean, minActivationAmount: Long, apCode: ByteArray
    ) : super(
        dp, atId, creator, version,
        stateBytes, csize, dsize, cUserStackBytes, cCallStackBytes,
        creationBlockHeight, sleepBetween,
        freezeWhenSameBalance, minActivationAmount, apCode
    ) {
        this.name = name
        this.description = description
        dbKey = dp.atStore.atDbKeyFactory.newKey(atId)
        this.nextHeight = nextHeight
    }

    fun saveState() {
        var state: ATState? = dp.atStore.atStateTable[dp.atStore.atStateDbKeyFactory.newKey(id)]
        val prevHeight = dp.blockchainService.height
        val newNextHeight = prevHeight + waitForNumberOfBlocks
        if (state != null) {
            state.state = this.state
            state.prevHeight = prevHeight
            state.nextHeight = newNextHeight
            state.sleepBetween = this.sleepBetween
            state.prevBalance = getpBalance()
            state.freezeWhenSameBalance = freezeOnSameBalance()
            state.minActivationAmount = minActivationAmount()
        } else {
            state = ATState(
                dp, id,
                this.state, newNextHeight, sleepBetween,
                getpBalance(), freezeOnSameBalance(), minActivationAmount()
            )
        }
        dp.atStore.atStateTable.insert(state)
    }

    fun nextHeight(): Int {
        return nextHeight
    }

    open class ATState(
        dp: DependencyProvider,
        val atId: Long,
        var state: ByteArray,
        var nextHeight: Int,
        var sleepBetween: Int,
        var prevBalance: Long,
        var freezeWhenSameBalance: Boolean,
        var minActivationAmount: Long
    ) {
        val dbKey = dp.atStore.atStateDbKeyFactory.newKey(this.atId)
        var prevHeight: Int = 0
    }

    companion object {
        private val pendingFees = mutableMapOf<Long, Long>()
        private val pendingTransactions = mutableListOf<AtTransaction>()

        fun clearPendingFees() {
            pendingFees.clear()
        }

        fun clearPendingTransactions() {
            pendingTransactions.clear()
        }

        fun addPendingFee(id: Long, fee: Long) {
            pendingFees[id] = fee
        }

        fun addPendingFee(id: ByteArray, fee: Long) {
            addPendingFee(AtApiHelper.getLong(id), fee)
        }

        fun addPendingTransaction(atTransaction: AtTransaction) {
            pendingTransactions.add(atTransaction)
        }

        fun findPendingTransaction(recipientId: Long): Boolean {
            for (tx in pendingTransactions) {
                if (recipientId == tx.recipientId) {
                    return true
                }
            }
            return false
        }

        fun addAT(
            dp: DependencyProvider,
            atId: Long,
            creator: Long,
            name: String,
            description: String,
            creationBytes: ByteArray,
            height: Int
        ) {
            val at = AT(dp, atId, creator, name, description, creationBytes, height)

            dp.atController.resetMachine(at)

            dp.atStore.atTable.insert(at)

            at.saveState()

            val account = dp.accountService.getOrAddAccount(atId)
            account.apply(dp, ByteArray(32), height)
        }

        fun compressState(stateBytes: ByteArray?): ByteArray? {
            if (stateBytes == null || stateBytes.isEmpty()) {
                return null
            }

            ByteArrayOutputStream().use { bos ->
                GZIPOutputStream(bos).use { gzip ->
                    gzip.write(stateBytes)
                    gzip.flush()
                }
                return bos.toByteArray()
            }
        }

        fun decompressState(stateBytes: ByteArray?): ByteArray? {
            if (stateBytes == null || stateBytes.isEmpty()) {
                return null
            }

            ByteArrayInputStream(stateBytes).use { bis ->
                GZIPInputStream(bis).use { gzip ->
                    ByteArrayOutputStream().use { bos ->
                        gzip.copyTo(bos, 256)
                        return bos.toByteArray()
                    }
                }
            }
        }

        fun handleATBlockTransactionsListener(dp: DependencyProvider): (Block) -> Unit = { block ->
            pendingFees.forEach { (key, value) ->
                val atAccount = dp.accountService.getAccount(key)!!
                dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(atAccount, -value)
            }

            val transactions = mutableListOf<Transaction>()
            for ((senderId, recipientId, amount, message) in pendingTransactions) {
                dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(
                    dp.accountService.getAccount(senderId)!!,
                    -amount
                )
                dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(
                    dp.accountService.getOrAddAccount(recipientId),
                    amount
                )

                val builder = Transaction.Builder(
                    dp, 1.toByte(), Genesis.creatorPublicKey,
                    amount, 0L, block.timestamp, 1440.toShort(), Attachment.AtPayment(dp)
                )

                builder.senderId(senderId)
                    .recipientId(recipientId)
                    .blockId(block.id)
                    .height(block.height)
                    .blockTimestamp(block.timestamp)
                    .ecBlockHeight(0)
                    .ecBlockId(0L)

                if (message != null) {
                    builder.message(Appendix.Message(dp, message, dp.blockchainService.height))
                }

                try {
                    val transaction = builder.build()
                    if (!dp.transactionDb.hasTransaction(transaction.id)) {
                        transactions.add(transaction)
                    }
                } catch (e: BurstException.NotValidException) {
                    throw Exception("Failed to construct AT payment transaction", e)
                }

            }

            if (transactions.isNotEmpty()) {
                // WATCH: Replace after transactions are converted!
                dp.transactionDb.saveTransactions(transactions)
            }
        }
    }
}
