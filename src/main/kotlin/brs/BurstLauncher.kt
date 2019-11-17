package brs

import brs.util.logging.safeError
import brs.util.logging.safeInfo
import brs.util.logging.safeWarn
import org.slf4j.LoggerFactory
import java.awt.GraphicsEnvironment

object BurstLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val logger = LoggerFactory.getLogger(BurstLauncher::class.java)
        var canRunGui = true

        if (args.contains("--headless")) {
            logger.safeInfo { "Running in headless mode as specified by argument" }
            canRunGui = false
        }

        if (canRunGui && GraphicsEnvironment.isHeadless()) {
            logger.safeError { "Cannot start GUI as running in headless environment" }
            canRunGui = false
        }

        if (canRunGui) {
            try {
                Class.forName("javafx.application.Application")
            } catch (e: ClassNotFoundException) {
                logger.safeError { "Could not start GUI as your JRE does not seem to have JavaFX installed. To install please install the \"openjfx\" package (eg. \"sudo apt install openjfx\")" }
                canRunGui = false
            }
        }

        if (canRunGui) {
            try {
                Class.forName("brs.BurstGUI")
                    .getDeclaredMethod("main", Array<String>::class.java)
                    .invoke(null, args as Any)
            } catch (e: Exception) {
                logger.safeWarn { "Your build does not seem to include the BurstGUI extension or it cannot be run. Running as headless..." }
                Burst.init(true)
            }
        } else {
            Burst.init(true)
        }
    }
}
