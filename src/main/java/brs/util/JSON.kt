package brs.util

import brs.Constants.PROTOCOL
import com.google.gson.*
import com.google.gson.stream.JsonReader
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.io.Writer
import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Supplier
import java.util.stream.Collector

object JSON {
    val emptyJSON: JsonElement = JsonObject()

    fun prepareRequest(json: JsonObject): JsonElement {
        json.addProperty(PROTOCOL, "B1")
        return json
    }

    fun getAsJsonObject(jsonElement: JsonElement?): JsonObject {
        return if (jsonElement != null && jsonElement.isJsonObject) jsonElement.asJsonObject else JsonObject()
    }

    fun getAsJsonArray(jsonElement: JsonElement?): JsonArray {
        return if (jsonElement != null && jsonElement.isJsonArray) jsonElement.asJsonArray else JsonArray()
    }

    fun getAsString(jsonElement: JsonElement?): String { // TODO should this be a nullable return type?
        return if (jsonElement != null && jsonElement.isJsonPrimitive) jsonElement.asString else ""
    }

    fun getAsLong(jsonElement: JsonElement?): Long {
        return if (jsonElement != null && jsonElement.isJsonPrimitive) jsonElement.asLong else 0
    }

    fun getAsInt(jsonElement: JsonElement?): Int {
        return if (jsonElement != null && jsonElement.isJsonPrimitive) jsonElement.asInt else 0
    }

    fun getAsShort(jsonElement: JsonElement?): Short {
        return if (jsonElement != null && jsonElement.isJsonPrimitive) jsonElement.asShort else 0
    }

    fun getAsByte(jsonElement: JsonElement?): Byte {
        return if (jsonElement != null && jsonElement.isJsonPrimitive) jsonElement.asByte else 0
    }

    fun getAsBoolean(jsonElement: JsonElement?): Boolean {
        return jsonElement != null && jsonElement.isJsonPrimitive && jsonElement.asBoolean
    }
}

fun JsonElement.writeTo(writer: Writer) {
    writer.write(this.toJsonString())
    writer.flush()
}

fun JsonObject.addAll(objectToAdd: JsonObject) {
    for ((key, value) in objectToAdd.entrySet()) {
        this.add(key, value)
    }
}

fun JsonElement?.toJsonString(): String {
    // TODO do we really need a nullable receiver?
    return this?.toString() ?: JsonNull.INSTANCE.toString()
}

fun JsonElement.cloneJson(): JsonElement {
    return this.toJsonString().parseJson()
}

fun Reader.parseJson(): JsonElement {
    val json = JsonParser().parse(if (this is JsonReader) this else JsonReader(this))
    if (json.isJsonPrimitive) {
        throw JsonParseException("Json is primitive, was probably bad json interpreted as string")
    }
    return json
}

fun String.parseJson(): JsonElement {
    return StringReader(this).parseJson()
}

fun <T: JsonElement> Iterable<T>.toJsonArray(): JsonArray {
    val jsonArray = JsonArray()
    for (item in this) jsonArray.add(item)
    return jsonArray
}

fun JsonArray.isEmpty() = this.size() == 0

fun JsonObject.isEmpty() = this.size() == 0
