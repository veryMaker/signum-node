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
    fun getBlockDb(): BlockDb

    /**
     * TODO
     */
    fun getPeerDb(): PeerDb

    /**
     * TODO
     */
    fun getTransactionDb(): TransactionDb

    /**
     * TODO
     */
    fun getAccountStore(): AccountStore

    /**
     * TODO
     */
    fun getAliasStore(): AliasStore

    /**
     * TODO
     */
    fun getAssetStore(): AssetStore

    /**
     * TODO
     */
    fun getAssetTransferStore(): AssetTransferStore

    /**
     * TODO
     */
    fun getATStore(): ATStore

    /**
     * TODO
     */
    fun getBlockchainStore(): BlockchainStore

    /**
     * TODO
     */
    fun getDigitalGoodsStoreStore(): DigitalGoodsStoreStore

    /**
     * TODO
     */
    fun getEscrowStore(): EscrowStore

    /**
     * TODO
     */
    fun getIndirectIncomingStore(): IndirectIncomingStore

    /**
     * TODO
     */
    fun getOrderStore(): OrderStore

    /**
     * TODO
     */
    fun getSubscriptionStore(): SubscriptionStore

    /**
     * TODO
     */
    fun getTradeStore(): TradeStore
}
