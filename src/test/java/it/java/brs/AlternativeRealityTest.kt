package it.java.brs

import brs.util.mustGetAsJsonObject
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import it.common.AbstractIT
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.BufferedInputStream

class AlternativeRealityTest : AbstractIT() {
    @Before
    fun setUp() {
        setupIT()
    }

    @After
    fun tearDown() {
        tearDownIT()
    }

    @Test
    fun reality1() = runBlocking {
        getReality("reality1.json").forEach { jsonObject ->
            super.processBlock(jsonObject)
            delay(500)
        }
    }

    @Test
    fun reality2() = runBlocking {
        getReality("reality2.json").forEach { jsonObject ->
            super.processBlock(jsonObject)
            delay(500)
        }
    }

    private fun getReality(realityName: String): List<JsonObject> {
        val parser = JsonParser()

        val inputStream = BufferedInputStream(javaClass.getResourceAsStream("/alternatereality/$realityName"))
        val inputFileContent = String(inputStream.readBytes())
        inputStream.close()

        val array = parser.parse(inputFileContent) as JsonArray

        val result = mutableListOf<JsonObject>()

        array.forEach { obj ->
            result.add(obj.mustGetAsJsonObject("obj"))
        }

        return result
    }
}
