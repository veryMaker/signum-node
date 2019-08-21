package brs.fluxcapacitor

import brs.Blockchain
import brs.common.QuickMocker
import brs.props.Prop
import brs.props.PropertyService
import brs.props.Props
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.junit.Assert.*

class FluxCapacitorImplTest {

    private var blockchainMock: Blockchain? = null
    private var propertyServiceMock: PropertyService? = null

    private var t: FluxCapacitorImpl? = null

    @BeforeEach
    fun setUp() {
        blockchainMock = mock()
        propertyServiceMock = QuickMocker.defaultPropertyService()
    }

    @DisplayName("Feature is active on ProdNet")
    @Test
    fun featureIsActiveOnProdNet() {
        whenever(propertyServiceMock!!.get<Boolean>(eq<Prop<Boolean>>(Props.DEV_TESTNET))).doReturn(false)
        whenever(blockchainMock!!.height).doReturn(500000)

        t = FluxCapacitorImpl(blockchainMock, propertyServiceMock)

        assertTrue(t!!.getValue(FluxValues.PRE_DYMAXION))
    }

    @DisplayName("Feature is not active on ProdNet")
    @Test
    fun featureIsInactiveProdNet() {
        whenever(propertyServiceMock!!.get<Boolean>(eq<Prop<Boolean>>(Props.DEV_TESTNET))).doReturn(false)
        whenever(blockchainMock!!.height).doReturn(499999)

        t = FluxCapacitorImpl(blockchainMock, propertyServiceMock)

        assertFalse(t!!.getValue(FluxValues.POC2))
    }

    @DisplayName("Feature is active on TestNet")
    @Test
    fun featureIsActiveTestNet() {
        whenever(propertyServiceMock!!.get<Boolean>(eq<Prop<Boolean>>(Props.DEV_TESTNET))).doReturn(true)
        whenever(blockchainMock!!.height).doReturn(88999)

        t = FluxCapacitorImpl(blockchainMock, propertyServiceMock)

        assertTrue(t!!.getValue(FluxValues.POC2))

        whenever(blockchainMock!!.height).doReturn(30000)

        assertFalse(t!!.getValue(FluxValues.POC2))
    }

    @DisplayName("FluxInt gives its default value when no historical moments changed it yet")
    @Test
    fun fluxIntDefaultValue() {
        whenever(propertyServiceMock!!.get<Boolean>(eq<Prop<Boolean>>(Props.DEV_TESTNET))).doReturn(false)
        whenever(blockchainMock!!.height).doReturn(88000)

        t = FluxCapacitorImpl(blockchainMock, propertyServiceMock)

        assertEquals(255, t!!.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS))
    }

    @DisplayName("FluxInt gives a new value when a historical moment has passed")
    @Test
    fun fluxIntHistoricalValue() {
        whenever(propertyServiceMock!!.get<Boolean>(eq<Prop<Boolean>>(Props.DEV_TESTNET))).doReturn(false)
        whenever(blockchainMock!!.height).doReturn(500000)

        t = FluxCapacitorImpl(blockchainMock, propertyServiceMock)

        assertEquals(1020, t!!.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS))
    }

    @DisplayName("FluxInt on TestNet gives its default value when no historical moments changed it yet")
    @Test
    fun fluxIntTestNetDefaultValue() {
        whenever(propertyServiceMock!!.get<Boolean>(eq<Prop<Boolean>>(Props.DEV_TESTNET))).doReturn(true)

        t = FluxCapacitorImpl(blockchainMock, propertyServiceMock)

        whenever(blockchainMock!!.height).doReturn(5)

        assertEquals(255, t!!.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS))
    }

    @DisplayName("FluxInt on TestNet gives a new value when a historical moment has passed")
    @Test
    fun fluxIntTestNetHistoricalValue() {
        whenever(propertyServiceMock!!.get(eq<Prop<Boolean>>(Props.DEV_TESTNET))).doReturn(true)

        t = FluxCapacitorImpl(blockchainMock, propertyServiceMock)

        whenever(blockchainMock!!.height).doReturn(88000)

        assertEquals(1020, t!!.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS))
    }

    @DisplayName("FluxInt on TestNet gives a different value because the historical moment configuration is different")
    @Test
    fun fluxIntTestNetHistoricalMomentChangedThroughProperty() {
        whenever(propertyServiceMock!!.get<Boolean>(eq<Prop<Boolean>>(Props.DEV_TESTNET))).doReturn(true)
        whenever(propertyServiceMock!!.get<Int>(eq<Prop<Int>>(Props.DEV_PRE_DYMAXION_BLOCK_HEIGHT))).doReturn(12345)

        t = FluxCapacitorImpl(blockchainMock, propertyServiceMock)

        assertEquals(255, t!!.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS, 12344))
        assertEquals(1020, t!!.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS, 12345))
    }
}
