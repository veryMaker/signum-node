package brs.services

import brs.util.Time.FasterTime

interface TimeService {
    /**
     * TODO
     */
    val epochTime: Int

    /**
     * TODO
     */
    val epochTimeMillis: Long

    /**
     * TODO
     */
    fun setTime(fasterTime: FasterTime)
}
