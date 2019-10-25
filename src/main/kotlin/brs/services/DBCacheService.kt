package brs.services

import brs.db.BurstKey
import org.ehcache.Cache

interface DBCacheService {
    fun close()
    fun <V> getCache(name: String, valueClass: Class<V>): Cache<BurstKey, V>?
    fun flushCache()
}