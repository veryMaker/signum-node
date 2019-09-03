package brs.util.atomic

import kotlin.reflect.KProperty

class AtomicLazy<T>(private val lazy: () -> T): Atomic<T>() {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return ref.get() ?: lazy()
    }
}