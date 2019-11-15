package brs.db

import org.ehcache.Cache

interface VersionedBatchEntityTable<T> : VersionedEntityTable<T> {
    /**
     * TODO
     */
    fun getBatch(): Map<BurstKey, T>

    /**
     * TODO
     */
    fun getCache(): Cache<BurstKey, T>?

    /**
     * TODO
     */
    fun flushCache()
}
