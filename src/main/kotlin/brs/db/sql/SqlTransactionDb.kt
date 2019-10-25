package brs.db.sql

import brs.Appendix
import brs.BurstException
import brs.DependencyProvider
import brs.Transaction
import brs.db.TransactionDb
import brs.schema.Tables.TRANSACTION
import brs.schema.tables.records.TransactionRecord
import brs.transaction.TransactionType
import brs.util.convert.toUnsignedString
import brs.util.db.fetchAndMap
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class SqlTransactionDb(private val dp: DependencyProvider) : TransactionDb {

    override fun findTransaction(transactionId: Long): Transaction {
        return dp.db.getUsingDslContext<Transaction> { ctx ->
            try {
                val transactionRecord = ctx.selectFrom(TRANSACTION).where(TRANSACTION.ID.eq(transactionId)).fetchOne()
                return@getUsingDslContext loadTransaction(transactionRecord)
            } catch (e: BurstException.ValidationException) {
                throw RuntimeException("Transaction already in database, id = $transactionId, does not pass validation!", e)
            }
        }
    }

    override fun findTransactionByFullHash(fullHash: ByteArray): Transaction {
        return dp.db.getUsingDslContext<Transaction> { ctx ->
            try {
                val transactionRecord = ctx.selectFrom(TRANSACTION).where(TRANSACTION.FULL_HASH.eq(fullHash)).fetchOne()
                return@getUsingDslContext loadTransaction(transactionRecord)
            } catch (e: BurstException.ValidationException) {
                throw RuntimeException("Transaction already in database, full_hash = $fullHash, does not pass validation!", e)
            }
        }
    }

    override fun hasTransaction(transactionId: Long): Boolean {
        return dp.db.getUsingDslContext<Boolean> { ctx -> ctx.fetchExists(ctx.selectFrom(TRANSACTION).where(TRANSACTION.ID.eq(transactionId))) }
    }

    override fun hasTransactionByFullHash(fullHash: ByteArray): Boolean {
        return dp.db.getUsingDslContext<Boolean> { ctx -> ctx.fetchExists(ctx.selectFrom(TRANSACTION).where(TRANSACTION.FULL_HASH.eq(fullHash))) }
    }

    override fun loadTransaction(tr: TransactionRecord): Transaction {
        val buffer: ByteBuffer
        if (tr.attachmentBytes != null) {
            buffer = ByteBuffer.wrap(tr.attachmentBytes)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
        } else {
            buffer = ByteBuffer.allocate(0)
        }

        val transactionType = TransactionType.findTransactionType(dp, tr.type!!, tr.subtype!!)
        val builder = Transaction.Builder(dp, tr.version!!, tr.senderPublicKey,
                tr.amount!!, tr.fee!!, tr.timestamp!!, tr.deadline!!,
                transactionType!!.parseAttachment(buffer, tr.version!!))
                .signature(tr.signature)
                .blockId(tr.blockId!!)
                .height(tr.height!!)
                .id(tr.id!!)
                .senderId(tr.senderId!!)
                .blockTimestamp(tr.blockTimestamp!!)
                .fullHash(tr.fullHash)
        val referencedTransactionFullHash = tr.referencedTransactionFullhash
        if (referencedTransactionFullHash != null) {
            builder.referencedTransactionFullHash(referencedTransactionFullHash)
        }
        if (transactionType.hasRecipient()) {
            builder.recipientId(Optional.ofNullable(tr.recipientId).orElse(0L))
        }
        if (tr.hasMessage!!) {
            builder.message(Appendix.Message(buffer, tr.version!!))
        }
        if (tr.hasEncryptedMessage!!) {
            builder.encryptedMessage(Appendix.EncryptedMessage(buffer, tr.version!!))
        }
        if (tr.hasPublicKeyAnnouncement!!) {
            builder.publicKeyAnnouncement(Appendix.PublicKeyAnnouncement(dp, buffer, tr.version!!))
        }
        if (tr.hasEncrypttoselfMessage!!) {
            builder.encryptToSelfMessage(Appendix.EncryptToSelfMessage(buffer, tr.version!!))
        }
        if (tr.version > 0) {
            builder.ecBlockHeight(tr.ecBlockHeight!!)
            builder.ecBlockId(Optional.ofNullable(tr.ecBlockId).orElse(0L))
        }

        return builder.build()
    }

    override fun findBlockTransactions(blockId: Long): Collection<Transaction> {
        return dp.db.getUsingDslContext<List<Transaction>> { ctx ->
            ctx.selectFrom(TRANSACTION)
                    .where(TRANSACTION.BLOCK_ID.eq(blockId))
                    .and(TRANSACTION.SIGNATURE.isNotNull)
                    .fetchAndMap { record ->
                        try {
                            return@fetchAndMap loadTransaction(record)
                        } catch (e: BurstException.ValidationException) {
                            throw RuntimeException("Transaction already in database for block_id = ${blockId.toUnsignedString()} does not pass validation!", e)
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
            dp.db.useDslContext { ctx ->
                val insertBatch = ctx.batch(
                        ctx.insertInto(TRANSACTION, TRANSACTION.ID, TRANSACTION.DEADLINE,
                                TRANSACTION.SENDER_PUBLIC_KEY, TRANSACTION.RECIPIENT_ID, TRANSACTION.AMOUNT,
                                TRANSACTION.FEE, TRANSACTION.REFERENCED_TRANSACTION_FULLHASH, TRANSACTION.HEIGHT,
                                TRANSACTION.BLOCK_ID, TRANSACTION.SIGNATURE, TRANSACTION.TIMESTAMP,
                                TRANSACTION.TYPE,
                                TRANSACTION.SUBTYPE, TRANSACTION.SENDER_ID, TRANSACTION.ATTACHMENT_BYTES,
                                TRANSACTION.BLOCK_TIMESTAMP, TRANSACTION.FULL_HASH, TRANSACTION.VERSION,
                                TRANSACTION.HAS_MESSAGE, TRANSACTION.HAS_ENCRYPTED_MESSAGE,
                                TRANSACTION.HAS_PUBLIC_KEY_ANNOUNCEMENT, TRANSACTION.HAS_ENCRYPTTOSELF_MESSAGE,
                                TRANSACTION.EC_BLOCK_HEIGHT, TRANSACTION.EC_BLOCK_ID)
                                .values(null as Long?, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null))
                for (transaction in transactions) {
                    insertBatch.bind(
                            transaction.id,
                            transaction.deadline,
                            transaction.senderPublicKey,
                            if (transaction.recipientId == 0L) null else transaction.recipientId,
                            transaction.amountNQT,
                            transaction.feeNQT,
                            transaction.referencedTransactionFullHash,
                            transaction.height,
                            transaction.blockId,
                            transaction.signature,
                            transaction.timestamp,
                            transaction.type.type,
                            transaction.type.subtype,
                            transaction.senderId,
                            getAttachmentBytes(transaction),
                            transaction.blockTimestamp,
                            transaction.fullHash,
                            transaction.version,
                            transaction.message != null,
                            transaction.encryptedMessage != null,
                            transaction.publicKeyAnnouncement != null,
                            transaction.encryptToSelfMessage != null,
                            transaction.ecBlockHeight,
                            if (transaction.ecBlockId != 0L) transaction.ecBlockId else null
                    )
                }
                insertBatch.execute()
            }
        }
    }

    override fun optimize() {
        dp.db.optimizeTable(TRANSACTION.name)
    }
}
