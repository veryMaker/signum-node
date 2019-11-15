package brs.util.delegates

import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class Atomic<T>(initialValue: T) : ReadWriteProperty<Any?, T> {
    protected val ref = AtomicReference<T>()

    init {
        ref.set(initialValue)
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return ref.get()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        ref.set(value)
    }
}