package brs

import brs.db.BurstKey
import brs.util.delegates.Atomic

open class Subscription(val senderId: Long,
                        val recipientId: Long,
                        val id: Long,
                        val amountPlanck: Long,
                        val frequency: Int,
                        timeNext: Int,
                        val dbKey: BurstKey) {
    var timeNext by Atomic(timeNext)
        private set

    // TODO rename method
    fun timeNextGetAndAdd(frequency: Int) {
        timeNext += frequency
    }
}
