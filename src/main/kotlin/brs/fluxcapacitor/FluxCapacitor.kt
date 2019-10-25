package brs.fluxcapacitor

interface FluxCapacitor {
    fun <T> getValue(fluxValue: FluxValue<T>): T
    fun <T> getValue(fluxValue: FluxValue<T>, height: Int): T
    fun getStartingHeight(fluxEnable: FluxEnable): Int?
}
