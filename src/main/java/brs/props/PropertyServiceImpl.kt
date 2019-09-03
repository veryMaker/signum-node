package brs.props

import brs.Burst
import org.slf4j.LoggerFactory

import java.util.*
import java.util.function.Function
import kotlin.reflect.KClass

class PropertyServiceImpl(private val properties: Properties) : PropertyService {
    private val logger = LoggerFactory.getLogger(Burst::class.java)
    private val parsers: Map<KClass<*>, Function<String, *>>

    private val alreadyLoggedProperties = mutableListOf<String>()

    init {
        val parsers = mutableMapOf<KClass<*>, Function<String, *>>()
        parsers[String::class] = Function<String, String> { this.getString(it) }
        parsers[Int::class] = Function<String, Int> { this.getInt(it) }
        parsers[Boolean::class] = Function<String, Boolean> { this.getBoolean(it) }
        parsers[List::class] = Function<String, List<*>> { this.getStringList(it) }
        this.parsers = parsers
    }

    override operator fun <T: Any> get(propName: String, defaultValue: T): T {
        val value = properties.getProperty(propName) ?: return defaultValue
        try {
            for ((key, value1) in parsers) {
                if (key == defaultValue.javaClass.kotlin) {
                    val parsed = value1.apply(value)
                    if (!defaultValue.javaClass.isInstance(parsed)) {
                        throw RuntimeException("Property parser returned type " + parsed.javaClass + ", was looking for type " + defaultValue.javaClass)
                    }
                    logOnce(propName, false, "{}: {}", propName, parsed.toString())

                    return parsed as T
                }
            }
        } catch (e: Exception) {
            logger.info("Failed to parse property {}, using default value {}", propName, defaultValue.toString())
        }

        return defaultValue
    }

    fun getBoolean(value: String): Boolean {
        if (value.matches("(?i)^1|active|true|yes|on$".toRegex())) {
            return true
        }

        if (value.matches("(?i)^0|false|no|off$".toRegex())) {
            return false
        }
        throw IllegalArgumentException()
    }

    fun getInt(value: String?): Int {
        var value = value
        var radix = 10

        if (value != null && value.matches("(?i)^0x.+$".toRegex())) {
            value = value.replaceFirst("^0x".toRegex(), "")
            radix = 16
        } else if (value != null && value.matches("(?i)^0b[01]+$".toRegex())) {
            value = value.replaceFirst("^0b".toRegex(), "")
            radix = 2
        }

        return Integer.parseInt(value!!, radix)
    }

    fun getString(value: String): String {
        if (value.isNotEmpty()) {
            return value
        }

        throw IllegalArgumentException()
    }

    private fun getStringList(value: String): List<String> {
        if (value.isEmpty()) {
            return emptyList()
        }
        val result = mutableListOf<String>()
        for (s in value.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val s1 = s.trim { it <= ' ' }
            if (s1.isNotEmpty()) {
                result.add(s1)
            }
        }
        return result
    }

    private fun logOnce(propertyName: String, debugLevel: Boolean, logText: String, vararg arguments: Any) {
        if (propertyName == Props.SOLO_MINING_PASSPHRASES.name) return
        if (!this.alreadyLoggedProperties.contains(propertyName)) {
            if (debugLevel) {
                this.logger.debug(logText, *arguments)
            } else {
                this.logger.info(logText, *arguments)
            }
            this.alreadyLoggedProperties.add(propertyName)
        }
    }
}
