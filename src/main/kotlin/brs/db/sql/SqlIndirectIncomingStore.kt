package brs.db.sql

import brs.entity.DependencyProvider
import brs.db.BurstKey
import brs.db.IndirectIncomingStore
import brs.db.useDslContext
import brs.schema.Tables.INDIRECT_INCOMING
import org.jooq.DSLContext
import org.jooq.Query
import org.jooq.Record

internal class SqlIndirectIncomingStore(private val dp: DependencyProvider) : IndirectIncomingStore {

    private val indirectIncomingTable: EntitySqlTable<IndirectIncomingStore.IndirectIncoming>

    init {
        val indirectIncomingDbKeyFactory = object : SqlDbKey.LinkKeyFactory<IndirectIncomingStore.IndirectIncoming>("account_id", "transaction_id") {
            override fun newKey(indirectIncoming: IndirectIncomingStore.IndirectIncoming): BurstKey {
                return newKey(indirectIncoming.accountId, indirectIncoming.transactionId)
            }
        }

        this.indirectIncomingTable = object : EntitySqlTable<IndirectIncomingStore.IndirectIncoming>("indirect_incoming", INDIRECT_INCOMING, indirectIncomingDbKeyFactory, dp) {
            override fun load(ctx: DSLContext, rs: Record): IndirectIncomingStore.IndirectIncoming {
                return IndirectIncomingStore.IndirectIncoming(rs.get(INDIRECT_INCOMING.ACCOUNT_ID), rs.get(INDIRECT_INCOMING.TRANSACTION_ID), rs.get(INDIRECT_INCOMING.HEIGHT))
            }

            private fun getQuery(ctx: DSLContext, indirectIncoming: IndirectIncomingStore.IndirectIncoming): Query {
                return ctx.mergeInto(INDIRECT_INCOMING, INDIRECT_INCOMING.ACCOUNT_ID, INDIRECT_INCOMING.TRANSACTION_ID, INDIRECT_INCOMING.HEIGHT)
                        .key(INDIRECT_INCOMING.ACCOUNT_ID, INDIRECT_INCOMING.TRANSACTION_ID)
                        .values(indirectIncoming.accountId, indirectIncoming.transactionId, indirectIncoming.height)
            }

            override fun save(ctx: DSLContext, indirectIncoming: IndirectIncomingStore.IndirectIncoming) {
                getQuery(ctx, indirectIncoming).execute()
            }

            override fun save(ctx: DSLContext, indirectIncomings: Array<IndirectIncomingStore.IndirectIncoming>) {
                val queries = mutableListOf<Query>()
                for (indirectIncoming in indirectIncomings) {
                    queries.add(getQuery(ctx, indirectIncoming))
                }
                ctx.batch(queries).execute()
            }
        }
    }

    override fun addIndirectIncomings(indirectIncomings: Collection<IndirectIncomingStore.IndirectIncoming>) {
        dp.db.useDslContext { ctx -> indirectIncomingTable.save(ctx, indirectIncomings.toTypedArray()) }
    }

    override fun getIndirectIncomings(accountId: Long, from: Int, to: Int): List<Long> {
        return indirectIncomingTable.getManyBy(INDIRECT_INCOMING.ACCOUNT_ID.eq(accountId), from, to)
                .map { it.transactionId }
    }
}
