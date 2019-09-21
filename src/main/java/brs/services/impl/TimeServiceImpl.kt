package brs.services.impl

import brs.services.TimeService
import brs.util.Time
import brs.util.Time.FasterTime
import brs.util.delegates.Atomic

class TimeServiceImpl : TimeService {
    override val epochTime: Int
        get() = time.time

    override val epochTimeMillis: Long
        get() = time.timeInMillis

    override fun setTime(fasterTime: FasterTime) {
        time = fasterTime
    }

    companion object {
        private var time by Atomic<Time>(Time.EpochTime())
    }
}
