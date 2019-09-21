package brs.db

import org.jooq.Record

interface BurstKey {
    val pkValues: LongArray

    interface Factory<T> {
        fun newKey(t: T): BurstKey
        fun newKey(record: Record): BurstKey
    }

    interface LongKeyFactory<T> : Factory<T> {
        override fun newKey(record: Record): BurstKey
        fun newKey(id: Long): BurstKey
    }

    interface LinkKeyFactory<T> : Factory<T> {
        fun newKey(idA: Long, idB: Long): BurstKey
    }
}
