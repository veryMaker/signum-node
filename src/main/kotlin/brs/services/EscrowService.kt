package brs.services

import brs.entity.Account
import brs.entity.Block
import brs.entity.Escrow
import brs.entity.Escrow.DecisionType

interface EscrowService {
    /**
     * TODO
     */
    fun getAllEscrowTransactions(): Collection<Escrow>

    /**
     * TODO
     */
    fun isEnabled(): Boolean

    /**
     * TODO
     */
    fun getEscrowTransaction(id: Long?): Escrow?

    /**
     * TODO
     */
    fun getEscrowTransactionsByParticipant(accountId: Long?): Collection<Escrow>

    /**
     * TODO
     */
    fun removeEscrowTransaction(id: Long?)

    /**
     * TODO
     */
    fun updateOnBlock(block: Block, blockchainHeight: Int)

    /**
     * TODO
     */
    fun addEscrowTransaction(
        sender: Account,
        recipient: Account,
        id: Long,
        amountPlanck: Long,
        requiredSigners: Int,
        signers: Collection<Long>,
        deadline: Int,
        deadlineAction: DecisionType
    )

    /**
     * TODO
     */
    fun sign(id: Long, decision: DecisionType, escrow: Escrow)

    /**
     * TODO
     */
    fun checkComplete(escrow: Escrow): DecisionType

    /**
     * TODO
     */
    fun doPayout(result: DecisionType, block: Block, blockchainHeight: Int, escrow: Escrow)

    /**
     * TODO
     */
    fun isIdSigner(id: Long?, escrow: Escrow): Boolean

    /**
     * TODO
     */
    fun saveResultTransaction(
        block: Block,
        escrowId: Long,
        recipientId: Long,
        amountPlanck: Long,
        decision: DecisionType,
        blockchainHeight: Int
    )
}
