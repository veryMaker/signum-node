/*
 * Some portion .. Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file LICENSE.txt
*/

package brs.at

import brs.*
import brs.entity.Account
import brs.entity.Block
import brs.objects.Genesis
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.entity.Transaction
import brs.transaction.appendix.Appendix
import brs.transaction.appendix.Attachment
import brs.util.BurstException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class AT : AtMachineState {
    val dbKey: BurstKey
    val name: String
    val description: String
    private val nextHeight: Int

    private constructor(dp: DependencyProvider, atId: ByteArray, creator: ByteArray, name: String, description: String, creationBytes: ByteArray, height: Int) : super(dp, atId, creator, creationBytes, height) {
        this.name = name
        this.description = description
        dbKey = atDbKeyFactory(dp).newKey(AtApiHelper.getLong(atId))
        this.nextHeight = dp.blockchainService.height
    }

    constructor(dp: DependencyProvider, atId: ByteArray, creator: ByteArray, name: String, description: String, version: Short,
                stateBytes: ByteArray, csize: Int, dsize: Int, cUserStackBytes: Int, cCallStackBytes: Int,
                creationBlockHeight: Int, sleepBetween: Int, nextHeight: Int,
                freezeWhenSameBalance: Boolean, minActivationAmount: Long, apCode: ByteArray) : super(dp, atId, creator, version,
            stateBytes, csize, dsize, cUserStackBytes, cCallStackBytes,
            creationBlockHeight, sleepBetween,
            freezeWhenSameBalance, minActivationAmount, apCode) {
        this.name = name
        this.description = description
        dbKey = atDbKeyFactory(dp).newKey(AtApiHelper.getLong(atId))
        this.nextHeight = nextHeight
    }

    private fun atStateTable(): VersionedEntityTable<ATState> {
        return dp.atStore.atStateTable
    }

    fun saveState() {
        var state: ATState? = atStateTable()[atStateDbKeyFactory(dp).newKey(AtApiHelper.getLong(this.id!!))]
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
            state = ATState(dp, AtApiHelper.getLong(this.id!!),
                    this.state, newNextHeight, sleepBetween,
                    getpBalance(), freezeOnSameBalance(), minActivationAmount())
        }
        atStateTable().insert(state)
    }

    fun nextHeight(): Int {
        return nextHeight
    }

    open class ATState(dp: DependencyProvider, val atId: Long, var state: ByteArray, var nextHeight: Int, var sleepBetween: Int, var prevBalance: Long, var freezeWhenSameBalance: Boolean, var minActivationAmount: Long) {
        val dbKey = atStateDbKeyFactory(dp).newKey(this.atId)
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

        fun findPendingTransaction(recipientId: ByteArray): Boolean {
            for (tx in pendingTransactions) {
                if (recipientId.contentEquals(tx.recipientId)) {
                    return true
                }
            }
            return false
        }

        // TODO stop passing dp around, fix this code to be organized properly!!!

        @Deprecated("Use dp.atStore.atDbKeyFactory instead")
        private fun atDbKeyFactory(dp: DependencyProvider): BurstKey.LongKeyFactory<AT> {
            return dp.atStore.atDbKeyFactory
        }

        @Deprecated("Use dp.atStore.atTable instead")
        private fun atTable(dp: DependencyProvider): VersionedEntityTable<AT> {
            return dp.atStore.atTable
        }

        @Deprecated("Use dp.atStore.atStateDbKeyFactory instead")
        private fun atStateDbKeyFactory(dp: DependencyProvider): BurstKey.LongKeyFactory<ATState> {
            return dp.atStore.atStateDbKeyFactory
        }

        @Deprecated("Use dp.atStore.getAT(AtApiHelper.getLong(id)) instead")
        fun getAT(dp: DependencyProvider, id: ByteArray): AT? {
            return getAT(dp, AtApiHelper.getLong(id))
        }

        @Deprecated("Use dp.atStore.getAT(id) instead")
        fun getAT(dp: DependencyProvider, id: Long?): AT? {
            return dp.atStore.getAT(id)
        }

        fun addAT(dp: DependencyProvider, atId: Long?, senderAccountId: Long?, name: String, description: String, creationBytes: ByteArray, height: Int) {
            val bf = ByteBuffer.allocate(8 + 8)
            bf.order(ByteOrder.LITTLE_ENDIAN)

            bf.putLong(atId!!)

            val id = ByteArray(8)

            bf.putLong(8, senderAccountId!!)

            val creator = ByteArray(8)
            bf.clear()
            bf.get(id, 0, 8)
            bf.get(creator, 0, 8)

            val at = AT(dp, id, creator, name, description, creationBytes, height)

            dp.atController.resetMachine(at)

            atTable(dp).insert(at)

            at.saveState()

            val account = Account.getOrAddAccount(dp, atId)
            account.apply(dp, ByteArray(32), height)
        }

        // TODO just do it yourself! or add a utils class or something... same goes for all of the methods around here doing this
        @Deprecated("Use dp.atStore.getOrderedATs() instead")
        fun getOrderedATs(dp: DependencyProvider): List<Long> {
            return dp.atStore.getOrderedATs()
        }

        fun compressState(stateBytes: ByteArray?): ByteArray? {
            if (stateBytes == null || stateBytes.isEmpty()) {
                return null
            }

            try {
                ByteArrayOutputStream().use { bos ->
                    GZIPOutputStream(bos).use { gzip ->
                        gzip.write(stateBytes)
                        gzip.flush()
                    }
                    return bos.toByteArray()
                }
            } catch (e: IOException) {
                throw RuntimeException(e.message, e)
            }

        }

        fun decompressState(stateBytes: ByteArray?): ByteArray? {
            if (stateBytes == null || stateBytes.isEmpty()) {
                return null
            }

            try {
                ByteArrayInputStream(stateBytes).use { bis ->
                    GZIPInputStream(bis).use { gzip ->
                        ByteArrayOutputStream().use { bos ->
                            gzip.copyTo(bos, 256)
                            return bos.toByteArray()
                        }
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e.message, e)
            }
        }

        fun handleATBlockTransactionsListener(dp: DependencyProvider): (Block) -> Unit = { block ->
            pendingFees.forEach { (key, value) ->
                val atAccount = dp.accountService.getAccount(key)!!
                dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(atAccount, -value)
            }

            val transactions = mutableListOf<Transaction>()
            for (atTransaction in pendingTransactions) {
                dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(dp.accountService.getAccount(AtApiHelper.getLong(atTransaction.senderId))!!, -atTransaction.amount)
                dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(dp.accountService.getOrAddAccount(AtApiHelper.getLong(atTransaction.recipientId)),
                    atTransaction.amount
                )

                val builder = Transaction.Builder(dp, 1.toByte(), Genesis.creatorPublicKey,
                    atTransaction.amount, 0L, block.timestamp, 1440.toShort(), Attachment.AtPayment(dp))

                builder.senderId(AtApiHelper.getLong(atTransaction.senderId))
                    .recipientId(AtApiHelper.getLong(atTransaction.recipientId))
                    .blockId(block.id)
                    .height(block.height)
                    .blockTimestamp(block.timestamp)
                    .ecBlockHeight(0)
                    .ecBlockId(0L)

                val message = atTransaction.message
                if (message != null) {
                    builder.message(Appendix.Message(dp, message, dp.blockchainService.height))
                }

                try {
                    val transaction = builder.build()
                    if (!dp.transactionDb.hasTransaction(transaction.id)) {
                        transactions.add(transaction)
                    }
                } catch (e: BurstException.NotValidException) {
                    throw RuntimeException("Failed to construct AT payment transaction", e)
                }

            }

            if (transactions.isNotEmpty()) {
                // WATCH: Replace after transactions are converted!
                dp.transactionDb.saveTransactions(transactions)
            }
        }
    }
}
