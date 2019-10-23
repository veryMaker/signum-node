package brs.db.store

import brs.at.AT
import brs.db.BurstKey
import brs.db.VersionedEntityTable

interface ATStore {
    suspend fun getOrderedATs(): List<Long>

    suspend fun getAllATIds(): Collection<Long>

    val atDbKeyFactory: BurstKey.LongKeyFactory<AT>

    val atTable: VersionedEntityTable<AT>

    val atStateDbKeyFactory: BurstKey.LongKeyFactory<AT.ATState>

    val atStateTable: VersionedEntityTable<AT.ATState>

    suspend fun isATAccountId(id: Long?): Boolean

    suspend fun getAT(id: Long?): AT?

    suspend fun getATsIssuedBy(accountId: Long?): List<Long>

    suspend fun findTransaction(startHeight: Int, endHeight: Int, atID: Long?, numOfTx: Int, minAmount: Long): Long?

    suspend fun findTransactionHeight(transactionId: Long?, height: Int, atID: Long?, minAmount: Long): Int
}
