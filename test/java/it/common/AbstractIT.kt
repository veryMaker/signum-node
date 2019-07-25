package it.common

import brs.Burst
import brs.common.TestInfrastructure
import brs.peer.Peers
import brs.peer.ProcessBlock
import brs.props.Props
import com.google.gson.JsonObject
import io.mockk.mockkStatic
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.util.Properties

@RunWith(JUnit4::class)
abstract class AbstractIT {

    private var processBlock: ProcessBlock? = null

    protected var apiSender = APISender()

    @Before
    fun setUp() {
        mockkStatic(Peers::class)
        Burst.init(testProperties())

        processBlock = ProcessBlock(Burst.getBlockchain(), Burst.getBlockchainProcessor())
    }

    @After
    fun shutdown() {
        Burst.shutdown(true)
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

    fun processBlock(jsonFirstBlock: JsonObject) {
        processBlock!!.processRequest(jsonFirstBlock, null)
    }

    fun rollback(height: Int) {
        Burst.getBlockchainProcessor().popOffTo(0)
    }
}
