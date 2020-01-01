package brs.services.impl

import brs.Burst
import brs.entity.Prop
import brs.objects.Props
import brs.services.PropertyService
import brs.util.logging.LogMessageProducer
import brs.util.logging.safeInfo
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.reflect.KClass

class PropertyServiceImpl(private val properties: Properties) : PropertyService {
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
    override operator fun <T : Any> get(prop: Prop<T>): T {
        val value = properties.getProperty(prop.name) ?: return prop.defaultValue
        try {
            parsers.forEach { (type, parser) ->
                if (type.isInstance(prop.defaultValue)) {
                    val parsed = parser(value)
                    if (!type.isInstance(parsed)) {
                        logger.safeInfo { "Property parser returned type ${parsed.javaClass}, was looking for type ${prop.defaultValue.javaClass}, using default value ${prop.defaultValue}" }
                        return prop.defaultValue
                    }
                    logOnce(prop.name) { "${prop.name}: $parsed" }
                    @Suppress("UNCHECKED_CAST")
                    return parsed as T
                }
            }
        } catch (e: Exception) {
            logger.safeInfo { "Failed to parse property ${prop.name}, using default value ${prop.defaultValue}" }
        }

        return prop.defaultValue
    }

    private fun getBoolean(value: String): Boolean {
        return when {
            value.matches(booleanTrueRegex) -> true
            value.matches(booleanFalseRegex) -> false
            else -> throw IllegalArgumentException()
        }
    }

    private fun getInt(value: String): Int {
        return when {
            value.matches(hexRegex) -> Integer.parseInt(value.replaceFirst("0x", ""), 16)
            value.matches(binaryRegex) -> Integer.parseInt(value.replaceFirst("0b", ""), 2)
            else -> Integer.parseInt(value, 10)
        }
    }

    private fun getString(value: String): String {
        require(value.isNotEmpty()) { "String property must not be empty" }
        return value.trim()
    }

    private fun getStringList(value: String): List<String> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(";")
                .map { element -> element.trim() }
                .filter { it.isNotEmpty() }
        }
    }

    private inline fun logOnce(propertyName: String, logText: LogMessageProducer) {
        if (propertyName == Props.SOLO_MINING_PASSPHRASES.name) return
        if (!this.alreadyLoggedProperties.contains(propertyName)) {
            logger.safeInfo(logText)
            this.alreadyLoggedProperties.add(propertyName)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Burst::class.java)

        private val hexRegex = Regex("(?i)^0x[0-9a-fA-F]+$")
        private val binaryRegex = Regex("(?i)^0b[01]+\$")
        private val booleanTrueRegex = Regex("(?i)^1|true|on|yes|active|enabled$")
        private val booleanFalseRegex = Regex("(?i)^0|false|off|no|inactive|disabled$")
    }
}
