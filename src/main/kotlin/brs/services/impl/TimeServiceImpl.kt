package brs.services.impl

import brs.services.TimeService
import burst.kit.crypto.BurstCrypto

class TimeServiceImpl : TimeService {
    override val epochTime
        get() = BurstCrypto.getInstance().currentBurstTime()
}
