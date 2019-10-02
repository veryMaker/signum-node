package brs.util

interface Observable<T, E : Enum<E>> {
    suspend fun addListener(eventType: E, listener: suspend (T) -> Unit)
}
