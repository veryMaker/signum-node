package brs.util.json

import brs.objects.Constants.PROTOCOL
import com.google.gson.*
import com.google.gson.stream.JsonReader
import java.io.Reader
import java.io.StringReader
import java.io.Writer

object JSON {
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

fun Reader.parseJson(): JsonElement {
    val json: JsonElement = JsonParser.parseReader(if (this is JsonReader) this else JsonReader(this))
    if (json.isJsonPrimitive) {
        throw JsonParseException("Json is primitive, was probably bad json interpreted as string")
    }
    return json
}

fun String.parseJson(): JsonElement {
    return StringReader(this).parseJson()
}

fun <T : JsonElement> Iterable<T>.toJsonArray(): JsonArray {
    val jsonArray = JsonArray()
    for (item in this) jsonArray.add(item)
    return jsonArray
}

@Suppress("NOTHING_TO_INLINE")
inline fun JsonArray.isEmpty() = this.size() == 0

@Suppress("NOTHING_TO_INLINE")
inline fun JsonObject.isEmpty() = this.size() == 0

// Functions that are used directly on elements. Try to minimize uses of these

fun JsonElement?.safeGetAsJsonArray(): JsonArray? {
    return if (this != null && this.isJsonArray) this.asJsonArray else null
}

fun JsonElement?.safeGetAsJsonObject(): JsonObject? {
    return if (this != null && this.isJsonObject) this.asJsonObject else null
}

fun JsonElement?.mustGetAsJsonObject(fieldName: String): JsonObject {
    return if (this != null && this.isJsonObject) this.asJsonObject else error("JSON: Could not get $fieldName")
}

fun JsonElement?.safeGetAsString(): String? {
    return if (this != null && this.isJsonPrimitive) this.asString else null
}

// Functions that are used on the parent objects

fun JsonObject.getMemberAsString(fieldName: String): String? {
    return this.get(fieldName).safeGetAsString()
}

fun JsonObject.mustGetMemberAsString(fieldName: String): String {
    return this.get(fieldName).run { if (this != null && this.isJsonPrimitive) this.asString else error("JSON: Could not get $fieldName") }
}

fun JsonObject.getMemberAsInt(fieldName: String): Int? {
    return this.get(fieldName).run { if (this != null && this.isJsonPrimitive) this.asInt else null }
}

fun JsonObject.mustGetMemberAsInt(fieldName: String): Int {
    return this.get(fieldName).run { if (this != null && this.isJsonPrimitive) this.asInt else error("JSON: Could not get $fieldName") }
}

fun JsonObject.getMemberAsLong(fieldName: String): Long? {
    return this.get(fieldName).run { if (this != null && this.isJsonPrimitive) this.asLong else null }
}

fun JsonObject.mustGetMemberAsLong(fieldName: String): Long {
    return this.get(fieldName).run { if (this != null && this.isJsonPrimitive) this.asLong else error("JSON: Could not get $fieldName") }
}

fun JsonObject.getMemberAsBoolean(fieldName: String): Boolean? {
    return this.get(fieldName).run { if (this != null && this.isJsonPrimitive) this.asBoolean else null }
}

fun JsonObject.mustGetMemberAsBoolean(fieldName: String): Boolean {
    return this.get(fieldName).run { if (this != null && this.isJsonPrimitive) this.asBoolean else error("JSON: Could not get $fieldName") }
}

fun JsonObject.getMemberAsShort(fieldName: String): Short? {
    return this.get(fieldName).run { if (this != null && this.isJsonPrimitive) this.asShort else null }
}

fun JsonObject.mustGetMemberAsShort(fieldName: String): Short {
    return this.get(fieldName).run { if (this != null && this.isJsonPrimitive) this.asShort else error("JSON: Could not get $fieldName") }
}

fun JsonObject.getMemberAsByte(fieldName: String): Byte? {
    return this.get(fieldName).run { if (this != null && this.isJsonPrimitive) this.asByte else null }
}

fun JsonObject.mustGetMemberAsByte(fieldName: String): Byte {
    return this.get(fieldName).run { if (this != null && this.isJsonPrimitive) this.asByte else error("JSON: Could not get $fieldName") }
}

fun JsonObject.getMemberAsJsonArray(fieldName: String): JsonArray? {
    return this.get(fieldName).run { if (this != null && this.isJsonArray) this.asJsonArray else null }
}

fun JsonObject.mustGetMemberAsJsonArray(fieldName: String): JsonArray {
    return this.get(fieldName).run { if (this != null && this.isJsonArray) this.asJsonArray else error("JSON: Could not get $fieldName") }
}

fun JsonObject.getMemberAsJsonObject(fieldName: String): JsonObject? {
    return this.get(fieldName).run { if (this != null && this.isJsonObject) this.asJsonObject else null }
}

fun JsonObject.mustGetMemberAsJsonObject(fieldName: String): JsonObject {
    return this.get(fieldName).mustGetAsJsonObject(fieldName)
}

// Functions to be used directly on parent arrays

fun JsonArray.getElementAsString(index: Int): String? {
    return this.get(index).safeGetAsString()
}

fun JsonArray.mustGetElementAsString(index: Int): String {
    return this.get(index).run { if (this != null && this.isJsonPrimitive) this.asString else error("JSON: Could not get index $index") }
}

fun JsonArray.mustGetElementAsLong(index: Int): Long {
    return this.get(index).run { if (this != null && this.isJsonPrimitive) this.asLong else error("JSON: Could not get index $index") }
}

fun JsonArray.mustGetElementAsJsonObject(index: Int): JsonObject {
    return this.get(index).run { if (this != null && this.isJsonObject) this.asJsonObject else error("JSON: Could not get index $index") }
}

/**
 * Removes items from the end of the array until the size == maxLength.
 * Does nothing if size <= maxLength to begin with.
 * After this, size must be <= maxLength.
 * @param maxLength The maximum length this array could have after the truncation.
 */
fun JsonArray.truncate(maxLength: Int) {
    while (this.size() > maxLength) {
        this.remove(this.size() - 1)
    }
}
