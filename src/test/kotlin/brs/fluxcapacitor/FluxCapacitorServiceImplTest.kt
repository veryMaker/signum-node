package brs.fluxcapacitor

import brs.objects.FluxValues
import brs.services.BlockchainService
import brs.common.QuickMocker
import brs.services.PropertyService
import brs.objects.Props
import brs.services.impl.FluxCapacitorServiceImpl
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

import org.junit.Assert.*

class FluxCapacitorServiceImplTest {

    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var propertyServiceMock: PropertyService

    private lateinit var t: FluxCapacitorServiceImpl

    @BeforeEach
    fun setUp() {
        blockchainServiceMock = mock()
        propertyServiceMock = QuickMocker.defaultPropertyService()
    }

    @DisplayName("Feature is active on ProdNet")
    @Test
    fun featureIsActiveOnProdNet() {
        whenever(propertyServiceMock.get(eq(Props.DEV_TESTNET))).doReturn(false)
        whenever(blockchainServiceMock.height).doReturn(500000)

        t = FluxCapacitorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainServiceMock,
                propertyServiceMock
            )
        )

        assertTrue(t.getValue(FluxValues.PRE_DYMAXION))
    }

    @DisplayName("Feature is not active on ProdNet")
    @Test
    fun featureIsInactiveProdNet() {
        whenever(propertyServiceMock.get(eq(Props.DEV_TESTNET))).doReturn(false)
        whenever(blockchainServiceMock.height).doReturn(499999)

        t = FluxCapacitorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainServiceMock,
                propertyServiceMock
            )
        )

        assertFalse(t.getValue(FluxValues.POC2))
    }

    @DisplayName("Feature is active on TestNet")
    @Test
    fun featureIsActiveTestNet() {
        whenever(propertyServiceMock.get(eq(Props.DEV_TESTNET))).doReturn(true)
        whenever(blockchainServiceMock.height).doReturn(88999)

        t = FluxCapacitorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainServiceMock,
                propertyServiceMock
            )
        )

        assertTrue(t.getValue(FluxValues.POC2))

        whenever(blockchainServiceMock.height).doReturn(30000)

        assertFalse(t.getValue(FluxValues.POC2))
    }

    @DisplayName("FluxInt gives its default value when no historical moments changed it yet")
    @Test
    fun fluxIntDefaultValue() {
        whenever(propertyServiceMock.get(eq(Props.DEV_TESTNET))).doReturn(false)
        whenever(blockchainServiceMock.height).doReturn(88000)

        t = FluxCapacitorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainServiceMock,
                propertyServiceMock
            )
        )

        assertEquals(255, t.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS))
    }

    @DisplayName("FluxInt gives a new value when a historical moment has passed")
    @Test
    fun fluxIntHistoricalValue() {
        whenever(propertyServiceMock.get(eq(Props.DEV_TESTNET))).doReturn(false)
        whenever(blockchainServiceMock.height).doReturn(500000)

        t = FluxCapacitorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainServiceMock,
                propertyServiceMock
            )
        )

        assertEquals(1020, t.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS))
    }

    @DisplayName("FluxInt on TestNet gives its default value when no historical moments changed it yet")
    @Test
    fun fluxIntTestNetDefaultValue() {
        whenever(propertyServiceMock.get(eq(Props.DEV_TESTNET))).doReturn(true)

        t = FluxCapacitorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainServiceMock,
                propertyServiceMock
            )
        )

        whenever(blockchainServiceMock.height).doReturn(5)

        assertEquals(255, t.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS))
    }

    @DisplayName("FluxInt on TestNet gives a new value when a historical moment has passed")
    @Test
    fun fluxIntTestNetHistoricalValue() {
        whenever(propertyServiceMock.get(eq(Props.DEV_TESTNET))).doReturn(true)

        t = FluxCapacitorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainServiceMock,
                propertyServiceMock
            )
        )

        whenever(blockchainServiceMock.height).doReturn(88000)

        assertEquals(1020, t.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS))
    }

    @DisplayName("FluxInt on TestNet gives a different value because the historical moment configuration is different")
    @Test
    fun fluxIntTestNetHistoricalMomentChangedThroughProperty() {
        whenever(propertyServiceMock.get(eq(Props.DEV_TESTNET))).doReturn(true)
        whenever(propertyServiceMock.get(eq(Props.DEV_PRE_DYMAXION_BLOCK_HEIGHT))).doReturn(12345)

        t = FluxCapacitorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainServiceMock,
                propertyServiceMock
            )
        )

        assertEquals(255, t.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS, 12344))
        assertEquals(1020, t.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS, 12345))
    }
}
