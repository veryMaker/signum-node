package brs.fluxcapacitor

/**
 * A special type of FluxValue used for eg. forks that goes from disabled to enabled at a certain historical moment.
 */
class FluxEnable(val enablePoint: HistoricalMoments) : FluxValue<Boolean>(false, ValueChange(enablePoint, true))
