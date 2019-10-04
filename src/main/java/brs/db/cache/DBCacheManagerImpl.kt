package brs.db.cache

import brs.Account
import brs.DependencyProvider
import brs.db.BurstKey
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.Status
import org.ehcache.config.CacheConfiguration
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder

class DBCacheManagerImpl(private val dp: DependencyProvider) { // TODO interface
    private val cacheManager: CacheManager
    private val caches = mutableMapOf<String, CacheConfiguration<BurstKey, *>>()

    init {
        caches["account"] = CacheConfigurationBuilder.newCacheConfigurationBuilder(BurstKey::class.java, Account::class.java, ResourcePoolsBuilder.heap(8192)).build()
        var cacheBuilder: CacheManagerBuilder<*> = CacheManagerBuilder.newCacheManagerBuilder()
        for ((key, value) in caches) {
            cacheBuilder = cacheBuilder.withCache(key, value)
        }
        cacheManager = cacheBuilder.build(true)
    }

    fun close() {
        if (cacheManager.status == Status.AVAILABLE) {
            cacheManager.close()
        }
    }

    private fun <V> getEHCache(name: String, valueClass: Class<V>): Cache<BurstKey, V>? {
        return cacheManager.getCache(name, BurstKey::class.java, valueClass)
    }

    fun <V> getCache(name: String, valueClass: Class<V>): Cache<BurstKey, V>? {
        val cache = getEHCache(name, valueClass) ?: return null
        return StatisticsCache(cache, name, dp.statisticsManager)
    }

    fun flushCache() {
        for ((key, value) in caches) {
            val cache = getEHCache(key, value.valueType)
            cache?.clear()
        }
    }
}
