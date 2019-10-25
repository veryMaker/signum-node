package brs.util

interface Observable<T, E : Enum<E>> {
    fun addListener(eventType: E, listener: (T) -> Unit)
}
