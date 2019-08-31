package brs.fluxcapacitor

open class FluxValue<T> @SafeVarargs
constructor(val defaultValue: T, vararg valueChanges: ValueChange<T>) {
    val valueChanges: Array<ValueChange<T>>

    init {
        this.valueChanges = valueChanges
    }

    class ValueChange<T>(val historicalMoment: HistoricalMoments, val newValue: T)
}
