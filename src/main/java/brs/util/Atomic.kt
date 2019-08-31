package brs.util

import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class Atomic<T>: ReadWriteProperty<Any?, T> {
    protected val ref = AtomicReference<T>()

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return ref.get()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        ref.set(value)
    }
}