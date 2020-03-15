package brs.util.cache

import org.ehcache.Cache
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <K, V> Cache<K, V>.tryCache(key: K, lookup: () -> V?): V? {
    contract { callsInPlace(lookup, InvocationKind.AT_MOST_ONCE) }
    if (this.containsKey(key)) return this[key]
    val newValue = lookup() ?: return null
    this[key] = newValue
    return newValue
}

@Suppress("NOTHING_TO_INLINE", "EXTENSION_SHADOWED_BY_MEMBER")
inline operator fun <K, V> Cache<K, V>.get(key: K): V? = this.get(key)

@Suppress("NOTHING_TO_INLINE")
inline operator fun <K, V> Cache<K, V>.set(key: K, value: V) = this.put(key, value)
