package brs.util

import brs.Constants.PROTOCOL
import com.google.gson.*
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.io.Writer
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Supplier
import java.util.stream.Collector

object JSON {

    @JvmField
    val emptyJSON: JsonElement = JsonObject()

    @JvmStatic
    fun prepareRequest(json: JsonObject): JsonElement {
        json.addProperty(PROTOCOL, "B1")
        return json
    }

    @JvmStatic
    fun parse(jsonString: String): JsonElement {
        return parse(StringReader(jsonString))
    }

    @JvmStatic
    fun parse(jsonReader: Reader): JsonElement {
        val json = JsonParser().parse(jsonReader)
        if (json.isJsonPrimitive) {
            throw JsonParseException("Json is primitive, was probably bad json interpreted as string")
        }
        return json
    }

    @JvmStatic
    fun cloneJson(json: JsonElement): JsonElement {
        return parse(toJsonString(json))
    }

    @JvmStatic
    fun addAll(parent: JsonObject, objectToAdd: JsonObject) {
        for ((key, value) in objectToAdd.entrySet()) {
            parent.add(key, value)
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun writeTo(jsonElement: JsonElement, writer: Writer) {
        writer.write(toJsonString(jsonElement))
        writer.flush()
    }

    @JvmStatic
    fun toJsonString(jsonElement: JsonElement?): String {
        return jsonElement?.toString() ?: "null"
    }

    @JvmStatic
    fun getAsJsonObject(jsonElement: JsonElement?): JsonObject {
        return if (jsonElement != null && jsonElement.isJsonObject) jsonElement.asJsonObject else JsonObject()
    }

    @JvmStatic
    fun getAsJsonArray(jsonElement: JsonElement?): JsonArray {
        return if (jsonElement != null && jsonElement.isJsonArray) jsonElement.asJsonArray else JsonArray()
    }

    @JvmStatic
    fun getAsString(jsonElement: JsonElement?): String? {
        return if (jsonElement != null && jsonElement.isJsonPrimitive) jsonElement.asString else null
    }

    @JvmStatic
    fun getAsLong(jsonElement: JsonElement?): Long {
        return if (jsonElement != null && jsonElement.isJsonPrimitive) jsonElement.asLong else 0
    }

    @JvmStatic
    fun getAsInt(jsonElement: JsonElement?): Int {
        return if (jsonElement != null && jsonElement.isJsonPrimitive) jsonElement.asInt else 0
    }

    @JvmStatic
    fun getAsShort(jsonElement: JsonElement?): Short {
        return if (jsonElement != null && jsonElement.isJsonPrimitive) jsonElement.asShort else 0
    }

    @JvmStatic
    fun getAsByte(jsonElement: JsonElement?): Byte {
        return if (jsonElement != null && jsonElement.isJsonPrimitive) jsonElement.asByte else 0
    }

    @JvmStatic
    fun getAsBoolean(jsonElement: JsonElement?): Boolean {
        return jsonElement != null && jsonElement.isJsonPrimitive && jsonElement.asBoolean
    }

    @JvmStatic
    fun <T : JsonElement> jsonArrayCollector(): Collector<T, *, JsonArray> {
        return Collector.of(Supplier<JsonArray> { JsonArray() }, BiConsumer<JsonArray, T> { obj, element -> obj.add(element) }, BinaryOperator<JsonArray> { left: JsonArray, right: JsonArray ->
            left.addAll(right)
            left
        })
    }
}
