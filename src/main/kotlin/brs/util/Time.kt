package brs.util

import brs.objects.Constants

// TODO burstkit4j integration
interface Time {
    /**
     * Gets the current Burst Time in seconds
     */
    val timeInSeconds: Int

    object EpochTime : Time {
        override val timeInSeconds: Int
            get() = ((System.currentTimeMillis() - Constants.EPOCH_BEGINNING + 500) / 1000).toInt()
    }

    class FasterTime(private val timeSeconds: Int, private val multiplier: Int) : Time {
        private val systemStartTime: Long

        init {
            require(!(multiplier > 1000 || multiplier <= 0)) { "Time multiplier must be between 1 and 1000" }
            this.systemStartTime = System.currentTimeMillis()
        }

        override val timeInSeconds: Int
            get() = timeSeconds + ((System.currentTimeMillis() - systemStartTime) / (1000 / multiplier)).toInt()
    }
}
