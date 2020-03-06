package brs.db

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Query
import org.jooq.UpdatableRecord
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Use the DSL Context of the DB
 * @param action The action to perform using the DSL Context
 * @return The value fetched by the [action]
 */
inline fun <T> Db.useDslContext(action: (DSLContext) -> T): T {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    getDslContext().use { context -> return action(context) }
}

/**
 * Perform [action] within a database transaction if we are not already in a transaction.
 */
inline fun Db.ensureInTransaction(action: () -> Unit) {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    if (this.isInTransaction()) {
        action()
    } else {
        this.transaction {
            action()
        }
    }
}

/**
 * Perform [action] within a database transaction, and revert
 * if an exception was thrown.
 * @param action The action to perform within a DB transaction
 */
inline fun Db.transaction(action: () -> Unit) {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    beginTransaction()
    try {
        action()
        commitTransaction()
    } catch (e: Exception) {
        rollbackTransaction()
        throw e
    } finally {
        endTransaction()
    }
}

fun Db.assertInTransaction() {
    check(isInTransaction()) { "Database not in transaction" }
}

fun DSLContext.upsert(record: UpdatableRecord<*>, vararg keys: Field<*>): Query {
    return insertInto(record.getTable())
        .set(record)
        .onConflict(*keys) // TODO work around having to use spread operator here...
        .doUpdate()
        .set(record)
}
