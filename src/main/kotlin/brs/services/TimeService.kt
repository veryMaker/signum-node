package brs.services

import brs.util.Time

interface TimeService {
    /**
     * TODO
     */
    val epochTime: Int

    /**
     * TODO
     */
    fun setTime(time: Time)
}
