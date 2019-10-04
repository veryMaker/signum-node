package brs.util

import brs.Constants

// TODO burstkit4j integration
interface Time {
    val time: Int

    val timeInMillis: Long

    class EpochTime : Time {

        override val time: Int
            get() = ((System.currentTimeMillis() - Constants.EPOCH_BEGINNING + 500) / 1000).toInt()

        override val timeInMillis: Long
            get() = System.currentTimeMillis() - Constants.EPOCH_BEGINNING + 500
    }

    class FasterTime(private val timeSeconds: Int, private val multiplier: Int) : Time {
        private val systemStartTime: Long

        override val timeInMillis: Long
            get() = timeSeconds + (System.currentTimeMillis() - systemStartTime) / multiplier

        init {
            require(!(multiplier > 1000 || multiplier <= 0)) { "Time multiplier must be between 1 and 1000" }
            this.systemStartTime = System.currentTimeMillis()
        }

        override val time: Int
            get() = timeSeconds + ((System.currentTimeMillis() - systemStartTime) / (1000 / multiplier)).toInt()
    }
}
