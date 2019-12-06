package brs.db

import org.jooq.DSLContext
import java.sql.Connection

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
    fun <V> getCache(tableName: String): MutableMap<BurstKey, V>

    /**
     * TODO
     */
    fun <V> getBatch(tableName: String): MutableMap<BurstKey, V>

    /**
     * TODO
     */
    fun beginTransaction(): Connection

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

    val allTables: Collection<Table>
}
