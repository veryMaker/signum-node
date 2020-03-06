package brs.util.cache

import org.ehcache.Cache
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <K, V> Cache<K, V>.tryCache(key: K, lookup: () -> V?): V? {
    contract { callsInPlace(lookup, InvocationKind.AT_MOST_ONCE) }
    if (this.containsKey(key)) return this.get(key)
    val newValue = lookup() ?: return null
    this.put(key, newValue)
    return newValue
}
