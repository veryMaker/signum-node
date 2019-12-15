package brs.util

import java.io.PrintWriter
import java.io.StringWriter
import java.text.MessageFormat
import java.util.*
import java.util.logging.Formatter
import java.util.logging.LogRecord
import java.util.logging.Logger

/**
 * A Java logging formatter that writes more compact output than the default
 */
internal object BriefLogFormatter : Formatter() {
    /**
     * Format the log record as follows:
     *
     * Date Level Message ExceptionTrace
     *
     * @param       logRecord       The log record
     * @return                      The formatted string
     */
    override fun format(logRecord: LogRecord): String {
        val arguments = arrayOfNulls<Any>(5)
        arguments[0] = Date(logRecord.millis)
        arguments[1] = logRecord.level.name
        arguments[2] = logRecord.message
        arguments[4] = logRecord.loggerName

        val exc = logRecord.thrown
        if (exc != null) {
            val result = StringWriter()
            exc.printStackTrace(PrintWriter(result))
            arguments[3] = result.toString()
        } else {
            arguments[3] = ""
        }

        arguments[4] = logRecord.loggerName

        return messageFormat.format(arguments)
    }

    /** Format used for log messages */
    private val messageFormat = MessageFormat("[{1}] {0,date,yyyy-MM-dd HH:mm:ss} {4} - {2}\n{3}")

    /** LoggerConfigurator instance at the top of the name tree  */
    private val logger = Logger.getLogger("")

    /**
     * Configures JDK logging to use this class for everything
     */
    fun init() {
        val handlers = logger.handlers
        for (handler in handlers)
            handler.formatter = BriefLogFormatter
    }
}
