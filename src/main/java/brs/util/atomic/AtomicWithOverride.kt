package brs.util.atomic

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// TODO this is unused...
class AtomicWithOverride<T>(private val getValueDelegate: ((() -> T) -> T) = { it() }, private val setValueDelegate: (((T, (T) -> Unit) -> Unit)) = { value, setter -> setter(value) }): Atomic<T>() {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getValueDelegate { super.getValue(thisRef, property) }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        setValueDelegate(value) { super.setValue(thisRef, property, it) }
    }
}