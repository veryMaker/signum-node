package brs.util.logging

import org.slf4j.Logger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

typealias LogMessageProducer = () -> String
typealias NullableLogMessageProducer = () -> String?

@UseExperimental(ExperimentalContracts::class)
inline fun Logger.safeTrace(messageProducer: LogMessageProducer) {
    contract { callsInPlace(messageProducer, InvocationKind.EXACTLY_ONCE) }
    if (this.isTraceEnabled) {
        this.trace(messageProducer())
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun Logger.safeTrace(t: Throwable, messageProducer: NullableLogMessageProducer) {
    contract { callsInPlace(messageProducer, InvocationKind.EXACTLY_ONCE) }
    if (this.isTraceEnabled) {
        this.trace(messageProducer(), t)
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun Logger.safeDebug(messageProducer: LogMessageProducer) {
    contract { callsInPlace(messageProducer, InvocationKind.EXACTLY_ONCE) }
    if (this.isDebugEnabled) {
        this.debug(messageProducer())
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun Logger.safeDebug(t: Throwable, messageProducer: NullableLogMessageProducer) {
    contract { callsInPlace(messageProducer, InvocationKind.EXACTLY_ONCE) }
    if (this.isDebugEnabled) {
        this.debug(messageProducer(), t)
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun Logger.safeInfo(messageProducer: LogMessageProducer) {
    contract { callsInPlace(messageProducer, InvocationKind.EXACTLY_ONCE) }
    if (this.isInfoEnabled) {
        this.info(messageProducer())
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun Logger.safeInfo(t: Throwable, messageProducer: NullableLogMessageProducer) {
    contract { callsInPlace(messageProducer, InvocationKind.EXACTLY_ONCE) }
    if (this.isInfoEnabled) {
        this.info(messageProducer(), t)
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun Logger.safeWarn(messageProducer: LogMessageProducer) {
    contract { callsInPlace(messageProducer, InvocationKind.EXACTLY_ONCE) }
    if (this.isWarnEnabled) {
        this.warn(messageProducer())
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun Logger.safeWarn(t: Throwable, messageProducer: NullableLogMessageProducer) {
    contract { callsInPlace(messageProducer, InvocationKind.EXACTLY_ONCE) }
    if (this.isWarnEnabled) {
        this.warn(messageProducer(), t)
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun Logger.safeError(messageProducer: LogMessageProducer) {
    contract { callsInPlace(messageProducer, InvocationKind.EXACTLY_ONCE) }
    if (this.isErrorEnabled) {
        this.error(messageProducer())
    }
}

@UseExperimental(ExperimentalContracts::class)
inline fun Logger.safeError(t: Throwable, messageProducer: NullableLogMessageProducer) {
    contract { callsInPlace(messageProducer, InvocationKind.EXACTLY_ONCE) }
    if (this.isErrorEnabled) {
        this.error(messageProducer(), t)
    }
}
