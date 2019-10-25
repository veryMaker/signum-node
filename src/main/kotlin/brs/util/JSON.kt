package brs.util

import brs.Constants.PROTOCOL
import com.google.gson.*
import com.google.gson.stream.JsonReader
import java.io.Reader
import java.io.StringReader
import java.io.Writer

object JSON {
    val emptyJSON: JsonElement = JsonObject()

    fun prepareRequest(json: JsonObject): JsonElement {
        json.addProperty(PROTOCOL, "B1")
        return json
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

fun JsonElement?.safeGetAsJsonObject(): JsonObject? {
    return if (this != null && this.isJsonObject) this.asJsonObject else null
}

fun JsonElement?.mustGetAsJsonObject(fieldName: String): JsonObject {
    return if (this != null && this.isJsonObject) this.asJsonObject else error("JSON: Could not get $fieldName")
}

fun JsonElement?.safeGetAsJsonArray(): JsonArray? {
    return if (this != null && this.isJsonArray) this.asJsonArray else null
}

fun JsonElement?.mustGetAsJsonArray(fieldName: String): JsonArray {
    return if (this != null && this.isJsonArray) this.asJsonArray else error("JSON: Could not get $fieldName")
}

fun JsonElement?.safeGetAsString(): String? {
    return if (this != null && this.isJsonPrimitive) this.asString else null
}

fun JsonElement?.mustGetAsString(fieldName: String): String {
    return if (this != null && this.isJsonPrimitive) this.asString else error("JSON: Could not get $fieldName")
}

fun JsonElement?.safeGetAsLong(): Long? {
    return if (this != null && this.isJsonPrimitive) this.asLong else null
}

fun JsonElement?.mustGetAsLong(fieldName: String): Long {
    return if (this != null && this.isJsonPrimitive) this.asLong else error("JSON: Could not get $fieldName")
}

fun JsonElement?.safeGetAsInt(): Int? {
    return if (this != null && this.isJsonPrimitive) this.asInt else null
}

fun JsonElement?.mustGetAsInt(fieldName: String): Int {
    return if (this != null && this.isJsonPrimitive) this.asInt else error("JSON: Could not get $fieldName")
}

fun JsonElement?.safeGetAsShort(): Short? {
    return if (this != null && this.isJsonPrimitive) this.asShort else null
}

fun JsonElement?.mustGetAsShort(fieldName: String): Short {
    return if (this != null && this.isJsonPrimitive) this.asShort else error("JSON: Could not get $fieldName")
}

fun JsonElement?.safeGetAsByte(): Byte? {
    return if (this != null && this.isJsonPrimitive) this.asByte else null
}

fun JsonElement?.mustGetAsByte(fieldName: String): Byte {
    return if (this != null && this.isJsonPrimitive) this.asByte else error("JSON: Could not get $fieldName")
}

fun JsonElement?.safeGetAsBoolean(): Boolean? {
    return if (this != null && this.isJsonPrimitive) this.asBoolean else null
}

fun JsonElement?.mustGetAsBoolean(fieldName: String): Boolean {
    return if (this != null && this.isJsonPrimitive) this.asBoolean else error("JSON: Could not get $fieldName")
}
