package brs.db.store

import brs.at.AT
import brs.db.BurstKey
import brs.db.VersionedEntityTable

interface ATStore {

    val orderedATs: List<Long>

    val allATIds: Collection<Long>

    val atDbKeyFactory: BurstKey.LongKeyFactory<AT>

    val atTable: VersionedEntityTable<AT>

    val atStateDbKeyFactory: BurstKey.LongKeyFactory<AT.ATState>

    val atStateTable: VersionedEntityTable<AT.ATState>

    fun isATAccountId(id: Long?): Boolean

    fun getAT(id: Long?): AT

    fun getATsIssuedBy(accountId: Long?): List<Long>

    fun findTransaction(startHeight: Int, endHeight: Int, atID: Long?, numOfTx: Int, minAmount: Long): Long?

    fun findTransactionHeight(transactionId: Long?, height: Int, atID: Long?, minAmount: Long): Int
}
