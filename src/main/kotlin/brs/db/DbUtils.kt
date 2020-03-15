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

fun DSLContext.upsert(table: Table<*>, record: Map<Field<*>, *>, keys: Collection<Field<*>>): Query {
    return when(dialect()) {
        SQLDialect.CUBRID, SQLDialect.DERBY, SQLDialect.FIREBIRD, SQLDialect.H2, SQLDialect.HSQLDB, SQLDialect.MARIADB, SQLDialect.MYSQL, SQLDialect.POSTGRES -> mergeInto(table, record.keys)
            .key(keys)
            .values(record.values)
        else -> insertInto(table)
            .set(record)
            .onConflict(keys)
            .doUpdate()
            .set(record)
    }
}

/**
 * Hack that turns a collection of keys and a collection of values into a map.
 * TODO remove when bulk upsert is optimized
 */
private fun <K, V> mapOf(keys: Collection<K>, values: Collection<V>): Map<K, V> {
    require(keys.size == values.size)

    return object : AbstractMap<K, V>() {
        override val entries: Set<Map.Entry<K, V>>
            get() = object : AbstractSet<Map.Entry<K, V>>() {
                override val size: Int
                    get() = keys.size

                override fun iterator(): Iterator<Map.Entry<K, V>> {
                    return object : Iterator<Map.Entry<K, V>> {
                        val keyIterator = keys.iterator()
                        val valueIterator = values.iterator()

                        override fun hasNext(): Boolean {
                            return keyIterator.hasNext()
                        }

                        override fun next(): Map.Entry<K, V> {
                            return object : Map.Entry<K, V> {
                                override val key = keyIterator.next()
                                override val value = valueIterator.next()
                            }
                        }
                    }
                }
            }
    }
}

fun DSLContext.upsert(table: Table<*>, columns: Collection<Field<*>>, values: Collection<Collection<*>>, keys: Collection<Field<*>>): Batch {
    // TODO turn into just one query
    values.forEach { value ->
        upsert(table, mapOf(columns, value), keys)
    }
}
