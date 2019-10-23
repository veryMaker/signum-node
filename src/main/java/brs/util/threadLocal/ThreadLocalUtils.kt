package brs.util.threadLocal

import kotlinx.coroutines.ensurePresent
import kotlinx.coroutines.isPresent

suspend fun <T: Any> ThreadLocal<T>.getSafely(): T? {
    return if (isPresent()) get() else null
}

suspend fun <T> ThreadLocal<T>.setSafely(t: T) {
    ensurePresent()
    set(t)
}

suspend fun <T> ThreadLocal<T>.removeSafely() {
    ensurePresent()
    remove()
}