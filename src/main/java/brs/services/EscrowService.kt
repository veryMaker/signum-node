package brs.services

import brs.Account
import brs.Block
import brs.Escrow
import brs.Escrow.DecisionType

interface EscrowService {
    suspend fun getAllEscrowTransactions(): Collection<Escrow>

    suspend fun isEnabled(): Boolean

    suspend fun getEscrowTransaction(id: Long?): Escrow?

    suspend fun getEscrowTransactionsByParticipant(accountId: Long?): Collection<Escrow>

    suspend fun removeEscrowTransaction(id: Long?)

    suspend fun updateOnBlock(block: Block, blockchainHeight: Int)

    suspend fun addEscrowTransaction(sender: Account, recipient: Account, id: Long, amountNQT: Long, requiredSigners: Int, signers: Collection<Long>, deadline: Int, deadlineAction: DecisionType)

    suspend fun sign(id: Long, decision: DecisionType, escrow: Escrow)

    suspend fun checkComplete(escrow: Escrow): DecisionType

    suspend fun doPayout(result: DecisionType, block: Block, blockchainHeight: Int, escrow: Escrow)

    suspend fun isIdSigner(id: Long?, escrow: Escrow): Boolean

    suspend fun saveResultTransaction(block: Block, escrowId: Long, recipientId: Long, amountNQT: Long, decision: DecisionType, blockchainHeight: Int)
}
