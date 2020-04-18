package brs.services.impl

import brs.db.BurstKey
import brs.entity.*
import brs.objects.Props
import brs.services.DBCacheService
import org.ehcache.Cache
import org.ehcache.CacheManager
import org.ehcache.Status
import org.ehcache.config.CacheConfiguration
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.ehcache.config.units.EntryUnit

class DBCacheServiceImpl(private val dp: DependencyProvider) : DBCacheService {
    private val cacheManager: CacheManager
    private val caches: Map<String, CacheConfiguration<*, *>>

    init {
        val caches = mutableMapOf<String, CacheConfiguration<*, *>>()
        val resourcePoolBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .heap(dp.propertyService.get(Props.MAX_CACHED_ENTITIES).toLong(), EntryUnit.ENTRIES)
        caches["account"] = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            BurstKey::class.java,
            Account::class.java,
            resourcePoolBuilder
        ).build()
        caches["indirect_incoming"] = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            BurstKey::class.java,
            IndirectIncoming::class.java,
            resourcePoolBuilder
        ).build()
        caches["block_id"] = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Long::class.javaObjectType,
            Block::class.java,
            resourcePoolBuilder
        ).build()
        caches["block_height"] = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Int::class.javaObjectType,
            Block::class.java,
            resourcePoolBuilder
        ).build()
        caches["transaction"] = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Long::class.javaObjectType,
            Transaction::class.java,
            resourcePoolBuilder
        ).build()
        var cacheBuilder = CacheManagerBuilder.newCacheManagerBuilder()
        for ((key, value) in caches) {
            cacheBuilder = cacheBuilder.withCache(key, value)
        }
        this.caches = caches
        cacheManager = cacheBuilder.build(true)
    }

    override fun close() {
        if (cacheManager.status == Status.AVAILABLE) {
            cacheManager.close()
        }
    }

    override fun <V> getCache(name: String, valueClass: Class<V>): Cache<BurstKey, V>? { // TODO this ain't gonna work!
        val cache = cacheManager.getCache(name, BurstKey::class.java, valueClass) ?: return null
        return StatisticsCache(cache, name, dp.statisticsService) // TODO constructing this every time sucks
    }

    override fun <K, V> getCache(name: String, keyClass: Class<K>, valueClass: Class<V>): Cache<K, V>? {
        return cacheManager.getCache(name, keyClass, valueClass)
    }

    override fun flushCache() {
        for ((key, value) in caches) {
            val cache = cacheManager.getCache(key, value.keyType, value.valueType)
            cache?.clear()
        }
    }
}
