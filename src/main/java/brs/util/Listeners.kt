package brs.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class Listeners<T, E : Enum<E>> {
    private val listenersMap = ConcurrentHashMap<Enum<E>, MutableList<(T) -> Unit>>() // Remember, this map type cannot take null keys.

    // TODO reverse argument order to allow for Kotlin's special syntax
    fun addListener(listener: (T) -> Unit, eventType: Enum<E>): Boolean {
        synchronized(this) {
            val listeners = listenersMap.computeIfAbsent(eventType) { CopyOnWriteArrayList() }
            return listeners.add(listener)
        }
    }

    fun removeListener(listener: (T) -> Unit, eventType: Enum<E>): Boolean {
        synchronized(this) {
            val listeners = listenersMap[eventType]
            if (listeners != null) {
                return listeners.remove(listener)
            }
        }
        return false
    }

    fun accept(t: T, eventType: Enum<E>) {
        val listeners = listenersMap[eventType]
        if (listeners != null) {
            for (listener in listeners) {
                listener(t)
            }
        }
    }
}
