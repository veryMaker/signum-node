package brs.util.delegates

import brs.util.sync.Mutex
import brs.util.sync.withLock
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AtomicLazy<T>(private val lazy: () -> T) : ReadWriteProperty<Any?, T> {
    private val ref = AtomicReference<T>()
    private val mutex = Mutex()
    private var initialized by Atomic(false)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        mutex.withLock {
            return if (initialized) ref.get() else {
                val lazyResult = lazy()
                ref.set(lazyResult)
                initialized = true
                lazyResult
            }
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        mutex.withLock {
            ref.set(value)
            initialized = true
        }
    }
}
