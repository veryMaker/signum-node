package brs.util.logging

import org.slf4j.Logger

typealias LogMessageProducer = () -> String

inline fun Logger.safeTrace(messageProducer: LogMessageProducer) {
    if (this.isTraceEnabled) {
        this.trace(messageProducer())
    }
}

inline fun Logger.safeDebug(messageProducer: LogMessageProducer) {
    if (this.isDebugEnabled) {
        this.debug(messageProducer())
    }
}

inline fun Logger.safeInfo(messageProducer: LogMessageProducer) {
    if (this.isInfoEnabled) {
        this.info(messageProducer())
    }
}

inline fun Logger.safeWarn(messageProducer: LogMessageProducer) {
    if (this.isWarnEnabled) {
        this.warn(messageProducer())
    }
}

inline fun Logger.safeError(messageProducer: LogMessageProducer) {
    if (this.isErrorEnabled) {
        this.error(messageProducer())
    }
}
