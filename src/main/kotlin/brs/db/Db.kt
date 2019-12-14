package brs.db

import org.jooq.DSLContext
import org.jooq.Table as JooqTable

interface Db {
    /**
     * TODO
     */
    fun getDslContext(): DSLContext

    /**
     * TODO
     */
    fun isInTransaction(): Boolean

    /**
     * TODO
     */
    fun shutdown()

    /**
     * TODO
     */
    fun <V> getCache(table: JooqTable<*>): MutableMap<BurstKey, V>

    /**
     * TODO
     */
    fun <V> getBatch(table: JooqTable<*>): MutableMap<BurstKey, V>

    /**
     * TODO
     */
    fun beginTransaction()

    /**
     * TODO
     */
    fun commitTransaction()

    /**
     * TODO
     */
    fun rollbackTransaction()

    /**
     * TODO
     */
    fun endTransaction()

    /**
     * TODO
     */
    fun optimizeTable(tableName: String)

    /**
     * TODO
     */
    val blockDb: BlockDb

    /**
     * TODO
     */
    val peerDb: PeerDb

    /**
     * TODO
     */
    val transactionDb: TransactionDb

    /**
     * TODO
     */
    val accountStore: AccountStore

    /**
     * TODO
     */
    val aliasStore: AliasStore

    /**
     * TODO
     */
    val assetStore: AssetStore

    /**
     * TODO
     */
    val assetTransferStore: AssetTransferStore

    /**
     * TODO
     */
    val atStore: ATStore

    /**
     * TODO
     */
    val blockchainStore: BlockchainStore

    /**
     * TODO
     */
    val digitalGoodsStoreStore: DigitalGoodsStoreStore

    /**
     * TODO
     */
    val escrowStore: EscrowStore

    /**
     * TODO
     */
    val indirectIncomingStore: IndirectIncomingStore

    /**
     * TODO
     */
    val orderStore: OrderStore

    /**
     * TODO
     */
    val subscriptionStore: SubscriptionStore

    /**
     * TODO
     */
    val tradeStore: TradeStore

    /**
     * TODO
     */
    val allTables: Collection<Table>

    /**
     * TODO
     */
    fun optimizeDatabase()

    /**
     * TODO
     */
    fun deleteAll()
}
