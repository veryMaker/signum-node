package brs.db

import org.jooq.DSLContext
import java.sql.Connection

interface Db {
    fun getDslContext(): DSLContext
    fun isInTransaction(): Boolean
    fun shutdown()
    fun <V> getCache(tableName: String): MutableMap<BurstKey, V>
    fun <V> getBatch(tableName: String): MutableMap<BurstKey, V>
    fun beginTransaction(): Connection
    fun commitTransaction()
    fun rollbackTransaction()
    fun endTransaction()
    fun optimizeTable(tableName: String)

    fun getBlockDb(): BlockDb
    fun getPeerDb(): PeerDb
    fun getTransactionDb(): TransactionDb
    fun getAccountStore(): AccountStore
    fun getAliasStore(): AliasStore
    fun getAssetStore(): AssetStore
    fun getAssetTransferStore(): AssetTransferStore
    fun getATStore(): ATStore
    fun getBlockchainStore(): BlockchainStore
    fun getDigitalGoodsStoreStore(): DigitalGoodsStoreStore
    fun getEscrowStore(): EscrowStore
    fun getIndirectIncomingStore(): IndirectIncomingStore
    fun getOrderStore(): OrderStore
    fun getSubscriptionStore(): SubscriptionStore
    fun getTradeStore(): TradeStore
}
