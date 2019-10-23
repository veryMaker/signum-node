package brs.db.store

import brs.Escrow
import brs.Transaction
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.db.sql.DbKey

interface EscrowStore {

    val escrowDbKeyFactory: BurstKey.LongKeyFactory<Escrow>

    val escrowTable: VersionedEntityTable<Escrow>

    val decisionDbKeyFactory: DbKey.LinkKeyFactory<Escrow.Decision>

    val decisionTable: VersionedEntityTable<Escrow.Decision>

    val resultTransactions: MutableList<Transaction>

    suspend fun getEscrowTransactionsByParticipant(accountId: Long?): Collection<Escrow>

    suspend fun getDecisions(id: Long?): Collection<Escrow.Decision>
}
