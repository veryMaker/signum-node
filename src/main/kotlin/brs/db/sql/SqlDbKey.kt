package brs.db.sql

import brs.db.BurstKey
import org.jooq.*

internal interface SqlDbKey : BurstKey {
    override val primaryKeyValues: LongArray

    abstract class Factory<T> internal constructor(val primaryKeyColumns: Array<Field<Long>>) : BurstKey.Factory<T> {
        abstract fun applySelfJoin(query: SelectQuery<Record>, queryTable: Table<*>)
    }

    fun getPrimaryKeyConditions(tableClass: Table<*>): Collection<Condition>

    abstract class LongKeyFactory<T>(private val idColumn: Field<Long>) : Factory<T>(arrayOf(idColumn)), BurstKey.LongKeyFactory<T> {
        override fun newKey(record: Record): BurstKey {
            val result = record.get(idColumn)
            return LongKey(result!!, idColumn.name)
        }

        override fun newKey(id: Long): BurstKey {
            return LongKey(id, idColumn.name)
        }

        override fun applySelfJoin(query: SelectQuery<Record>, queryTable: Table<*>) {
            query.addConditions(queryTable.field(idColumn.name, Long::class.java).eq(idColumn))
        }
    }

    abstract class LinkKeyFactory<T>(private val idColumnA: Field<Long>, private val idColumnB: Field<Long>) : Factory<T>(arrayOf(idColumnA, idColumnB)), BurstKey.LinkKeyFactory<T> {
        override fun newKey(record: Record): SqlDbKey {
            return LinkKey(
                record.get(idColumnA, Long::class.java),
                record.get(idColumnB, Long::class.java),
                idColumnA,
                idColumnB
            )
        }

        override fun newKey(idA: Long, idB: Long): SqlDbKey {
            return LinkKey(idA, idB, idColumnA, idColumnB)
        }

        override fun applySelfJoin(query: SelectQuery<Record>, queryTable: Table<*>) {
            query.addConditions(
                queryTable.field(idColumnA.name, Long::class.java).eq(idColumnA),
                queryTable.field(idColumnB.name, Long::class.java).eq(idColumnB)
            )
        }
    }

    class LongKey internal constructor(private val id: Long, private val idColumn: String) : SqlDbKey {
        override val primaryKeyValues: LongArray
            get() = longArrayOf(id)

        override fun equals(other: Any?): Boolean {
            return other is LongKey && other.id == id
        }

        override fun hashCode(): Int {
            return (id xor id.ushr(32)).toInt()
        }

        override fun getPrimaryKeyConditions(tableClass: Table<*>): Collection<Condition> {
            return listOf(tableClass.field(idColumn, Long::class.java).eq(id))
        }
    }

    data class LinkKey internal constructor(
        private val idA: Long,
        private val idB: Long,
        private val idColumnA: Field<Long>,
        private val idColumnB: Field<Long>
    ) : SqlDbKey {
        override val primaryKeyValues = longArrayOf(idA, idB)

        override fun getPrimaryKeyConditions(tableClass: Table<*>): Collection<Condition> {
            return listOf(idColumnA.eq(idA), idColumnB.eq(idB))
        }
    }
}
