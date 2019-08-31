package brs.services.impl

import brs.DependencyProvider
import brs.at.AT
import brs.db.store.ATStore
import brs.services.ATService

class ATServiceImpl(private val dp: DependencyProvider) : ATService {

    override val allATIds: Collection<Long>
        get() = dp.atStore.allATIds

    override fun getATsIssuedBy(accountId: Long?): List<Long> {
        return dp.atStore.getATsIssuedBy(accountId)
    }

    override fun getAT(id: Long?): AT {
        return dp.atStore.getAT(id)
    }

}
