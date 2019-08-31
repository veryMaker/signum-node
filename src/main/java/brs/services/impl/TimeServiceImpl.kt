package brs.services.impl

import brs.services.TimeService
import brs.util.Time
import brs.util.Time.FasterTime

import java.util.concurrent.atomic.AtomicReference

class TimeServiceImpl : TimeService {

    override val epochTime: Int
        get() = time.get().time

    override val epochTimeMillis: Long
        get() = time.get().timeInMillis

    override fun setTime(t: FasterTime) {
        time.set(t)
    }

    companion object {

        private val time = AtomicReference<Time>(Time.EpochTime())
    }

}
