package brs.db.sql

import brs.db.BurstKey
import brs.util.string.countMatches
import org.jooq.*

internal interface SqlDbKey : BurstKey {
    override val pkValues: LongArray

    abstract class Factory<T> internal constructor(
        pkClause: String, val pkColumns: Array<String>, // expects tables to be named a and b
        val selfJoinClause: String
    ) : BurstKey.Factory<T> {
        /**
         * The number of variables in PKClause
         */
        val pkVariables = pkClause.countMatches("?")

        abstract fun applySelfJoin(query: SelectQuery<Record>, queryTable: Table<*>, otherTable: Table<*>)
    }

    fun getPKConditions(tableClass: Table<*>): Collection<Condition>

    abstract class LongKeyFactory<T>(private val idColumn: Field<Long>) : Factory<T>(
        " WHERE " + idColumn.name + " = ? ",
        arrayOf(idColumn.name),
        " a." + idColumn.name + " = b." + idColumn.name + " "
    ), BurstKey.LongKeyFactory<T> {
        override fun newKey(record: Record): BurstKey {
            val result = record.get(idColumn)
            return LongKey(result!!, idColumn.name)
        }

        override fun newKey(id: Long): BurstKey {
            return LongKey(id, idColumn.name)
        }

        override fun applySelfJoin(query: SelectQuery<Record>, queryTable: Table<*>, otherTable: Table<*>) {
            query.addConditions(
                queryTable.field(idColumn.name, Long::class.java).eq(
                    otherTable.field(idColumn.name, Long::class.java)
                )
            )
        }
    }

    abstract class LinkKeyFactory<T>(private val idColumnA: String, private val idColumnB: String) : Factory<T>(
        " WHERE $idColumnA = ? AND $idColumnB = ? ",
        arrayOf(idColumnA, idColumnB),
        " a.$idColumnA = b.$idColumnA AND a.$idColumnB = b.$idColumnB "
    ), BurstKey.LinkKeyFactory<T> {
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

        override fun applySelfJoin(query: SelectQuery<Record>, queryTable: Table<*>, otherTable: Table<*>) {
            query.addConditions(
                queryTable.field(idColumnA, Long::class.java).eq(
                    otherTable.field(idColumnA, Long::class.java)
                )
            )
            query.addConditions(
                queryTable.field(idColumnB, Long::class.java).eq(
                    otherTable.field(idColumnB, Long::class.java)
                )
            )
        }
    }

    class LongKey internal constructor(private val id: Long, private val idColumn: String) : SqlDbKey {
        override val pkValues: LongArray
            get() = longArrayOf(id)

        override fun equals(other: Any?): Boolean {
            return other is LongKey && other.id == id
        }

        override fun hashCode(): Int {
            return (id xor id.ushr(32)).toInt()
        }

        override fun getPKConditions(tableClass: Table<*>): Collection<Condition> {
            return listOf(tableClass.field(idColumn, Long::class.java).eq(id))
        }
    }

    class LinkKey internal constructor(
        private val idA: Long,
        private val idB: Long,
        private val idColumnA: String,
        private val idColumnB: String
    ) : SqlDbKey {
        override val pkValues: LongArray
            get() = longArrayOf(idA, idB)

        override fun equals(other: Any?): Boolean {
            return other is LinkKey && other.idA == idA && other.idB == idB
        }

        override fun hashCode(): Int {
            return (idA xor idA.ushr(32)).toInt() xor (idB xor idB.ushr(32)).toInt()
        }

        override fun getPKConditions(tableClass: Table<*>): Collection<Condition> {
            return listOf(
                tableClass.field(idColumnA, Long::class.java).eq(idA),
                tableClass.field(idColumnB, Long::class.java).eq(idB)
            )
        }
    }
}
