package brs.services

import brs.db.BurstKey
import org.ehcache.Cache

interface DBCacheService {
    /**
     * TODO
     */
    fun close()

    /**
     * TODO
     */
    fun <V> getCache(name: String, valueClass: Class<V>): Cache<BurstKey, V>?

    /**
     * TODO
     */
    fun flushCache()
}