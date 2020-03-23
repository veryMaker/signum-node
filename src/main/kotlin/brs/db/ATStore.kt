package brs.db

import brs.at.AT

interface ATStore {
    /**
     * TODO
     */
    fun getOrderedATs(): List<Long>

    /**
     * TODO
     */
    fun getAllATIds(): Collection<Long>

    /**
     * TODO
     */
    val atDbKeyFactory: BurstKey.LongKeyFactory<AT>

    /**
     * TODO
     */
    val atTable: EntityTable<AT>

    /**
     * TODO
     */
    val atStateDbKeyFactory: BurstKey.LongKeyFactory<AT.ATState>

    /**
     * TODO
     */
    val atStateTable: MutableEntityTable<AT.ATState>

    /**
     * TODO
     */
    fun isATAccountId(id: Long): Boolean

    /**
     * TODO
     */
    fun getAT(id: Long): AT?

    /**
     * TODO
     */
    fun getATsIssuedBy(accountId: Long): List<Long>

    /**
     * TODO
     */
    fun findTransaction(startHeight: Int, endHeight: Int, atID: Long, numOfTx: Int, minAmount: Long): Long?

    /**
     * TODO
     */
    fun findTransactionHeight(transactionId: Long, height: Int, atID: Long, minAmount: Long): Int
}
