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
        escrowTable = object : SqlMutableBatchEntityTable<Escrow>(ESCROW, ESCROW.HEIGHT, ESCROW.LATEST, escrowDbKeyFactory, Escrow::class.java, dp) {
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

            override fun saveBatch(ctx: DSLContext, entities: Collection<Escrow>) {
                val height = dp.blockchainService.height
                val query = ctx.insertInto(ESCROW, ESCROW.ID, ESCROW.SENDER_ID, ESCROW.RECIPIENT_ID, ESCROW.AMOUNT, ESCROW.REQUIRED_SIGNERS, ESCROW.DEADLINE, ESCROW.DEADLINE_ACTION, ESCROW.HEIGHT, ESCROW.LATEST)
                entities.forEach { entity ->
                    query.values(
                        entity.id,
                        entity.senderId,
                        entity.recipientId,
                        entity.amountPlanck,
                        entity.requiredSigners,
                        entity.deadline,
                        Escrow.decisionToByte(entity.deadlineAction).toInt(),
                        height,
                        true
                    )
                }
                query.execute()
            }
        }

        decisionTable = object : SqlMutableBatchEntityTable<Escrow.Decision>(ESCROW_DECISION, ESCROW_DECISION.HEIGHT, ESCROW_DECISION.LATEST, decisionDbKeyFactory, Escrow.Decision::class.java, dp) {
            override fun load(record: Record) = Escrow.Decision(
                decisionDbKeyFactory.newKey(
                    record.get(ESCROW_DECISION.ESCROW_ID),
                    record.get(ESCROW_DECISION.ACCOUNT_ID)
                ),
                record.get(ESCROW_DECISION.ESCROW_ID),
                record.get(ESCROW_DECISION.ACCOUNT_ID),
                byteToDecision(record.get(ESCROW_DECISION.DECISION).toByte()))

            override fun saveBatch(ctx: DSLContext, entities: Collection<Escrow.Decision>) {
                val height = dp.blockchainService.height
                val query = ctx.insertInto(ESCROW_DECISION, ESCROW_DECISION.ESCROW_ID, ESCROW_DECISION.ACCOUNT_ID, ESCROW_DECISION.DECISION, ESCROW_DECISION.HEIGHT, ESCROW_DECISION.LATEST)
                entities.forEach { entity ->
                    query.values(
                        entity.escrowId,
                        entity.accountId,
                        Escrow.decisionToByte(entity.decision).toInt(),
                        height,
                        true
                    )
                }
                query.execute()
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
