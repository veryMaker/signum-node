package brs.util.delegates

import kotlin.reflect.KProperty

class AtomicWithOverride<T>(
    initialValue: T? = null,
    private val getValueDelegate: ((get: () -> T) -> T) = { it() },
    private val setValueDelegate: (((value: T, set: (T) -> Unit) -> Unit)) = { value, setter ->
        setter(value)
    }
) : Atomic<T>(initialValue) {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getValueDelegate { super.getValue(thisRef, property) }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        setValueDelegate(value) { super.setValue(thisRef, property, it) }
    }
}