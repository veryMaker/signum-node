package brs.util

import java.util.concurrent.locks.StampedLock
import java.util.function.Supplier

object StampedLockUtils {
    @JvmStatic
    fun <T> stampedLockRead(lock: StampedLock, supplier: Supplier<T>): T {
        var stamp = lock.tryOptimisticRead()
        var retVal = supplier.get()
        if (!lock.validate(stamp)) {
            stamp = lock.readLock()
            try {
                retVal = supplier.get()
            } finally {
                lock.unlockRead(stamp)
            }
        }
        return retVal
    }
}
