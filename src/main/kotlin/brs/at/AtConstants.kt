package brs.at

import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.objects.FluxValues

class AtConstants(private val dp: DependencyProvider) {
    private val versions: Map<Short, AtVersion> = mapOf(
        1.toShort() to AtVersion(
            minFee = 1000L,
            stepFee = Constants.ONE_BURST / 10L,
            maxSteps = 2000L,
            apiStepMultiplier = 10L,
            costPerPage = Constants.ONE_BURST,
            maxWaitForNumOfBlocks = 31536000L,
            maxSleepBetweenBlocks = 31536000L,
            pageSize = 256L,
            maxMachineCodePages = 10L,
            maxMachineDataPages = 10L,
            maxMachineUserStackPages = 10L,
            maxMachineCallStackPages = 10L,
            blocksForRandom = 15L,
            maxPayloadForBlock = dp.fluxCapacitorService.getValue(FluxValues.MAX_PAYLOAD_LENGTH) / 2L, // Use at max half size of the block.
            averageBlockMinutes = 4L
            ),
        2.toShort() to AtVersion(
            minFee = 1000L,
            stepFee = Constants.FEE_QUANT / 10L,
            maxSteps = 100000L,
            apiStepMultiplier = 10L,
            costPerPage = Constants.FEE_QUANT * 10,
            maxWaitForNumOfBlocks = 31536000L,
            maxSleepBetweenBlocks = 31536000L,
            pageSize = 256L,
            maxMachineCodePages = 20L,
            maxMachineDataPages = 10L,
            maxMachineUserStackPages = 10L,
            maxMachineCallStackPages = 10L,
            blocksForRandom = 15L,
            maxPayloadForBlock = dp.fluxCapacitorService.getValue(FluxValues.MAX_PAYLOAD_LENGTH) / 2L, // Use at max half size of the block.
            averageBlockMinutes = 4L
        )
    )

    fun atVersion(blockHeight: Int): Short {
        return dp.fluxCapacitorService.getValue(FluxValues.AT_VERSION, blockHeight)
    }

    operator fun get(height: Int): AtVersion {
        return versions[atVersion(height)] ?: error("At height $height AT version is ${atVersion(height)} but no constants exist for this version.")
    }
}
