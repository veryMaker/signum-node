package brs.at

data class AtVersion(
    val minFee: Long,
    val stepFee: Long,
    val maxSteps: Long,
    val apiStepMultiplier: Long,
    val costPerPage: Long,
    val maxWaitForNumOfBlocks: Long,
    val maxSleepBetweenBlocks: Long, // TODO this is unused??
    val pageSize: Long,
    val maxMachineCodePages: Long,
    val maxMachineDataPages: Long,
    val maxMachineUserStackPages: Long,
    val maxMachineCallStackPages: Long,
    val blocksForRandom: Long,
    val maxPayloadForBlock: Long, // TODO this is unused??
    val averageBlockMinutes: Long
)