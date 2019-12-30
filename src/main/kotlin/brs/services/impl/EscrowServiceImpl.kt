package brs.services.impl

import brs.entity.*
import brs.entity.Escrow.Decision
import brs.entity.Escrow.DecisionType
import brs.objects.Genesis
import brs.schema.Tables.ESCROW
import brs.services.EscrowService
import brs.transaction.appendix.Attachment
import org.jooq.Condition
import java.util.concurrent.ConcurrentSkipListSet

class EscrowServiceImpl(private val dp: DependencyProvider) : EscrowService {
    private val escrowTable = dp.escrowStore.escrowTable
    private val escrowDbKeyFactory = dp.escrowStore.escrowDbKeyFactory
    private val decisionTable = dp.escrowStore.decisionTable
    private val decisionDbKeyFactory = dp.escrowStore.decisionDbKeyFactory
    private val resultTransactions = dp.escrowStore.resultTransactions

    override fun getAllEscrowTransactions() = escrowTable.getAll(0, -1)

    private val updatedEscrowIds = ConcurrentSkipListSet<Long>()

    override fun getEscrowTransaction(id: Long): Escrow? {
        return escrowTable[escrowDbKeyFactory.newKey(id)]
    }

    override fun getEscrowTransactionsByParticipant(accountId: Long): Collection<Escrow> {
        return dp.escrowStore.getEscrowTransactionsByParticipant(accountId)
    }

    override fun removeEscrowTransaction(id: Long) {
        val escrow = escrowTable[escrowDbKeyFactory.newKey(id)] ?: return
        escrow.decisions.forEach { decisionTable.delete(it) }
        escrowTable.delete(escrow)
    }


    override fun addEscrowTransaction(
        sender: Account,
        recipient: Account,
        id: Long,
        amountPlanck: Long,
        requiredSigners: Int,
        signers: Collection<Long>,
        deadline: Int,
        deadlineAction: DecisionType
    ) {
        val dbKey = escrowDbKeyFactory.newKey(id)
        val newEscrowTransaction =
            Escrow(dp, dbKey, sender, recipient, id, amountPlanck, requiredSigners, deadline, deadlineAction)
        escrowTable.insert(newEscrowTransaction)
        val senderDbKey = decisionDbKeyFactory.newKey(id, sender.id)
        val senderDecision = Decision(senderDbKey, id, sender.id, DecisionType.UNDECIDED)
        decisionTable.insert(senderDecision)
        val recipientDbKey = decisionDbKeyFactory.newKey(id, recipient.id)
        val recipientDecision = Decision(recipientDbKey, id, recipient.id, DecisionType.UNDECIDED)
        decisionTable.insert(recipientDecision)
        for (signer in signers) {
            val signerDbKey = decisionDbKeyFactory.newKey(id, signer)
            val decision = Decision(signerDbKey, id, signer, DecisionType.UNDECIDED)
            decisionTable.insert(decision)
        }
    }

    override fun sign(id: Long, decision: DecisionType, escrow: Escrow) {
        if (id == escrow.senderId && decision != DecisionType.RELEASE) {
            return
        }

        if (id == escrow.recipientId && decision != DecisionType.REFUND) {
            return
        }

        val decisionChange = decisionTable[decisionDbKeyFactory.newKey(escrow.id, id)] ?: return
        decisionChange.decision = decision

        decisionTable.insert(decisionChange)

        updatedEscrowIds.add(escrow.id)
    }

    override fun checkComplete(escrow: Escrow): DecisionType {
        val senderDecision = decisionTable[decisionDbKeyFactory.newKey(escrow.id, escrow.senderId)]!!
        if (senderDecision.decision == DecisionType.RELEASE) {
            return DecisionType.RELEASE
        }
        val recipientDecision = decisionTable[decisionDbKeyFactory.newKey(escrow.id, escrow.recipientId)]!!
        if (recipientDecision.decision == DecisionType.REFUND) {
            return DecisionType.REFUND
        }

        var countRelease = 0
        var countRefund = 0
        var countSplit = 0

        for (decision in dp.escrowStore.getDecisions(escrow.id)) {
            if (decision.accountId == escrow.senderId || decision.accountId == escrow.recipientId) {
                continue
            }
            when (decision.decision) {
                DecisionType.RELEASE -> countRelease++
                DecisionType.REFUND -> countRefund++
                DecisionType.SPLIT -> countSplit++
                else -> {
                }
            }
        }

        if (countRelease >= escrow.requiredSigners) {
            return DecisionType.RELEASE
        }
        if (countRefund >= escrow.requiredSigners) {
            return DecisionType.REFUND
        }
        return if (countSplit >= escrow.requiredSigners) {
            DecisionType.SPLIT
        } else DecisionType.UNDECIDED

    }

    private fun getUpdateOnBlockClause(timestamp: Int): Condition {
        return ESCROW.DEADLINE.lt(timestamp)
    }

    override fun updateOnBlock(block: Block, blockchainHeight: Int) {
        resultTransactions.clear()

        escrowTable.getManyBy(getUpdateOnBlockClause(block.timestamp), 0, -1)
            .forEach { escrow -> updatedEscrowIds.add(escrow.id) }

        if (!updatedEscrowIds.isEmpty()) {
            for (escrowId in updatedEscrowIds) {
                val escrow = escrowTable[escrowDbKeyFactory.newKey(escrowId!!)]!!
                var result: DecisionType = checkComplete(escrow)
                if (result != DecisionType.UNDECIDED || escrow.deadline < block.timestamp) {
                    if (result == DecisionType.UNDECIDED) {
                        result = escrow.deadlineAction
                    }
                    doPayout(result, block, blockchainHeight, escrow)

                    removeEscrowTransaction(escrowId)
                }
            }
            if (resultTransactions.isNotEmpty()) {
                dp.transactionDb.saveTransactions(resultTransactions)
            }
            updatedEscrowIds.clear()
        }
    }

    override fun doPayout(result: DecisionType, block: Block, blockchainHeight: Int, escrow: Escrow) {
        when (result) {
            DecisionType.RELEASE -> {
                dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(
                    dp.accountService.getAccount(escrow.recipientId)!!,
                    escrow.amountPlanck
                )
                saveResultTransaction(
                    block,
                    escrow.id,
                    escrow.recipientId,
                    escrow.amountPlanck,
                    DecisionType.RELEASE,
                    blockchainHeight
                )
            }
            DecisionType.REFUND -> {
                dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(
                    dp.accountService.getAccount(escrow.senderId)!!,
                    escrow.amountPlanck
                )
                saveResultTransaction(
                    block,
                    escrow.id,
                    escrow.senderId,
                    escrow.amountPlanck,
                    DecisionType.REFUND,
                    blockchainHeight
                )
            }
            DecisionType.SPLIT -> {
                val halfAmountPlanck = escrow.amountPlanck / 2
                dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(
                    dp.accountService.getAccount(escrow.recipientId)!!,
                    halfAmountPlanck
                )
                dp.accountService.addToBalanceAndUnconfirmedBalancePlanck(
                    dp.accountService.getAccount(escrow.senderId)!!,
                    escrow.amountPlanck - halfAmountPlanck
                )
                saveResultTransaction(
                    block,
                    escrow.id,
                    escrow.recipientId,
                    halfAmountPlanck,
                    DecisionType.SPLIT,
                    blockchainHeight
                )
                saveResultTransaction(
                    block,
                    escrow.id,
                    escrow.senderId,
                    escrow.amountPlanck - halfAmountPlanck,
                    DecisionType.SPLIT,
                    blockchainHeight
                )
            }
            DecisionType.UNDECIDED -> {
                // Do nothing
            }
        }
    }

    override fun isIdSigner(id: Long?, escrow: Escrow): Boolean {
        return decisionTable[decisionDbKeyFactory.newKey(escrow.id, id!!)] != null
    }

    override fun saveResultTransaction(
        block: Block,
        escrowId: Long,
        recipientId: Long,
        amountPlanck: Long,
        decision: DecisionType,
        blockchainHeight: Int
    ) {
        val attachment = Attachment.AdvancedPaymentEscrowResult(dp, escrowId, decision, blockchainHeight)
        val builder = Transaction.Builder(
            dp,
            1.toByte(),
            Genesis.creatorPublicKey,
            amountPlanck,
            0L,
            block.timestamp,
            1440.toShort(),
            attachment
        )
        builder.senderId(0L)
            .recipientId(recipientId)
            .blockId(block.id)
            .height(block.height)
            .blockTimestamp(block.timestamp)
            .ecBlockHeight(0)
            .ecBlockId(0L)

        val transaction: Transaction
        transaction = builder.build()

        if (!dp.transactionDb.hasTransaction(transaction.id)) {
            resultTransactions.add(transaction)
        }
    }
}
