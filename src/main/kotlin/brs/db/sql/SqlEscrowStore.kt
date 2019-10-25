package brs.db.sql

import brs.DependencyProvider
import brs.Escrow
import brs.Transaction
import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.db.store.EscrowStore
import brs.schema.Tables.ESCROW
import brs.schema.Tables.ESCROW_DECISION
import brs.schema.tables.records.EscrowDecisionRecord
import brs.schema.tables.records.EscrowRecord
import org.jooq.DSLContext
import org.jooq.Record

class SqlEscrowStore(private val dp: DependencyProvider) : EscrowStore {
    override val escrowDbKeyFactory: BurstKey.LongKeyFactory<Escrow> = object : DbKey.LongKeyFactory<Escrow>(ESCROW.ID) {
        override fun newKey(escrow: Escrow): BurstKey {
            return escrow.dbKey
        }
    }

    override val escrowTable: VersionedEntityTable<Escrow>
    override val decisionDbKeyFactory: DbKey.LinkKeyFactory<Escrow.Decision> = object : DbKey.LinkKeyFactory<Escrow.Decision>("escrow_id", "account_id") {
        override fun newKey(decision: Escrow.Decision): BurstKey {
            return decision.dbKey
        }
    }
    override val decisionTable: VersionedEntityTable<Escrow.Decision>
    override val resultTransactions = mutableListOf<Transaction>()

    init {
        escrowTable = object : VersionedEntitySqlTable<Escrow>("escrow", ESCROW, escrowDbKeyFactory, dp) {
            override fun load(ctx: DSLContext, rs: Record): Escrow {
                return SqlEscrow(rs)
            }

            override fun save(ctx: DSLContext, escrow: Escrow) {
                saveEscrow(ctx, escrow)
            }
        }

        decisionTable = object : VersionedEntitySqlTable<Escrow.Decision>("escrow_decision", ESCROW_DECISION, decisionDbKeyFactory, dp) {
            override fun load(ctx: DSLContext, record: Record): Escrow.Decision {
                return SqlDecision(record)
            }

            override fun save(ctx: DSLContext, decision: Escrow.Decision) {
                saveDecision(ctx, decision)
            }
        }
    }

    private fun saveDecision(ctx: DSLContext, decision: Escrow.Decision) {
        ctx.mergeInto<EscrowDecisionRecord, Long, Long, Int, Int, Boolean>(ESCROW_DECISION, ESCROW_DECISION.ESCROW_ID, ESCROW_DECISION.ACCOUNT_ID, ESCROW_DECISION.DECISION, ESCROW_DECISION.HEIGHT, ESCROW_DECISION.LATEST)
                .key(ESCROW_DECISION.ESCROW_ID, ESCROW_DECISION.ACCOUNT_ID, ESCROW_DECISION.HEIGHT)
                .values(decision.escrowId, decision.accountId, Escrow.decisionToByte(decision.decision!!).toInt(), dp.blockchain.height, true)
                .execute()
    }

    override fun getEscrowTransactionsByParticipant(accountId: Long?): Collection<Escrow> {
        val filtered = mutableListOf<Escrow>()
        for (decision in decisionTable.getManyBy(ESCROW_DECISION.ACCOUNT_ID.eq(accountId), 0, -1)) {
            val escrow = escrowTable[escrowDbKeyFactory.newKey(decision.escrowId!!)]
            if (escrow != null) {
                filtered.add(escrow)
            }
        }
        return filtered
    }

    private fun saveEscrow(ctx: DSLContext, escrow: Escrow) {
        ctx.mergeInto<EscrowRecord, Long, Long, Long, Long, Int, Int, Int, Int, Boolean>(ESCROW, ESCROW.ID, ESCROW.SENDER_ID, ESCROW.RECIPIENT_ID, ESCROW.AMOUNT, ESCROW.REQUIRED_SIGNERS, ESCROW.DEADLINE, ESCROW.DEADLINE_ACTION, ESCROW.HEIGHT, ESCROW.LATEST)
                .key(ESCROW.ID, ESCROW.HEIGHT)
                .values(escrow.id, escrow.senderId, escrow.recipientId, escrow.amountPlanck, escrow.requiredSigners, escrow.deadline, Escrow.decisionToByte(escrow.deadlineAction).toInt(), dp.blockchain.height, true)
                .execute()
    }

    private inner class SqlDecision internal constructor(record: Record) : Escrow.Decision(decisionDbKeyFactory.newKey(record.get(ESCROW_DECISION.ESCROW_ID), record.get(ESCROW_DECISION.ACCOUNT_ID)), record.get(ESCROW_DECISION.ESCROW_ID), record.get(ESCROW_DECISION.ACCOUNT_ID), Escrow.byteToDecision(record.get(ESCROW_DECISION.DECISION).toByte()))

    private inner class SqlEscrow internal constructor(record: Record) : Escrow(dp, record.get(ESCROW.ID), record.get(ESCROW.SENDER_ID), record.get(ESCROW.RECIPIENT_ID), escrowDbKeyFactory.newKey(record.get(ESCROW.ID)), record.get(ESCROW.AMOUNT), record.get(ESCROW.REQUIRED_SIGNERS), record.get(ESCROW.DEADLINE), byteToDecision(record.get(ESCROW.DEADLINE_ACTION).toByte())!!)

    override fun getDecisions(id: Long?): Collection<Escrow.Decision> {
        return decisionTable.getManyBy(ESCROW_DECISION.ESCROW_ID.eq(id), 0, -1)
    }
}
