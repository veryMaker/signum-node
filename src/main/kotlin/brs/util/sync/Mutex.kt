package brs.util.sync

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Re-entrant lock
 */
inline class Mutex(val lock: Any = Any())

inline fun <T> Mutex.withLock(action: (waitTime: Long) -> T): T {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    val startTime = System.currentTimeMillis()
    return synchronized(lock) {
        action(System.currentTimeMillis() - startTime)
    }
}
