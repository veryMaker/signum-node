package brs.util

import kotlin.reflect.KProperty

class AtomicLazy<T>(private val lazy: () -> T): Atomic<T>() {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return ref.get() ?: lazy()
    }
}