package brs.util.delegates

import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AtomicLateinit<T : Any> : ReadWriteProperty<Any?, T> {
    private val ref = AtomicReference<T>()

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return ref.get() ?: throw UninitializedPropertyAccessException()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        ref.set(value)
    }
}
