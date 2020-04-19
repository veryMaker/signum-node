package brs.db.sql

import brs.db.BurstKey
import brs.db.EscrowStore
import brs.db.MutableEntityTable
import brs.entity.DependencyProvider
import brs.entity.Escrow
import brs.entity.Escrow.Companion.byteToDecision
import brs.entity.Transaction
import brs.schema.Tables.ESCROW
import brs.schema.Tables.ESCROW_DECISION
import brs.util.db.upsert
import org.jooq.DSLContext
import org.jooq.Record

internal class SqlEscrowStore(private val dp: DependencyProvider) : EscrowStore {
    override val escrowDbKeyFactory = object : SqlDbKey.LongKeyFactory<Escrow>(ESCROW.ID) {
        override fun newKey(entity: Escrow): BurstKey {
            return entity.dbKey
        }
    }

    override val escrowTable: MutableEntityTable<Escrow>
    override val decisionDbKeyFactory = object : SqlDbKey.LinkKeyFactory<Escrow.Decision>(ESCROW_DECISION.ESCROW_ID, ESCROW_DECISION.ACCOUNT_ID) {
        override fun newKey(entity: Escrow.Decision): BurstKey {
            return entity.dbKey
        }
    }
    override val decisionTable: MutableEntityTable<Escrow.Decision>
    override val resultTransactions = mutableListOf<Transaction>()

    init {
        escrowTable = object : SqlMutableEntityTable<Escrow>(ESCROW, ESCROW.HEIGHT, ESCROW.LATEST, escrowDbKeyFactory, dp) { // TODO batch table!
            override fun load(record: Record) = Escrow(
                dp,
                record.get(ESCROW.ID),
                record.get(ESCROW.SENDER_ID),
                record.get(ESCROW.RECIPIENT_ID),
                escrowDbKeyFactory.newKey(record.get(ESCROW.ID)),
                record.get(ESCROW.AMOUNT),
                record.get(ESCROW.REQUIRED_SIGNERS),
                record.get(ESCROW.DEADLINE),
                byteToDecision(record.get(ESCROW.DEADLINE_ACTION).toByte()))

            private val upsertColumns = listOf(ESCROW.ID, ESCROW.SENDER_ID, ESCROW.RECIPIENT_ID, ESCROW.AMOUNT, ESCROW.REQUIRED_SIGNERS, ESCROW.DEADLINE, ESCROW.DEADLINE_ACTION, ESCROW.HEIGHT, ESCROW.LATEST)
            private val upsertKeys = listOf(ESCROW.ID, ESCROW.HEIGHT)

            override fun save(ctx: DSLContext, entity: Escrow) {
                ctx.upsert(ESCROW, upsertKeys, mapOf(
                    ESCROW.ID to entity.id,
                    ESCROW.SENDER_ID to entity.senderId,
                    ESCROW.RECIPIENT_ID to entity.recipientId,
                    ESCROW.AMOUNT to entity.amountPlanck,
                    ESCROW.REQUIRED_SIGNERS to entity.requiredSigners,
                    ESCROW.DEADLINE to entity.deadline,
                    ESCROW.DEADLINE_ACTION to Escrow.decisionToByte(entity.deadlineAction).toInt(),
                    ESCROW.HEIGHT to dp.blockchainService.height,
                    ESCROW.LATEST to true
                )).execute()
            }

            override fun save(ctx: DSLContext, entities: Collection<Escrow>) {
                if (entities.isEmpty()) return
                val height = dp.blockchainService.height
                ctx.upsert(ESCROW, upsertColumns, upsertKeys, entities.map { entity -> arrayOf(
                    entity.id,
                    entity.senderId,
                    entity.recipientId,
                    entity.amountPlanck,
                    entity.requiredSigners,
                    entity.deadline,
                    Escrow.decisionToByte(entity.deadlineAction).toInt(),
                    height,
                    true
                ) }).execute()
            }
        }

        decisionTable = object :
            SqlMutableEntityTable<Escrow.Decision>(ESCROW_DECISION, ESCROW_DECISION.HEIGHT, ESCROW_DECISION.LATEST, decisionDbKeyFactory, dp) { // TODO batch table!
            override fun load(record: Record) = Escrow.Decision(
                decisionDbKeyFactory.newKey(
                    record.get(ESCROW_DECISION.ESCROW_ID),
                    record.get(ESCROW_DECISION.ACCOUNT_ID)
                ),
                record.get(ESCROW_DECISION.ESCROW_ID),
                record.get(ESCROW_DECISION.ACCOUNT_ID),
                byteToDecision(record.get(ESCROW_DECISION.DECISION).toByte()))

            private val upsertColumns = listOf(ESCROW_DECISION.ESCROW_ID, ESCROW_DECISION.ACCOUNT_ID, ESCROW_DECISION.DECISION, ESCROW_DECISION.HEIGHT, ESCROW_DECISION.LATEST)
            private val upsertKeys = listOf(ESCROW_DECISION.ESCROW_ID, ESCROW_DECISION.ACCOUNT_ID, ESCROW_DECISION.HEIGHT)

            override fun save(ctx: DSLContext, entity: Escrow.Decision) {
                ctx.upsert(ESCROW_DECISION, upsertKeys, mapOf(
                    ESCROW_DECISION.ESCROW_ID to entity.escrowId,
                    ESCROW_DECISION.ACCOUNT_ID to entity.accountId,
                    ESCROW_DECISION.DECISION to Escrow.decisionToByte(entity.decision).toInt(),
                    ESCROW_DECISION.HEIGHT to dp.blockchainService.height,
                    ESCROW_DECISION.LATEST to true
                )).execute()
            }

            override fun save(ctx: DSLContext, entities: Collection<Escrow.Decision>) {
                if (entities.isEmpty()) return
                val height = dp.blockchainService.height
                ctx.upsert(ESCROW_DECISION, upsertColumns, upsertKeys, entities.map { entity -> arrayOf(
                    entity.escrowId,
                    entity.accountId,
                    Escrow.decisionToByte(entity.decision).toInt(),
                    height,
                    true
                ) }).execute()
            }
        }
    }

    override fun getEscrowTransactionsByParticipant(accountId: Long): Collection<Escrow> {
        val filtered = mutableListOf<Escrow>()
        for (decision in decisionTable.getManyBy(ESCROW_DECISION.ACCOUNT_ID.eq(accountId), 0, -1)) {
            val escrow = escrowTable[escrowDbKeyFactory.newKey(decision.escrowId)]
            if (escrow != null) {
                filtered.add(escrow)
            }
        }
        return filtered
    }

    override fun getDecisions(id: Long?): Collection<Escrow.Decision> {
        return decisionTable.getManyBy(ESCROW_DECISION.ESCROW_ID.eq(id), 0, -1)
    }
}
