package it.common

import brs.Burst
import brs.common.TestInfrastructure
import brs.peer.ProcessBlock
import brs.props.Props
import com.google.gson.JsonObject
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
abstract class AbstractIT {

    private lateinit var processBlock: ProcessBlock

    protected var apiSender = APISender()
    private lateinit var burst: Burst

    @Before
    fun setUp() {
        burst = Burst(testProperties(), false)
        processBlock = ProcessBlock(burst.dp.blockchain, burst.dp.blockchainProcessor)
    }

    @After
    fun shutdown() {
        burst.shutdown(false)
    }

    private fun testProperties(): Properties {
        val props = Properties()

        props.setProperty(Props.DEV_TESTNET.name, "true")
        props.setProperty(Props.DEV_OFFLINE.name, "true")
        props.setProperty(Props.DEV_DB_URL.name, TestInfrastructure.IN_MEMORY_DB_URL)
        props.setProperty(Props.DB_MAX_ROLLBACK.name, "1440")
        props.setProperty(Props.DB_CONNECTIONS.name, "1")

        props.setProperty(Props.API_SERVER.name, "on")
        props.setProperty(Props.API_LISTEN.name, "127.0.0.1")
        props.setProperty(Props.DEV_API_PORT.name, "" + TestInfrastructure.TEST_API_PORT)
        props.setProperty(Props.API_ALLOWED.name, "*")
        props.setProperty(Props.API_UI_DIR.name, "html/ui")

        return props
    }

    fun processBlock(jsonFirstBlock: JsonObject) = runBlocking {
        processBlock!!.processRequest(jsonFirstBlock, mock())
    }

    fun rollback(height: Int) = runBlocking {
        burst.dp.blockchainProcessor.popOffTo(0)
    }
}
