package brs.util

import brs.util.sync.read
import brs.util.sync.write
import java.util.concurrent.locks.StampedLock

class Listeners<T, E : Enum<E>> {
    private val listenersMap = mutableMapOf<Enum<E>, MutableList<(T) -> Unit>>()
    private val stampedLock = StampedLock()

    fun addListener(eventType: Enum<E>, listener: (T) -> Unit) {
        stampedLock.write {
            listenersMap.computeIfAbsent(eventType) { mutableListOf() }.add(listener)
        }
    }

    fun accept(eventType: Enum<E>, t: T) {
        // Read via the stamped lock from the map, return if null, otherwise each listener should accept the value.
        (stampedLock.read { listenersMap[eventType] } ?: return).forEach { it(t) }
    }
}
