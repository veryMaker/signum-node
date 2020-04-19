package brs.db.sql

import brs.db.BurstKey
import org.jooq.*

internal abstract class SqlDbKey : BurstKey {
    abstract class Factory<T> internal constructor(val primaryKeyColumns: Array<Field<Long>>) : BurstKey.Factory<T> {
        abstract fun applySelfJoin(query: SelectQuery<Record>, queryTable: Table<*>)
    }

    abstract val primaryKeyConditions: List<Condition>

    /**
     * All conditions in [primaryKeyConditions], concatenated with the AND operator.
     */
    val allPrimaryKeyConditions by lazy {
        val conditions = primaryKeyConditions
        var condition = conditions.first()
        for (index in 1 until conditions.size) {
            condition = condition.and(conditions[index])
        }
        condition
    }

    abstract class LongKeyFactory<T>(private val idColumn: Field<Long>) : Factory<T>(arrayOf(idColumn)), BurstKey.LongKeyFactory<T> {
        override fun newKey(record: Record): BurstKey {
            val result = record.get(idColumn)
            checkNotNull(result) { "Record contained no ID" }
            return LongKey(result, idColumn)
        }

        override fun newKey(id: Long): BurstKey {
            return LongKey(id, idColumn)
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

    class LongKey internal constructor(private val id: Long, column: Field<Long>) : SqlDbKey() {
        override val primaryKeyValues: LongArray
            get() = longArrayOf(id)

        override fun equals(other: Any?): Boolean {
            return other is LongKey && other.id == id
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }

        override val primaryKeyConditions = listOf(column.eq(id))
    }

    class LinkKey internal constructor(
        private val idA: Long,
        private val idB: Long,
        idColumnA: Field<Long>,
        idColumnB: Field<Long>
    ) : SqlDbKey() {
        override val primaryKeyValues = longArrayOf(idA, idB)

        override val primaryKeyConditions = listOf(idColumnA.eq(idA), idColumnB.eq(idB))

        override fun equals(other: Any?): Boolean {
            return other is LinkKey && other.idA == idA && other.idB == idB
        }

        override fun hashCode(): Int {
            var result = idA.hashCode()
            result = 31 * result + idB.hashCode()
            return result
        }
    }
}
