package brs

import brs.db.BurstKey

import java.util.concurrent.atomic.AtomicInteger

open class Subscription(val senderId: Long?,
                        val recipientId: Long?,
                        val id: Long?,
                        val amountNQT: Long?,
                        val frequency: Int,
                        timeNext: Int,
                        val dbKey: BurstKey
) {
    private val timeNext: AtomicInteger

    init {
        this.timeNext = AtomicInteger(timeNext)
    }

    fun getTimeNext(): Int {
        return timeNext.get()
    }

    fun timeNextGetAndAdd(frequency: Int) {
        timeNext.getAndAdd(frequency)
    }
}
