package brs.services.impl

import brs.services.TimeService
import brs.util.Time

class TimeServiceImpl : TimeService {
    private var time: Time = Time.EpochTime

    override val epochTime: Int
        get() = time.timeInSeconds

    override fun setTime(time: Time) {
        this.time = time
    }
}
