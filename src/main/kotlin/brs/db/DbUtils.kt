package brs.db

import org.jooq.*
import org.jooq.Table
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

private fun SQLDialect.supportsMerge(): Boolean {
    return when(this) {
        SQLDialect.CUBRID, SQLDialect.DERBY, SQLDialect.FIREBIRD, SQLDialect.H2, SQLDialect.HSQLDB, SQLDialect.MARIADB, SQLDialect.MYSQL, SQLDialect.POSTGRES -> true
        else -> false
    }
}

fun DSLContext.upsert(table: Table<*>, keys: Collection<Field<*>>, record: Map<Field<*>, *>): Query {
    return if (dialect().supportsMerge()) {
        mergeInto(table, record.keys)
            .key(keys)
            .values(record.values)
    } else {
        insertInto(table)
            .set(record)
            .onConflict(keys)
            .doUpdate()
            .set(record)
    }
}

/**
 * This takes a collection of keys and returns a map, with each key in [keys] corresponding to `null` in the map.
 * This is used for creating a blank query for adding bind variables to later.
 */
private fun <T> mapKeysToNull(keys: Collection<T>): Map<T, Nothing?> {
    return object : AbstractMap<T, Nothing?>() {
        override val entries: Set<Map.Entry<T, Nothing?>>
            get() = object : AbstractSet<Map.Entry<T, Nothing?>>() {
                override val size: Int
                    get() = keys.size

                override fun iterator(): Iterator<Map.Entry<T, Nothing?>> {
                    return object : Iterator<Map.Entry<T, Nothing?>> {
                        val keyIterator = keys.iterator()

                        override fun hasNext(): Boolean {
                            return keyIterator.hasNext()
                        }

                        override fun next(): Map.Entry<T, Nothing?> {
                            return object : Map.Entry<T, Nothing?> {
                                override val key = keyIterator.next()
                                override val value: Nothing? = null
                            }
                        }
                    }
                }
            }
    }
}

fun DSLContext.upsert(table: Table<*>, columns: Collection<Field<*>>, keys: Collection<Field<*>>, values: Collection<Array<*>>): Batch {
    require(columns.isNotEmpty() && keys.isNotEmpty() && values.isNotEmpty()) { "Columns, keys and values must not be empty" }
    val query = batch(upsert(table, keys, mapKeysToNull(columns)))
    values.forEach { value ->
        query.bind(*value)
    }
    return query
}
