package brs.db.sql

import brs.db.TransactionDb
import brs.db.useDslContext
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.schema.Tables.TRANSACTION
import brs.schema.tables.records.TransactionRecord
import brs.transaction.appendix.Appendix
import brs.transaction.type.TransactionType
import brs.util.BurstException
import brs.util.convert.toUnsignedString
import brs.util.db.fetchAndMap
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class SqlTransactionDb(private val dp: DependencyProvider) : TransactionDb {
    override fun findTransaction(transactionId: Long): Transaction? {
        return dp.db.useDslContext { ctx ->
            try {
                val transactionRecord = ctx.selectFrom(TRANSACTION).where(TRANSACTION.ID.eq(transactionId)).fetchOne() ?: return null
                return@useDslContext loadTransaction(transactionRecord)
            } catch (e: BurstException.ValidationException) {
                throw Exception("Transaction already in database, id = $transactionId, does not pass validation!", e)
            }
        }
    }

    override fun findTransactionByFullHash(fullHash: ByteArray): Transaction? {
        return dp.db.useDslContext { ctx ->
            try {
                val transactionRecord = ctx.selectFrom(TRANSACTION).where(TRANSACTION.FULL_HASH.eq(fullHash)).fetchOne() ?: return null
                return@useDslContext loadTransaction(transactionRecord)
            } catch (e: BurstException.ValidationException) {
                throw Exception("Transaction already in database, full_hash = $fullHash, does not pass validation!", e)
            }
        }
    }

    override fun hasTransaction(transactionId: Long): Boolean {
        return dp.db.useDslContext { ctx ->
            ctx.fetchExists(
                ctx.selectFrom(TRANSACTION).where(
                    TRANSACTION.ID.eq(
                        transactionId
                    )
                )
            )
        }
    }

    override fun hasTransactionByFullHash(fullHash: ByteArray): Boolean {
        return dp.db.useDslContext { ctx ->
            ctx.fetchExists(ctx.selectFrom(TRANSACTION).where(TRANSACTION.FULL_HASH.eq(fullHash)))
        }
    }

    override fun loadTransaction(record: TransactionRecord): Transaction {
        val buffer: ByteBuffer
        if (record.attachmentBytes != null) {
            buffer = ByteBuffer.wrap(record.attachmentBytes)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
        } else {
            buffer = ByteBuffer.allocate(0)
        }

        val transactionType = TransactionType.findTransactionType(dp, record.type, record.subtype) ?: error("Could not find transaction with type ${record.type} and subtype ${record.subtype}")
        val builder = Transaction.Builder(
            dp, record.version, record.senderPublicKey,
            record.amount, record.fee, record.timestamp, record.deadline,
            transactionType.parseAttachment(buffer, record.version)
        )
            .signature(record.signature)
            .blockId(record.blockId)
            .height(record.height)
            .id(record.id)
            .senderId(record.senderId)
            .blockTimestamp(record.blockTimestamp)
            .fullHash(record.fullHash)
        val referencedTransactionFullHash = record.referencedTransactionFullhash
        if (referencedTransactionFullHash != null) builder.referencedTransactionFullHash(referencedTransactionFullHash)
        if (transactionType.hasRecipient() && record.recipientId != null) builder.recipientId(record.recipientId)
        if (record.hasMessage) builder.message(Appendix.Message(buffer, record.version))
        if (record.hasEncryptedMessage) builder.encryptedMessage(Appendix.EncryptedMessage(buffer, record.version))
        if (record.hasPublicKeyAnnouncement) builder.publicKeyAnnouncement(Appendix.PublicKeyAnnouncement(dp, buffer, record.version))
        if (record.hasEncrypttoselfMessage) builder.encryptToSelfMessage(Appendix.EncryptToSelfMessage(buffer, record.version))
        if (record.version > 0) {
            builder.ecBlockHeight(record.ecBlockHeight)
            if (record.ecBlockId != null) builder.ecBlockId(record.ecBlockId)
        }

        return builder.build()
    }

    override fun findBlockTransactions(blockId: Long): Collection<Transaction> {
        return dp.db.useDslContext { ctx ->
            ctx.selectFrom(TRANSACTION)
                .where(TRANSACTION.BLOCK_ID.eq(blockId))
                .and(TRANSACTION.SIGNATURE.isNotNull)
                .fetchAndMap { record ->
                    try {
                        return@fetchAndMap loadTransaction(record)
                    } catch (e: BurstException.ValidationException) {
                        throw Exception(
                            "Transaction already in database for block_id = ${blockId.toUnsignedString()} does not pass validation!",
                            e
                        )
                    }
                }
        }
    }

    private fun getAttachmentBytes(transaction: Transaction): ByteArray? {
        var bytesLength = 0
        for (appendage in transaction.appendages) {
            bytesLength += appendage.size
        }
        return if (bytesLength == 0) {
            null
        } else {
            val buffer = ByteBuffer.allocate(bytesLength)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            for (appendage in transaction.appendages) {
                appendage.putBytes(buffer)
            }
            buffer.array()
        }
    }

    override fun saveTransactions(transactions: Collection<Transaction>) {
        if (transactions.isNotEmpty()) {
            dp.db.useDslContext<Unit> { ctx ->
                ctx.batchInsert(transactions.map { transaction ->
                    TransactionRecord().apply {
                        id = transaction.id
                        deadline = transaction.deadline
                        setSenderPublicKey(*transaction.senderPublicKey) // TODO better way of setting
                        if (transaction.recipientId != 0L) recipientId = transaction.recipientId
                        amount = transaction.amountPlanck
                        fee = transaction.feePlanck
                        if (transaction.referencedTransactionFullHash != null) setReferencedTransactionFullhash(*transaction.referencedTransactionFullHash) // TODO better way of setting
                        height = transaction.height
                        blockId = transaction.blockId
                        if (transaction.signature != null) setSignature(*transaction.signature!!) // TODO better way of setting
                        timestamp = transaction.timestamp
                        type = transaction.type.type
                        subtype = transaction.type.subtype
                        senderId = transaction.senderId
                        val attachment = getAttachmentBytes(transaction)
                        if (attachment != null) setAttachmentBytes(*attachment) // TODO better way of setting
                        blockTimestamp = transaction.blockTimestamp
                        setFullHash(*transaction.fullHash) // TODO better way of setting
                        version = transaction.version
                        hasMessage = transaction.message != null
                        hasEncryptedMessage = transaction.encryptedMessage != null
                        hasPublicKeyAnnouncement = transaction.publicKeyAnnouncement != null
                        hasEncrypttoselfMessage = transaction.encryptToSelfMessage != null
                        ecBlockHeight = transaction.ecBlockHeight
                        if (transaction.ecBlockId != 0L) ecBlockId = transaction.ecBlockId
                        changed(true)
                    }
                }).execute()
            }
        }
    }

    override fun optimize() {
        dp.db.optimizeTable(TRANSACTION.name)
    }
}
