package brs.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

class Listeners<T, E : Enum<E>> {
    private val listenersMap = ConcurrentHashMap<Enum<E>, MutableList<(T) -> Unit>>()

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
