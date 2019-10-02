package brs.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Listeners<T, E : Enum<E>> {
    private val listenersMap = mutableMapOf<Enum<E>, MutableList<suspend (T) -> Unit>>() // Remember, this map type cannot take null keys.
    private val mutex = Mutex()

    suspend fun addListener(eventType: Enum<E>, listener: suspend (T) -> Unit) {
        mutex.withLock {
            listenersMap.computeIfAbsent(eventType) { mutableListOf() }.add(listener)
        }
    }

    suspend fun accept(eventType: Enum<E>, t: T) {
        // Read via the stamped lock from the map, return if null, otherwise each listener should accept the value.
        mutex.withLock { (listenersMap[eventType] ?: return).forEach { it(t) } }
        /*
        mutex.withLock {
            val listeners = listenersMap[eventType] ?: return
            coroutineScope {
                val jobs = mutableListOf<Job>()
                listeners.forEach {
                    jobs.add(launch { it(t) })
                }
                jobs.forEach { it.join() }
            }
        }
         */
    }
}
