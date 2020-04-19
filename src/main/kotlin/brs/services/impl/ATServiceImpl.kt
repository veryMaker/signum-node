package brs.services.impl

import brs.at.AT
import brs.entity.DependencyProvider
import brs.services.ATService

class ATServiceImpl(private val dp: DependencyProvider) : ATService {
    override fun getAllATIds() = dp.db.atStore.getAllATIds()

    override fun getATsIssuedBy(accountId: Long): List<Long> {
        return dp.db.atStore.getATsIssuedBy(accountId)
    }

    override fun getAT(id: Long): AT? {
        return dp.db.atStore.getAT(id)
    }
}
