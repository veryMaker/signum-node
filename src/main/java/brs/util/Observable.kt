package brs.util

import java.util.function.Consumer

interface Observable<T, E : Enum<E>> {
    fun addListener(listener: (T) -> Unit, eventType: E): Boolean
    fun removeListener(listener: (T) -> Unit, eventType: E): Boolean
}
