package brs.util.sync

import java.util.concurrent.locks.StampedLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@UseExperimental(ExperimentalContracts::class)
inline fun <T> StampedLock.read(reader: () -> T): T {
    contract { callsInPlace(reader, InvocationKind.AT_LEAST_ONCE) }
    val stamp = this.tryOptimisticRead()
    if (stamp == 0L) {
        return this.forceRead(reader)
    }
    val retVal = reader()
    if (!this.validate(stamp)) {
        return this.forceRead(reader)
    }
    return retVal
}

@UseExperimental(ExperimentalContracts::class)
inline fun <T> StampedLock.forceRead(reader: () -> T): T {
    contract { callsInPlace(reader, InvocationKind.EXACTLY_ONCE) }
    val stamp = this.readLock()
    try {
        return reader()
    } finally {
        this.unlockRead(stamp)
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun <T> StampedLock.write(writer: () -> T): T {
    contract { callsInPlace(writer, InvocationKind.EXACTLY_ONCE) }
    val stamp = this.writeLock()
    try {
        return writer()
    } finally {
        this.unlockWrite(stamp)
    }
}
