package brs.util

import java.util.function.Consumer

interface Observable<T, E : Enum<E>> {
    fun addListener(listener: Consumer<T>, eventType: E): Boolean
    fun removeListener(listener: Consumer<T>, eventType: E): Boolean
}
