package brs.fluxcapacitor

open class FluxValue<T> @SafeVarargs
constructor(val defaultValue: T, vararg val valueChanges: ValueChange<T>) {
    class ValueChange<T>(val historicalMoment: HistoricalMoments, val newValue: T)
}
