package brs.util

import java.util.concurrent.locks.StampedLock
import java.util.function.Supplier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

inline fun <T> StampedLock.read(reader: () -> T): T {
    var stamp = this.tryOptimisticRead()
    var retVal = reader()
    if (!this.validate(stamp)) {
        stamp = this.readLock()
        try {
            retVal = reader()
        } finally {
            this.unlockRead(stamp)
        }
    }
    return retVal
}

inline fun StampedLock.write(writer: () -> Unit) {
    val stamp = this.writeLock()
    try {
        writer()
    } finally {
        this.unlockWrite(stamp)
    }
}

inline fun <T> StampedLock.writeAndRead(action: () -> T): T {
    val stamp = this.writeLock()
    try {
        return action()
    } finally {
        this.unlockWrite(stamp)
    }
}
