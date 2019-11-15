package brs.util.rxjava

import brs.util.logging.safeError
import io.reactivex.functions.Consumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object RxJavaUtils {
    private val logger: Logger = LoggerFactory.getLogger(RxJavaUtils::class.java)

    val defaultErrorHandler: Consumer<Throwable> = Consumer { handleError(it) }

    fun handleError(t: Throwable) {
        logger.safeError(t) { "Uncaught error in an RxJava process" }
    }
}