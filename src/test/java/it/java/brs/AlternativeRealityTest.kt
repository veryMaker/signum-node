package it.java.brs

import brs.util.JSON
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import it.common.AbstractIT
import org.junit.Test
import java.io.BufferedInputStream

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.ArrayList

class AlternativeRealityTest : AbstractIT() {

    @Test
    @Throws(IOException::class, InterruptedException::class)
    fun normalReality() {
        for (jsonObject in getReality("reality1.json")) {
            super.processBlock(jsonObject)
            Thread.sleep(500)
        }
    }

    @Throws(IOException::class)
    fun getReality(realityName: String): List<JsonObject> {
        val parser = JsonParser()

        val inputStream = BufferedInputStream(javaClass.getResourceAsStream("/alternatereality/$realityName"))
        val inputFileContent = String(inputStream.readBytes())
        inputStream.close()

        val array = parser.parse(inputFileContent) as JsonArray

        val result = mutableListOf<JsonObject>()

        for (obj in array) {
            result.add(JSON.getAsJsonObject(obj))
        }

        return result
    }
}
