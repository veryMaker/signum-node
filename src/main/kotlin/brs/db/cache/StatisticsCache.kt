package brs.db.cache

import brs.services.StatisticsService
import org.ehcache.Cache
import org.ehcache.config.CacheRuntimeConfiguration

internal class StatisticsCache<K, V>(private val wrappedCache: Cache<K, V>, private val cacheName: String, private val statisticsService: StatisticsService) : Cache<K, V> {
    override fun get(k: K): V {
        return wrappedCache.get(k)
    }

    override fun put(k: K, v: V) {
        wrappedCache.put(k, v)
    }

    override fun containsKey(k: K): Boolean {
        val result = wrappedCache.containsKey(k)

        if (result) {
            statisticsService.foundObjectInCache(cacheName)
        } else {
            statisticsService.didNotFindObjectInCache(cacheName)
        }

        return result
    }

    override fun remove(k: K) {
        wrappedCache.remove(k)
    }

    override fun getAll(set: Set<K>): Map<K, V> {
        return wrappedCache.getAll(set)
    }

    override fun putAll(map: Map<out K, V>) {
        wrappedCache.putAll(map)
    }

    override fun removeAll(set: Set<K>) {
        wrappedCache.removeAll(set)
    }

    override fun clear() {
        wrappedCache.clear()
    }

    override fun putIfAbsent(k: K, v: V): V {
        return wrappedCache.putIfAbsent(k, v)
    }

    override fun remove(k: K, v: V): Boolean {
        return wrappedCache.remove(k, v)
    }

    override fun replace(k: K, v: V): V {
        return wrappedCache.replace(k, v)
    }

    override fun replace(k: K, v: V, v1: V): Boolean {
        return wrappedCache.replace(k, v, v1)
    }

    override fun getRuntimeConfiguration(): CacheRuntimeConfiguration<K, V> {
        return wrappedCache.runtimeConfiguration
    }

    override fun iterator(): MutableIterator<Cache.Entry<K, V>> {
        return wrappedCache.iterator()
    }
}
