package brs.db

import brs.entity.DependencyProvider
import org.ehcache.Cache

interface CachedTable<K, V> {
    val cacheKeyClass: Class<K>
    val cacheValueClass: Class<V>
    val cacheName: String
}

fun <K, V> CachedTable<K, V>.getCache(dp: DependencyProvider): Cache<K, V> {
    return dp.dbCacheService.getCache(cacheName, cacheKeyClass, cacheValueClass)!!
}
