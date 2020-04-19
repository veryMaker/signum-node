package brs.services

import org.ehcache.Cache

interface DBCacheService : AutoCloseable {

    /**
     * TODO
     */
    fun <K, V> getCache(name: String, keyClass: Class<K>, valueClass: Class<V>): Cache<K, V>?

    /**
     * TODO
     */
    fun flushCache()
}
