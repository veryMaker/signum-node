package brs.util.sync

import java.util.concurrent.locks.StampedLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@UseExperimental(ExperimentalContracts::class)
inline fun <T> StampedLock.read(reader: () -> T): T {
    contract { callsInPlace(reader, InvocationKind.AT_LEAST_ONCE) }
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

@UseExperimental(ExperimentalContracts::class)
inline fun StampedLock.write(writer: () -> Unit) {
    contract { callsInPlace(writer, InvocationKind.EXACTLY_ONCE) }
    val stamp = this.writeLock()
    try {
        writer()
    } finally {
        this.unlockWrite(stamp)
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun <T> StampedLock.writeAndRead(action: () -> T): T {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    val stamp = this.writeLock()
    try {
        return action()
    } finally {
        this.unlockWrite(stamp)
    }
}
