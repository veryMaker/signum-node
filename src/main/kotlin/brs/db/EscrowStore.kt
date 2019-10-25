package brs.db

import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.entity.Escrow
import brs.entity.Transaction

interface EscrowStore {

    val escrowDbKeyFactory: BurstKey.LongKeyFactory<Escrow>

    val escrowTable: VersionedEntityTable<Escrow>

    val decisionDbKeyFactory: BurstKey.LinkKeyFactory<Escrow.Decision>

    val decisionTable: VersionedEntityTable<Escrow.Decision>

    val resultTransactions: MutableList<Transaction>

    fun getEscrowTransactionsByParticipant(accountId: Long?): Collection<Escrow>

    fun getDecisions(id: Long?): Collection<Escrow.Decision>
}
