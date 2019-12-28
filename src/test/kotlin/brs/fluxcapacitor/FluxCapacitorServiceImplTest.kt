package brs.fluxcapacitor

import brs.common.QuickMocker
import brs.objects.FluxValues
import brs.objects.Props
import brs.services.BlockchainService
import brs.services.PropertyService
import brs.services.impl.FluxCapacitorServiceImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class FluxCapacitorServiceImplTest {

    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var propertyServiceMock: PropertyService

    private lateinit var t: FluxCapacitorServiceImpl

    @BeforeEach
    fun setUp() {
        blockchainServiceMock = mockk(relaxed = true)
        propertyServiceMock = QuickMocker.defaultPropertyService()
    }

    @DisplayName("Feature is active on ProdNet")
    @Test
    fun featureIsActiveOnProdNet() {
        every { propertyServiceMock.get(eq(Props.DEV_TESTNET)) } returns false
        every { blockchainServiceMock.height } returns 500000

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
        every { propertyServiceMock.get(eq(Props.DEV_TESTNET)) } returns false
        every { blockchainServiceMock.height } returns 499999

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
        every { propertyServiceMock.get(eq(Props.DEV_TESTNET)) } returns true
        every { blockchainServiceMock.height } returns 88999

        t = FluxCapacitorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainServiceMock,
                propertyServiceMock
            )
        )

        assertTrue(t.getValue(FluxValues.POC2))

        every { blockchainServiceMock.height } returns 30000

        assertFalse(t.getValue(FluxValues.POC2))
    }

    @DisplayName("FluxInt gives its default value when no historical moments changed it yet")
    @Test
    fun fluxIntDefaultValue() {
        every { propertyServiceMock.get(eq(Props.DEV_TESTNET)) } returns false
        every { blockchainServiceMock.height } returns 88000

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
        every { propertyServiceMock.get(eq(Props.DEV_TESTNET)) } returns false
        every { blockchainServiceMock.height } returns 500000

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
        every { propertyServiceMock.get(eq(Props.DEV_TESTNET)) } returns true

        t = FluxCapacitorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainServiceMock,
                propertyServiceMock
            )
        )

        every { blockchainServiceMock.height } returns 5

        assertEquals(255, t.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS))
    }

    @DisplayName("FluxInt on TestNet gives a new value when a historical moment has passed")
    @Test
    fun fluxIntTestNetHistoricalValue() {
        every { propertyServiceMock.get(eq(Props.DEV_TESTNET)) } returns true

        t = FluxCapacitorServiceImpl(
            QuickMocker.dependencyProvider(
                blockchainServiceMock,
                propertyServiceMock
            )
        )

        every { blockchainServiceMock.height } returns 88000

        assertEquals(1020, t.getValue(FluxValues.MAX_NUMBER_TRANSACTIONS))
    }

    @DisplayName("FluxInt on TestNet gives a different value because the historical moment configuration is different")
    @Test
    fun fluxIntTestNetHistoricalMomentChangedThroughProperty() {
        every { propertyServiceMock.get(eq(Props.DEV_TESTNET)) } returns true
        every { propertyServiceMock.get(eq(Props.DEV_PRE_DYMAXION_BLOCK_HEIGHT)) } returns 12345

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
