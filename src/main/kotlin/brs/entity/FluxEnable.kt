package brs.entity

import brs.objects.HistoricalMoments

/**
 * A special type of FluxValue used for eg. forks that goes from disabled to enabled at a certain historical moment.
 */
data class FluxEnable(val enablePoint: HistoricalMoments) : FluxValue<Boolean>(
    false,
    ValueChange(enablePoint, true)
)
