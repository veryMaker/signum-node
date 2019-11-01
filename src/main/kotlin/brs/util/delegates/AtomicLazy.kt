package brs.util.delegates

import kotlin.reflect.KProperty

class AtomicLazy<T>(private val lazy: () -> T) : Atomic<T>() {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        // TODO syncronize this, don't want lazy being called twice on different threads simultaneously
        var value = ref.get()
        if (value == null) {
            value = lazy()
            setValue(thisRef, property, value)
        }
        return value
    }
}