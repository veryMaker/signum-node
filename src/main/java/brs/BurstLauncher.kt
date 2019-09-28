package brs

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.GraphicsEnvironment
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.net.URL
import java.net.URLClassLoader

object BurstLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val logger = LoggerFactory.getLogger(BurstLauncher::class.java)
        var canRunGui = true

        addToClasspath(logger, "./conf")

        if (args.contains("--headless")) {
            logger.info("Running in headless mode as specified by argument")
            canRunGui = false
        }

        if (canRunGui && GraphicsEnvironment.isHeadless()) {
            logger.error("Cannot start GUI as running in headless environment")
            canRunGui = false
        }

        if (canRunGui) {
            try {
                Class.forName("javafx.application.Application")
            } catch (e: ClassNotFoundException) {
                logger.error("Could not start GUI as your JRE does not seem to have JavaFX installed. To install please install the \"openjfx\" package (eg. \"sudo apt install openjfx\")")
                canRunGui = false
            }

        }

        if (canRunGui) {
            try {
                Class.forName("brs.BurstGUI")
                        .getDeclaredMethod("main", Array<String>::class.java)
                        .invoke(null, args as Any)
            } catch (e: ClassNotFoundException) {
                logger.warn("Your build does not seem to include the BurstGUI extension or it cannot be run. Running as headless...")
                Burst.init(true)
            } catch (e: NoSuchMethodException) {
                logger.warn("Your build does not seem to include the BurstGUI extension or it cannot be run. Running as headless...")
                Burst.init(true)
            } catch (e: IllegalAccessException) {
                logger.warn("Your build does not seem to include the BurstGUI extension or it cannot be run. Running as headless...")
                Burst.init(true)
            } catch (e: InvocationTargetException) {
                logger.warn("Your build does not seem to include the BurstGUI extension or it cannot be run. Running as headless...")
                Burst.init(true)
            }
        } else {
            Burst.init(true)
        }
    }

    // TODO this is broken on some JVMs. We should come up with a better way to do this
    private fun addToClasspath(logger: Logger, path: String) {
        try {
            val f = File(path)
            val u = f.toURI()
            val urlClassLoader = ClassLoader.getSystemClassLoader() as URLClassLoader
            val urlClass = URLClassLoader::class.java
            val method = urlClass.getDeclaredMethod("addURL", URL::class.java)
            method.isAccessible = true
            method.invoke(urlClassLoader, u.toURL())
        } catch (e: Exception) {
            logger.warn("Could not add path \"$path\" to classpath", e)
        }

    }
}
