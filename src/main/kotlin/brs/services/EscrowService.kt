package brs.services

import brs.entity.Account
import brs.entity.Block
import brs.entity.Escrow
import brs.entity.Escrow.DecisionType

interface EscrowService {
    fun getAllEscrowTransactions(): Collection<Escrow>

    fun isEnabled(): Boolean

    fun getEscrowTransaction(id: Long?): Escrow?

    fun getEscrowTransactionsByParticipant(accountId: Long?): Collection<Escrow>

    fun removeEscrowTransaction(id: Long?)

    fun updateOnBlock(block: Block, blockchainHeight: Int)

    fun addEscrowTransaction(sender: Account, recipient: Account, id: Long, amountPlanck: Long, requiredSigners: Int, signers: Collection<Long>, deadline: Int, deadlineAction: DecisionType)

    fun sign(id: Long, decision: DecisionType, escrow: Escrow)

    fun checkComplete(escrow: Escrow): DecisionType

    fun doPayout(result: DecisionType, block: Block, blockchainHeight: Int, escrow: Escrow)

    fun isIdSigner(id: Long?, escrow: Escrow): Boolean

    fun saveResultTransaction(block: Block, escrowId: Long, recipientId: Long, amountPlanck: Long, decision: DecisionType, blockchainHeight: Int)
}
