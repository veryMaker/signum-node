package brs.at

import brs.Burst
import brs.Constants
import brs.DependencyProvider
import brs.fluxcapacitor.FluxValues

import java.util.HashMap


object AtConstants {
    lateinit var dp: DependencyProvider

    //platform based
    const val AT_ID_SIZE = 8
    private val MIN_FEE = mutableMapOf<Short, Long>()
    private val STEP_FEE = mutableMapOf<Short, Long>()
    private val MAX_STEPS = mutableMapOf<Short, Long>()
    private val API_STEP_MULTIPLIER = mutableMapOf<Short, Long>()
    private val COST_PER_PAGE = mutableMapOf<Short, Long>()
    private val MAX_WAIT_FOR_NUM_OF_BLOCKS = mutableMapOf<Short, Long>()
    private val MAX_SLEEP_BETWEEN_BLOCKS = mutableMapOf<Short, Long>()
    private val PAGE_SIZE = mutableMapOf<Short, Long>()
    private val MAX_MACHINE_CODE_PAGES = mutableMapOf<Short, Long>()
    private val MAX_MACHINE_DATA_PAGES = mutableMapOf<Short, Long>()
    private val MAX_MACHINE_USER_STACK_PAGES = mutableMapOf<Short, Long>()
    private val MAX_MACHINE_CALL_STACK_PAGES = mutableMapOf<Short, Long>()
    private val BLOCKS_FOR_RANDOM = mutableMapOf<Short, Long>()
    private val MAX_PAYLOAD_FOR_BLOCK = mutableMapOf<Short, Long>()
    private val AVERAGE_BLOCK_MINUTES = mutableMapOf<Short, Long>()

    fun init(dp: DependencyProvider) {
        this.dp = dp
    }

    init {
        // constants for AT version 1
        MIN_FEE[1.toShort()] = 1000L
        STEP_FEE[1.toShort()] = Constants.ONE_BURST / 10L
        MAX_STEPS[1.toShort()] = 2000L
        API_STEP_MULTIPLIER[1.toShort()] = 10L

        COST_PER_PAGE[1.toShort()] = Constants.ONE_BURST

        MAX_WAIT_FOR_NUM_OF_BLOCKS[1.toShort()] = 31536000L
        MAX_SLEEP_BETWEEN_BLOCKS[1.toShort()] = 31536000L

        PAGE_SIZE[1.toShort()] = 256L

        MAX_MACHINE_CODE_PAGES[1.toShort()] = 10L
        MAX_MACHINE_DATA_PAGES[1.toShort()] = 10L
        MAX_MACHINE_USER_STACK_PAGES[1.toShort()] = 10L
        MAX_MACHINE_CALL_STACK_PAGES[1.toShort()] = 10L

        BLOCKS_FOR_RANDOM[1.toShort()] = 15L //for testing 2 -> normally 1440
        MAX_PAYLOAD_FOR_BLOCK[1.toShort()] = dp.fluxCapacitor.getValue(FluxValues.MAX_PAYLOAD_LENGTH) / 2L //use at max half size of the block.
        AVERAGE_BLOCK_MINUTES[1.toShort()] = 4L
        // end of AT version 1

        // constants for AT version 2
        MIN_FEE[2.toShort()] = 1000L
        STEP_FEE[2.toShort()] = Constants.FEE_QUANT / 10L
        MAX_STEPS[2.toShort()] = 100000L
        API_STEP_MULTIPLIER[2.toShort()] = 10L

        COST_PER_PAGE[2.toShort()] = Constants.FEE_QUANT * 10

        MAX_WAIT_FOR_NUM_OF_BLOCKS[2.toShort()] = 31536000L
        MAX_SLEEP_BETWEEN_BLOCKS[2.toShort()] = 31536000L

        PAGE_SIZE[2.toShort()] = 256L

        MAX_MACHINE_CODE_PAGES[2.toShort()] = 10L
        MAX_MACHINE_DATA_PAGES[2.toShort()] = 10L
        MAX_MACHINE_USER_STACK_PAGES[2.toShort()] = 10L
        MAX_MACHINE_CALL_STACK_PAGES[2.toShort()] = 10L

        BLOCKS_FOR_RANDOM[2.toShort()] = 15L //for testing 2 -> normally 1440
        MAX_PAYLOAD_FOR_BLOCK[2.toShort()] = dp.fluxCapacitor.getValue(FluxValues.MAX_PAYLOAD_LENGTH) / 2L //use at max half size of the block.
        AVERAGE_BLOCK_MINUTES[2.toShort()] = 4L
        // end of AT version 2
    }

    fun atVersion(blockHeight: Int): Short {
        return dp.fluxCapacitor.getValue(FluxValues.AT_VERSION, blockHeight)
    }

    fun stepFee(height: Int): Long {
        return STEP_FEE[atVersion(height)]!!
    }

    fun maxSteps(height: Int): Long {
        return MAX_STEPS[atVersion(height)]!!
    }

    fun apiStepMultiplier(height: Int): Long {
        return API_STEP_MULTIPLIER[atVersion(height)]!!
    }

    fun costPerPage(height: Int): Long {
        return COST_PER_PAGE[atVersion(height)]!!
    }

    fun getMaxWaitForNumOfBlocks(height: Int): Long {
        return MAX_WAIT_FOR_NUM_OF_BLOCKS[atVersion(height)]!!
    }

    fun maxSleepBetweenBlocks(height: Int): Long {
        return MAX_SLEEP_BETWEEN_BLOCKS[atVersion(height)]!!
    }

    fun pageSize(height: Int): Long {
        return PAGE_SIZE[atVersion(height)]!!
    }

    fun maxMachineCodePages(height: Int): Long {
        return MAX_MACHINE_CODE_PAGES[atVersion(height)]!!
    }

    fun maxMachineDataPages(height: Int): Long {
        return MAX_MACHINE_DATA_PAGES[atVersion(height)]!!
    }

    fun maxMachineUserStackPages(height: Int): Long {
        return MAX_MACHINE_USER_STACK_PAGES[atVersion(height)]!!
    }

    fun maxMachineCallStackPages(height: Int): Long {
        return MAX_MACHINE_CALL_STACK_PAGES[atVersion(height)]!!
    }

    fun blocksForRandom(height: Int): Long {
        return BLOCKS_FOR_RANDOM[atVersion(height)]!!
    }

    fun maxPayloadForBlock(height: Int): Long {
        return MAX_PAYLOAD_FOR_BLOCK[atVersion(height)]!!
    }

    fun averageBlockMinutes(height: Int): Long {
        return AVERAGE_BLOCK_MINUTES[atVersion(height)]!!
    }
}
