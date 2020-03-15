package brs.db.sql

import brs.db.BurstKey
import brs.db.IndirectIncomingStore
import brs.db.upsert
import brs.db.useDslContext
import brs.entity.DependencyProvider
import brs.entity.IndirectIncoming
import brs.schema.Tables.INDIRECT_INCOMING
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
            SqlEntityTable<IndirectIncoming>(INDIRECT_INCOMING, indirectIncomingDbKeyFactory, INDIRECT_INCOMING.HEIGHT, null, dp) {
            override fun load(record: Record): IndirectIncoming {
                return IndirectIncoming(
                    record.get(INDIRECT_INCOMING.ACCOUNT_ID),
                    record.get(INDIRECT_INCOMING.TRANSACTION_ID),
                    record.get(INDIRECT_INCOMING.HEIGHT)
                )
            }

            private val upsertColumns = listOf(INDIRECT_INCOMING.ACCOUNT_ID, INDIRECT_INCOMING.TRANSACTION_ID, INDIRECT_INCOMING.HEIGHT)
            private val upsertKeys = listOf(INDIRECT_INCOMING.ACCOUNT_ID, INDIRECT_INCOMING.TRANSACTION_ID)

            override fun save(ctx: DSLContext, entity: IndirectIncoming) {
                ctx.upsert(INDIRECT_INCOMING, upsertKeys, mapOf(
                    INDIRECT_INCOMING.ACCOUNT_ID to entity.accountId,
                    INDIRECT_INCOMING.TRANSACTION_ID to entity.transactionId,
                    INDIRECT_INCOMING.HEIGHT to entity.height
                )).execute()
            }

            override fun save(ctx: DSLContext, entities: Collection<IndirectIncoming>) {
                ctx.upsert(INDIRECT_INCOMING,
                    upsertColumns,
                    upsertKeys,
                    entities.map { (accountId, transactionId, height) -> listOf(accountId, transactionId, height)}).execute()
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
