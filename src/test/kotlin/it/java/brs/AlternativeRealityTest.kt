package it.java.brs

import brs.util.json.mustGetAsJsonObject
import com.google.gson.JsonArray
import brs.util.jetty.get
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import it.common.AbstractIT
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.InputStreamReader

abstract class AlternativeRealityTest : AbstractIT() {
    abstract fun getDbUrl(): String

    @Before
    fun setUp() {
        setupIT(getDbUrl())
    }

    @After
    fun tearDown() {
        tearDownIT()
    }

    @Test
    fun reality1() {
        getReality("reality1.json").forEach { jsonObject ->
            super.processBlock(jsonObject)
            Thread.sleep(500)
        }
    }

    @Test
    fun reality2() {
        getReality("reality2.json").forEach { jsonObject ->
            super.processBlock(jsonObject)
            Thread.sleep(500)
        }
    }

    private fun getReality(realityName: String): List<JsonObject> {
        val array = InputStreamReader(javaClass.getResourceAsStream("/alternatereality/$realityName")).use { JsonParser.parseReader(it) as JsonArray }
        val result = mutableListOf<JsonObject>()
        array.forEach { obj -> result.add(obj.mustGetAsJsonObject("obj")) }
        return result
    }
}
