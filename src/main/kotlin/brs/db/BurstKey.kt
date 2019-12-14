package brs.db

import org.jooq.Record

interface BurstKey {
    val pkValues: LongArray

    interface Factory<T> {
        /**
         * Create a new key for an entity
         * @param entity The entity
         * @return The key for this entity
         */
        fun newKey(entity: T): BurstKey

        /**
         * Create a new key for an entity
         * @param record The entity's SQL record
         * @return The key for this entity
         */
        fun newKey(record: Record): BurstKey
    }

    interface LongKeyFactory<T> : Factory<T> {
        /**
         * Create a new key for an ID
         * @param id An entity ID
         * @return The key for this entity
         */
        fun newKey(id: Long): BurstKey
    }

    interface LinkKeyFactory<T> : Factory<T> {
        /**
         * Create a new key based on 2 (Long) identifying factors of an entity
         * @param idA First identifying factor of the entity
         * @param idB Second identifying factor of the entity
         * @return The key for this entity
         */
        fun newKey(idA: Long, idB: Long): BurstKey
    }
}
