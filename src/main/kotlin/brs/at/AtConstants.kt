package brs.at

import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.objects.FluxValues

class AtConstants(private val dp: DependencyProvider) {
    private val versions: Map<Short, AtVersion> = mapOf(
        1.toShort() to AtVersion(
            1000L,
            Constants.ONE_BURST / 10L,
            2000L,
            10L,
            Constants.ONE_BURST,
            31536000L,
            31536000L,
            256L,
            10L,
            10L,
            10L,
            10L,
            15L,
            dp.fluxCapacitorService.getValue(FluxValues.MAX_PAYLOAD_LENGTH) / 2L, // Use at max half size of the block.
            4L
            ),
        2.toShort() to AtVersion(
            1000L,
            Constants.FEE_QUANT / 10L,
            100000L,
            10L,
            Constants.FEE_QUANT * 10,
            31536000L,
            31536000L,
            256L,
            20L,
            10L,
            10L,
            10L,
            15L,
            dp.fluxCapacitorService.getValue(FluxValues.MAX_PAYLOAD_LENGTH) / 2L, // Use at max half size of the block.
            4L
        )
    )

    // TODO remove helper functions, replace with "operator fun get(height: Int)"

    fun atVersion(blockHeight: Int): Short {
        return dp.fluxCapacitorService.getValue(FluxValues.AT_VERSION, blockHeight)
    }

    fun stepFee(height: Int): Long {
        return versions[atVersion(height)]!!.stepFee
    }

    fun maxSteps(height: Int): Long {
        return versions[atVersion(height)]!!.maxSteps
    }

    fun apiStepMultiplier(height: Int): Long {
        return versions[atVersion(height)]!!.apiStepMultiplier
    }

    fun costPerPage(height: Int): Long {
        return versions[atVersion(height)]!!.costPerPage
    }

    fun maxWaitForNumOfBlocks(height: Int): Long {
        return versions[atVersion(height)]!!.maxWaitForNumOfBlocks
    }

    fun pageSize(height: Int): Long {
        return versions[atVersion(height)]!!.pageSize
    }

    fun maxMachineCodePages(height: Int): Long {
        return versions[atVersion(height)]!!.maxMachineCodePages
    }

    fun maxMachineDataPages(height: Int): Long {
        return versions[atVersion(height)]!!.maxMachineDataPages
    }

    fun maxMachineUserStackPages(height: Int): Long {
        return versions[atVersion(height)]!!.maxMachineUserStackPages
    }

    fun maxMachineCallStackPages(height: Int): Long {
        return versions[atVersion(height)]!!.maxMachineCallStackPages
    }

    fun blocksForRandom(height: Int): Long {
        return versions[atVersion(height)]!!.blocksForRandom
    }

    fun averageBlockMinutes(height: Int): Long {
        return versions[atVersion(height)]!!.averageBlockMinutes
    }
}
