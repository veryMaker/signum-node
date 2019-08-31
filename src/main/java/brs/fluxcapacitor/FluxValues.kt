package brs.fluxcapacitor

object FluxValues {

    val REWARD_RECIPIENT_ENABLE = FluxEnable(HistoricalMoments.REWARD_RECIPIENT_ENABLE)
    val DIGITAL_GOODS_STORE = FluxEnable(HistoricalMoments.DIGITAL_GOODS_STORE_BLOCK)
    val AUTOMATED_TRANSACTION_BLOCK = FluxEnable(HistoricalMoments.AUTOMATED_TRANSACTION_BLOCK)
    val AT_FIX_BLOCK_2 = FluxEnable(HistoricalMoments.AT_FIX_BLOCK_2)
    val AT_FIX_BLOCK_3 = FluxEnable(HistoricalMoments.AT_FIX_BLOCK_3)
    val AT_FIX_BLOCK_4 = FluxEnable(HistoricalMoments.AT_FIX_BLOCK_4)
    val PRE_DYMAXION = FluxEnable(HistoricalMoments.PRE_DYMAXION)
    val POC2 = FluxEnable(HistoricalMoments.POC2)
    val NEXT_FORK = FluxEnable(HistoricalMoments.NEXT_FORK)

    val AT_VERSION = FluxValue(1.toShort(), FluxValue.ValueChange(HistoricalMoments.NEXT_FORK, 2.toShort()))

    val MAX_NUMBER_TRANSACTIONS = FluxValue(255, FluxValue.ValueChange(HistoricalMoments.PRE_DYMAXION, 1020))
    val MAX_PAYLOAD_LENGTH = FluxValue(255 * 176, FluxValue.ValueChange(HistoricalMoments.PRE_DYMAXION, 1020 * 176))
}
