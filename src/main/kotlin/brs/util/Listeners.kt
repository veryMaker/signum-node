package brs.util

import brs.util.sync.Mutex

class Listeners<T, E : Enum<E>> {
    private val listenersMap = mutableMapOf<Enum<E>, MutableList<(T) -> Unit>>() // Remember, this map type cannot take null keys.
    private val mutex = Mutex()

    fun addListener(eventType: Enum<E>, listener: (T) -> Unit) {
        mutex.withLock {
            listenersMap.computeIfAbsent(eventType) { mutableListOf() }.add(listener)
        }
    }

    fun accept(eventType: Enum<E>, t: T) {
        // Read via the stamped lock from the map, return if null, otherwise each listener should accept the value.
        mutex.withLock { (listenersMap[eventType] ?: return).forEach { it(t) } }
        // TODO parallel processing?
    }
}
