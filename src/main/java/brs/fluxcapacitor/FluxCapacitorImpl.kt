package brs.fluxcapacitor

import brs.Blockchain
import brs.DependencyProvider
import brs.props.PropertyService
import brs.props.Props

import java.util.HashMap

class FluxCapacitorImpl(dp: DependencyProvider) : FluxCapacitor {

    private val propertyService: PropertyService
    private val blockchain: Blockchain

    // Map of Flux Value -> Change Height -> Index of ValueChange in FluxValue. Used as a cache.
    private val valueChangesPerFluxValue = HashMap<FluxValue<*>, Map<Int, Int>>()

    init {
        this.propertyService = dp.propertyService
        this.blockchain = dp.blockchain
    }

    override fun <T> getValue(fluxValue: FluxValue<T>): T {
        return getValueAt(fluxValue, blockchain.height)
    }

    override fun <T> getValue(fluxValue: FluxValue<T>, height: Int): T {
        return getValueAt(fluxValue, height)
    }

    private fun getHistoricalMomentHeight(historicalMoment: HistoricalMoments): Int {
        if (propertyService.get(Props.DEV_TESTNET)) {
            val overridingHeight = propertyService.get(historicalMoment.overridingProperty)
            return if (overridingHeight >= 0) overridingHeight else historicalMoment.testnetHeight
        } else {
            return historicalMoment.mainnetHeight
        }
    }

    private fun <T> computeValuesAtHeights(fluxValue: FluxValue<T>): Map<Int, Int> {
        return (valueChangesPerFluxValue as java.util.Map<FluxValue<*>, Map<Int, Int>>).computeIfAbsent(fluxValue) { fv ->
            val valueChangeIndexAtHeight = HashMap<Int, Int>()
            val valueChanges = fluxValue.valueChanges
            for (i in valueChanges.indices) {
                valueChangeIndexAtHeight[getHistoricalMomentHeight(valueChanges[i].historicalMoment)] = i
            }
            valueChangeIndexAtHeight
        }
    }

    private fun <T> getValueAt(fluxValue: FluxValue<T>, height: Int): T {
        var mostRecentValue = fluxValue.defaultValue
        var mostRecentChangeHeight = 0
        for ((entryHeight, value) in computeValuesAtHeights(fluxValue)) {
            if (entryHeight <= height && entryHeight >= mostRecentChangeHeight) {
                mostRecentValue = fluxValue.valueChanges[value].newValue
                mostRecentChangeHeight = entryHeight
            }
        }
        return mostRecentValue
    }

    override fun getStartingHeight(fluxEnable: FluxEnable): Int? {
        return getHistoricalMomentHeight(fluxEnable.enablePoint)
    }
}
