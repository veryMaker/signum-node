package brs.props

import brs.Burst
import brs.util.logging.LogMessageProducer
import brs.util.logging.safeInfo
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.reflect.KClass

class PropertyServiceImpl(private val properties: Properties) : PropertyService {
    private val logger = LoggerFactory.getLogger(Burst::class.java)
    private val parsers: Map<KClass<*>, (String) -> Any>

    private val alreadyLoggedProperties = mutableListOf<String>()

    init {
        val parsers = mutableMapOf<KClass<*>, (String) -> Any>()
        parsers[String::class] = { this.getString(it) }
        parsers[Int::class] = { this.getInt(it) }
        parsers[Boolean::class] = { this.getBoolean(it) }
        parsers[List::class] = { this.getStringList(it) }
        this.parsers = parsers
    }

    // TODO caching
    override operator fun <T: Any> get(propName: String, defaultValue: T): T {
        val value = properties.getProperty(propName) ?: return defaultValue
        try {
            parsers.forEach { (type, parser) ->
                if (type.isInstance(defaultValue)) {
                    val parsed = parser(value)
                    if (!type.isInstance(parsed)) {
                        logger.safeInfo { "Property parser returned type ${parsed.javaClass}, was looking for type ${defaultValue.javaClass}, using default value $defaultValue" }
                        return defaultValue
                    }
                    logOnce(propName) { "${propName}: ${parsed.toString()}" }

                    return parsed as T // TODO no unchecked
                }
            }
        } catch (e: Exception) {
            logger.safeInfo { "Failed to parse property $propName, using default value $defaultValue" }
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

    private inline fun logOnce(propertyName: String, logText: LogMessageProducer) {
        if (propertyName == Props.SOLO_MINING_PASSPHRASES.name) return
        if (!this.alreadyLoggedProperties.contains(propertyName)) {
            this.logger.safeInfo(logText)
            this.alreadyLoggedProperties.add(propertyName)
        }
    }
}
