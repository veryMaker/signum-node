package brs.services

import brs.db.BurstKey
import org.ehcache.Cache

interface DBCacheService : AutoCloseable {
    /**
     * TODO
     */
    fun <V> getCache(name: String, valueClass: Class<V>): Cache<BurstKey, V>?

    /**
     * TODO
     */
    fun <K, V> getCache(name: String, keyClass: Class<K>, valueClass: Class<V>): Cache<K, V>?

    /**
     * TODO
     */
    fun flushCache()
}
