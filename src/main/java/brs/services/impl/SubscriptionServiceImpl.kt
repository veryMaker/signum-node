package brs.services.impl

import brs.*
import brs.BurstException.NotValidException
import brs.services.SubscriptionService
import brs.util.convert.safeAdd

class SubscriptionServiceImpl(private val dp: DependencyProvider) : SubscriptionService {
    private val subscriptionTable = dp.subscriptionStore.subscriptionTable
    private val subscriptionDbKeyFactory = dp.subscriptionStore.subscriptionDbKeyFactory

    override suspend fun isEnabled(): Boolean {
            if (dp.blockchain.lastBlock.height >= Constants.BURST_SUBSCRIPTION_START_BLOCK) {
                return true
            }

            val subscriptionEnabled = dp.aliasService.getAlias("featuresubscription")
            return subscriptionEnabled != null && subscriptionEnabled.aliasURI == "enabled"
        }

    private val fee: Long
        get() = Constants.ONE_BURST

    override suspend fun getSubscription(id: Long?): Subscription? {
        return subscriptionTable.get(subscriptionDbKeyFactory.newKey(id!!))
    }

    override suspend fun getSubscriptionsByParticipant(accountId: Long?): Collection<Subscription> {
        return dp.subscriptionStore.getSubscriptionsByParticipant(accountId)
    }

    override suspend fun getSubscriptionsToId(accountId: Long?): Collection<Subscription> {
        return dp.subscriptionStore.getSubscriptionsToId(accountId)
    }

    override suspend fun addSubscription(sender: Account, recipient: Account, id: Long, amountNQT: Long, startTimestamp: Int, frequency: Int) {
        val dbKey = subscriptionDbKeyFactory.newKey(id)
        val subscription = Subscription(sender.id, recipient.id, id, amountNQT, frequency, startTimestamp + frequency, dbKey)

        subscriptionTable.insert(subscription)
    }

    override suspend fun applyConfirmed(block: Block, blockchainHeight: Int) {
        paymentTransactions.clear()
        for (subscription in appliedSubscriptions) {
            apply(block, blockchainHeight, subscription)
            subscriptionTable.insert(subscription)
        }
        if (paymentTransactions.isNotEmpty()) {
            dp.transactionDb.saveTransactions(paymentTransactions)
        }
        removeSubscriptions.forEach { this.removeSubscription(it) }
    }

    override suspend fun removeSubscription(id: Long) {
        val subscription = subscriptionTable.get(subscriptionDbKeyFactory.newKey(id))
        if (subscription != null) {
            subscriptionTable.delete(subscription)
        }
    }

    override suspend fun calculateFees(timestamp: Int): Long {
        var totalFeeNQT: Long = 0
        val appliedUnconfirmedSubscriptions = mutableListOf<Subscription>()
        for (subscription in dp.subscriptionStore.getUpdateSubscriptions(timestamp)) {
            if (removeSubscriptions.contains(subscription.id)) {
                continue
            }
            if (applyUnconfirmed(subscription)) {
                appliedUnconfirmedSubscriptions.add(subscription)
            }
        }
        if (appliedUnconfirmedSubscriptions.isNotEmpty()) {
            for (subscription in appliedUnconfirmedSubscriptions) {
                totalFeeNQT = totalFeeNQT.safeAdd(fee)
                undoUnconfirmed(subscription)
            }
        }
        return totalFeeNQT
    }

    override fun clearRemovals() {
        removeSubscriptions.clear()
    }

    override fun addRemoval(id: Long) {
        removeSubscriptions.add(id)
    }

    override suspend fun applyUnconfirmed(timestamp: Int): Long {
        appliedSubscriptions.clear()
        var totalFees: Long = 0
        for (subscription in dp.subscriptionStore.getUpdateSubscriptions(timestamp)) {
            if (removeSubscriptions.contains(subscription.id)) {
                continue
            }
            if (applyUnconfirmed(subscription)) {
                appliedSubscriptions.add(subscription)
                totalFees += fee
            } else {
                removeSubscriptions.add(subscription.id)
            }
        }
        return totalFees
    }

    private suspend fun applyUnconfirmed(subscription: Subscription): Boolean {
        val sender = dp.accountService.getAccount(subscription.senderId)
        val totalAmountNQT = subscription.amountNQT.safeAdd(fee)

        if (sender == null || sender.unconfirmedBalanceNQT < totalAmountNQT) {
            return false
        }

        dp.accountService.addToUnconfirmedBalanceNQT(sender, -totalAmountNQT)

        return true
    }

    private suspend fun undoUnconfirmed(subscription: Subscription) {
        val sender = dp.accountService.getAccount(subscription.senderId)
        val totalAmountNQT = subscription.amountNQT.safeAdd(fee)

        if (sender != null) {
            dp.accountService.addToUnconfirmedBalanceNQT(sender, totalAmountNQT)
        }
    }

    private suspend fun apply(block: Block, blockchainHeight: Int, subscription: Subscription) {
        val sender = dp.accountService.getAccount(subscription.senderId)!!
        val recipient = dp.accountService.getAccount(subscription.recipientId)!!

        val totalAmountNQT = subscription.amountNQT.safeAdd(fee)

        dp.accountService.addToBalanceNQT(sender, -totalAmountNQT)
        dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(recipient, subscription.amountNQT)

        val attachment = Attachment.AdvancedPaymentSubscriptionPayment(dp, subscription.id, blockchainHeight)
        val builder = Transaction.Builder(dp, 1.toByte(), sender.publicKey!!, subscription.amountNQT, fee, subscription.timeNext, 1440.toShort(), attachment)

        try {
            builder.senderId(subscription.senderId)
                    .recipientId(subscription.recipientId)
                    .blockId(block.id)
                    .height(block.height)
                    .blockTimestamp(block.timestamp)
                    .ecBlockHeight(0)
                    .ecBlockId(0L)
            val transaction = builder.build()
            if (!dp.transactionDb.hasTransaction(transaction.id)) {
                paymentTransactions.add(transaction)
            }
        } catch (e: NotValidException) {
            throw RuntimeException("Failed to build subscription payment transaction", e)
        }

        subscription.timeNextGetAndAdd(subscription.frequency)
    }

    companion object { // TODO why is this static??
        private val paymentTransactions = mutableListOf<Transaction>()
        private val appliedSubscriptions = mutableListOf<Subscription>()
        private val removeSubscriptions = mutableSetOf<Long>()
    }

}
