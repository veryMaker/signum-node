package brs.services

import brs.util.Time.FasterTime

interface TimeService {
    val epochTime: Int

    val epochTimeMillis: Long

    fun setTime(fasterTime: FasterTime)
}
