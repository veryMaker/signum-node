package brs.util

import java.util.concurrent.locks.StampedLock

class Listeners<T, E : Enum<E>> {
    private val listenersMap = mutableMapOf<Enum<E>, MutableList<suspend (T) -> Unit>>() // Remember, this map type cannot take null keys.
    private val stampedLock = StampedLock()

    suspend fun addListener(eventType: Enum<E>, listener: suspend (T) -> Unit) {
        stampedLock.write {
            listenersMap.computeIfAbsent(eventType) { mutableListOf() }.add(listener)
        }
    }

    suspend fun accept(eventType: Enum<E>, t: T) {
        // Read via the stamped lock from the map, return if null, otherwise each listener should accept the value.
        (stampedLock.read { listenersMap[eventType] } ?: return).forEach { it(t) }
    }
}
