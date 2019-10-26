package brs.db

import brs.entity.IndirectIncoming

interface IndirectIncomingStore {
    /**
     * TODO
     */
    fun addIndirectIncomings(indirectIncomings: Collection<IndirectIncoming>)

    /**
     * TODO
     */
    fun getIndirectIncomings(accountId: Long, from: Int, to: Int): List<Long>
}
