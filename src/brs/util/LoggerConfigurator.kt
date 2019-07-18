package brs.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Properties
import java.util.logging.LogManager
import java.util.logging.Logger

/**
 * Handle logging for the Burst node server
 */

object LoggerConfigurator {
    private val logger = Logger.getLogger(LoggerConfigurator::class.java.simpleName)

    /**
     * LoggerConfigurator initialization
     *
     * The existing Java logging configuration will be used if the Java logger has already
     * been initialized.  Otherwise, we will configure our own log manager and log handlers.
     * The conf/logging-default.properties and conf/logging.properties configuration
     * files will be used.  Entries in logging.properties will override entries in
     * logging-default.properties.
     */
    fun init() {
        val managerPackage = "java.util.logging.manager"
        val oldManager = System.getProperty(managerPackage)
        System.setProperty(managerPackage, "brs.util.BurstLogManager")
        if (LogManager.getLogManager() !is BurstLogManager) {
            System.setProperty(managerPackage,
                    oldManager ?: "java.util.logging.LogManager")
        }
        if (!java.lang.Boolean.getBoolean("brs.doNotConfigureLogging")) {
            try {
                var foundProperties = false
                val loggingProperties = Properties()
                ClassLoader.getSystemResourceAsStream("logging-default.properties").use { `is` ->
                    if (`is` != null) {
                        loggingProperties.load(`is`)
                        foundProperties = true
                    }
                }
                ClassLoader.getSystemResourceAsStream("logging.properties").use { `is` ->
                    if (`is` != null) {
                        loggingProperties.load(`is`)
                        foundProperties = true
                    }
                }
                if (foundProperties) {
                    val outStream = ByteArrayOutputStream()
                    loggingProperties.store(outStream, "logging properties")
                    val inStream = ByteArrayInputStream(outStream.toByteArray())
                    java.util.logging.LogManager.getLogManager().readConfiguration(inStream)
                    inStream.close()
                    outStream.close()
                }
                BriefLogFormatter.init()
            } catch (e: IOException) {
                throw RuntimeException("Error loading logging properties", e)
            }

        }

        logger.info("logging enabled")
    }

    /**
     * LoggerConfigurator shutdown
     */
    fun shutdown() {
        if (LogManager.getLogManager() is BurstLogManager) {
            (LogManager.getLogManager() as BurstLogManager).burstShutdown()
        }
    }
}
/**
 * No constructor
 */
