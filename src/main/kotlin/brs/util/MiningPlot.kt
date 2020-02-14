package brs.util

object MiningPlot {
    private const val HASH_SIZE = 32
    private const val HASHES_PER_SCOOP = 2
    const val SCOOP_SIZE = HASHES_PER_SCOOP * HASH_SIZE
    private const val SCOOPS_PER_PLOT = 4096
    const val PLOT_SIZE = SCOOPS_PER_PLOT * SCOOP_SIZE
}
