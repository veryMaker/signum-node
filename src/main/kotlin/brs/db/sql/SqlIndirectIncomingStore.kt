package brs.db.sql

import brs.db.BurstKey
import brs.db.IndirectIncomingStore
import brs.db.useDslContext
import brs.entity.DependencyProvider
import brs.entity.IndirectIncoming
import brs.schema.Tables.INDIRECT_INCOMING
import brs.util.db.upsert
import org.jooq.DSLContext
import org.jooq.Record

internal class SqlIndirectIncomingStore(private val dp: DependencyProvider) : IndirectIncomingStore {
    internal val indirectIncomingTable: SqlEntityTable<IndirectIncoming>

    init {
        val indirectIncomingDbKeyFactory =
            object : SqlDbKey.LinkKeyFactory<IndirectIncoming>(INDIRECT_INCOMING.ACCOUNT_ID, INDIRECT_INCOMING.TRANSACTION_ID) {
                override fun newKey(entity: IndirectIncoming): BurstKey {
                    return newKey(entity.accountId, entity.transactionId)
                }
            }

        this.indirectIncomingTable = object :
            SqlBatchEntityTable<IndirectIncoming>(INDIRECT_INCOMING, indirectIncomingDbKeyFactory, INDIRECT_INCOMING.HEIGHT, IndirectIncoming::class.java, dp) {
            override fun load(record: Record): IndirectIncoming {
                return IndirectIncoming(
                    record.get(INDIRECT_INCOMING.ACCOUNT_ID),
                    record.get(INDIRECT_INCOMING.TRANSACTION_ID),
                    record.get(INDIRECT_INCOMING.HEIGHT)
                )
            }

            private val upsertColumns = listOf(INDIRECT_INCOMING.ACCOUNT_ID, INDIRECT_INCOMING.TRANSACTION_ID, INDIRECT_INCOMING.HEIGHT)
            private val upsertKeys = listOf(INDIRECT_INCOMING.ACCOUNT_ID, INDIRECT_INCOMING.TRANSACTION_ID)

            override fun storeBatch(ctx: DSLContext, entities: Collection<IndirectIncoming>) {
                // FIXME because of lack of foreign keys on indirect incoming table, this breaks when BRS rolls back. TODO: change this into an insert when the migrations get fixed
                ctx.upsert(INDIRECT_INCOMING, upsertColumns, upsertKeys, entities.map { (accountId, transactionId, height) ->
                    arrayOf(accountId, transactionId, height)
                }).execute()
            }
        }
    }

    override fun addIndirectIncomings(indirectIncomings: Collection<IndirectIncoming>) {
        dp.db.useDslContext { ctx -> indirectIncomingTable.save(ctx, indirectIncomings) }
    }

    override fun getIndirectIncomings(accountId: Long, from: Int, to: Int): List<Long> {
        return indirectIncomingTable.getManyBy(INDIRECT_INCOMING.ACCOUNT_ID.eq(accountId), from, to)
            .map { it.transactionId }
    }
}
