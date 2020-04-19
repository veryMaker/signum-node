package brs.services.impl

import brs.entity.*
import brs.objects.Constants
import brs.services.SubscriptionService
import brs.transaction.appendix.Attachment
import brs.util.BurstException.NotValidException
import brs.util.convert.safeAdd

class SubscriptionServiceImpl(private val dp: DependencyProvider) : SubscriptionService {
    private val subscriptionTable = dp.db.subscriptionStore.subscriptionTable
    private val subscriptionDbKeyFactory = dp.db.subscriptionStore.subscriptionDbKeyFactory

    private val paymentTransactions = mutableListOf<Transaction>()
    private val appliedSubscriptions = mutableListOf<Subscription>()
    private val removeSubscriptions = mutableSetOf<Long>()

    private val fee: Long
        get() = Constants.ONE_BURST

    override fun getSubscription(id: Long?): Subscription? {
        return subscriptionTable[subscriptionDbKeyFactory.newKey(id!!)]
    }

    override fun getSubscriptionsByParticipant(accountId: Long): Collection<Subscription> {
        return dp.db.subscriptionStore.getSubscriptionsByParticipant(accountId)
    }

    override fun getSubscriptionsToId(accountId: Long): Collection<Subscription> {
        return dp.db.subscriptionStore.getSubscriptionsToId(accountId)
    }

    override fun addSubscription(
        sender: Account,
        recipient: Account,
        id: Long,
        amountPlanck: Long,
        startTimestamp: Int,
        frequency: Int
    ) {
        val dbKey = subscriptionDbKeyFactory.newKey(id)
        val subscription = Subscription(
            sender.id,
            recipient.id,
            id,
            amountPlanck,
            frequency,
            startTimestamp + frequency,
            dbKey
        )

        subscriptionTable.insert(subscription)
    }

    override fun applyConfirmed(block: Block, blockchainHeight: Int) {
        paymentTransactions.clear()
        for (subscription in appliedSubscriptions) {
            apply(block, blockchainHeight, subscription)
            subscriptionTable.insert(subscription)
        }
        if (paymentTransactions.isNotEmpty()) {
            dp.db.transactionDb.saveTransactions(paymentTransactions)
        }
        removeSubscriptions.forEach { this.removeSubscription(it) }
    }

    override fun removeSubscription(id: Long) {
        val subscription = subscriptionTable[subscriptionDbKeyFactory.newKey(id)]
        if (subscription != null) {
            subscriptionTable.delete(subscription)
        }
    }

    override fun calculateFees(timestamp: Int): Long {
        var totalFeePlanck: Long = 0
        val appliedUnconfirmedSubscriptions = mutableListOf<Subscription>()
        for (subscription in dp.db.subscriptionStore.getUpdateSubscriptions(timestamp)) {
            if (removeSubscriptions.contains(subscription.id)) {
                continue
            }
            if (applyUnconfirmed(subscription)) {
                appliedUnconfirmedSubscriptions.add(subscription)
            }
        }
        if (appliedUnconfirmedSubscriptions.isNotEmpty()) {
            for (subscription in appliedUnconfirmedSubscriptions) {
                totalFeePlanck = totalFeePlanck.safeAdd(fee)
                undoUnconfirmed(subscription)
            }
        }
        return totalFeePlanck
    }

    override fun clearRemovals() {
        removeSubscriptions.clear()
    }

    override fun addRemoval(id: Long) {
        removeSubscriptions.add(id)
    }

    override fun applyUnconfirmed(timestamp: Int): Long {
        appliedSubscriptions.clear()
        var totalFees: Long = 0
        for (subscription in dp.db.subscriptionStore.getUpdateSubscriptions(timestamp)) {
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

    private fun applyUnconfirmed(subscription: Subscription): Boolean {
        val sender = dp.accountService.getAccount(subscription.senderId)
        val totalAmountPlanck = subscription.amountPlanck.safeAdd(fee)

        if (sender == null || sender.unconfirmedBalancePlanck < totalAmountPlanck) {
            return false
        }

        dp.accountService.addToUnconfirmedBalancePlanck(sender, -totalAmountPlanck)

        return true
    }

    private fun undoUnconfirmed(subscription: Subscription) {
        val sender = dp.accountService.getAccount(subscription.senderId)
        val totalAmountPlanck = subscription.amountPlanck.safeAdd(fee)

        if (sender != null) {
            dp.accountService.addToUnconfirmedBalancePlanck(sender, totalAmountPlanck)
        }
    }

    private fun apply(block: Block, blockchainHeight: Int, subscription: Subscription) {
        val sender = dp.accountService.getAccount(subscription.senderId)!!
        val recipient = dp.accountService.getAccount(subscription.recipientId)!!

        dp.accountService.addToBalancePlanck(sender, -subscription.amountPlanck.safeAdd(fee))
        dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(recipient, subscription.amountPlanck)

        val attachment = Attachment.AdvancedPaymentSubscriptionPayment(dp, subscription.id, blockchainHeight)
        val builder = Transaction.Builder(
            dp,
            1.toByte(),
            sender.publicKey!!,
            subscription.amountPlanck,
            fee,
            subscription.timeNext,
            1440.toShort(),
            attachment
        )

        try {
            builder.senderId(subscription.senderId)
                .recipientId(subscription.recipientId)
                .blockId(block.id)
                .height(block.height)
                .blockTimestamp(block.timestamp)
                .ecBlockHeight(0)
                .ecBlockId(0L)
            val transaction = builder.build()
            if (!dp.db.transactionDb.hasTransaction(transaction.id)) {
                paymentTransactions.add(transaction)
            }
        } catch (e: NotValidException) {
            throw Exception("Failed to build subscription payment transaction", e)
        }

        subscription.timeNextGetAndAdd(subscription.frequency)
    }
}
