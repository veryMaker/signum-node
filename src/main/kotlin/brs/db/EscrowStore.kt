package brs.db

import brs.entity.Escrow
import brs.entity.Transaction

interface EscrowStore {
    /**
     * TODO
     */
    val escrowDbKeyFactory: BurstKey.LongKeyFactory<Escrow>

    /**
     * TODO
     */
    val escrowTable: VersionedEntityTable<Escrow>

    /**
     * TODO
     */
    val decisionDbKeyFactory: BurstKey.LinkKeyFactory<Escrow.Decision>

    /**
     * TODO
     */
    val decisionTable: VersionedEntityTable<Escrow.Decision>

    /**
     * TODO
     */
    val resultTransactions: MutableList<Transaction>

    /**
     * TODO
     */
    fun getEscrowTransactionsByParticipant(accountId: Long): Collection<Escrow>

    /**
     * TODO
     */
    fun getDecisions(id: Long?): Collection<Escrow.Decision>
}
