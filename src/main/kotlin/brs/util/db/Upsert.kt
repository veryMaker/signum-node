package brs.util.db

import org.jooq.*

private fun SQLDialect.supportsMerge(): Boolean {
    return when(this) {
        // SQLDialect.CUBRID is also supports merge but support for this in jOOQ is deprecated.
        SQLDialect.DERBY, SQLDialect.FIREBIRD, SQLDialect.H2, SQLDialect.HSQLDB, SQLDialect.MARIADB, SQLDialect.MYSQL, SQLDialect.POSTGRES -> true
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

private val NULL_ITERABLE = Iterable {
    object : Iterator<Nothing?> {
        override fun hasNext() = true
        override fun next() = null
    }
}

/**
 * Helper method that turns a collection of keys and their corresponding values into a map.
 * Order is critical: Key 1 is mapped to value 1, Key 2 to value 2, etc.
 */
private fun <K, V> mapOf(keys: Collection<K>, values: Iterable<V>): Map<K, V> {
    return object : AbstractMap<K, V>() {
        override val entries: Set<Map.Entry<K, V>>
            get() = object : AbstractSet<Map.Entry<K, V>>() {
                override val size: Int
                    get() = keys.size

                override fun iterator(): Iterator<Map.Entry<K, V>> {
                    return object : Iterator<Map.Entry<K, V>> {
                        val keyIterator = keys.iterator()
                        val valueIterator = values.iterator()

                        override fun hasNext() = keyIterator.hasNext()

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

fun DSLContext.upsert(table: Table<*>, columns: Collection<Field<*>>, keys: Collection<Field<*>>, values: Collection<Array<*>>): Batch {
    require(columns.isNotEmpty() && keys.isNotEmpty() && values.isNotEmpty()) { "Columns, keys and values must not be empty" }
    return if (dialect().supportsMerge()) {
        val query = batch(upsert(table, keys, mapOf(columns, NULL_ITERABLE)))
        values.forEach { value ->
            query.bind(*value)
        }
        query
    } else {
        // TODO I can't get the bind values to work both in the insert clause and the update clause for SQLite.
        // Whenever I do so, SQLite complains that the NOT NULL constraint is violated because,
        // in the template with no bind values, the value is null. Not sure how to work around this.
        // For now we'll just do a batch query.
        return batch(values.map { value ->
            require(columns.size == value.size)
            upsert(table, keys, mapOf(columns, value.asIterable()))
        })
    }
}

