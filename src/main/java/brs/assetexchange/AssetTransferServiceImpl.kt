package brs.assetexchange

import brs.AssetTransfer
import brs.AssetTransfer.Event
import brs.Attachment
import brs.Transaction
import brs.db.BurstKey
import brs.db.BurstKey.LongKeyFactory
import brs.db.sql.EntitySqlTable
import brs.db.store.AssetTransferStore
import brs.util.Listeners
import java.util.function.Consumer

internal class AssetTransferServiceImpl(private val assetTransferStore: AssetTransferStore) {

    private val listeners = Listeners<AssetTransfer, Event>()
    private val assetTransferTable: EntitySqlTable<AssetTransfer>
    private val transferDbKeyFactory: LongKeyFactory<AssetTransfer>

    val assetTransferCount: Int
        get() = assetTransferTable.count


    init {
        this.assetTransferTable = assetTransferStore.assetTransferTable
        this.transferDbKeyFactory = assetTransferStore.transferDbKeyFactory
    }

    fun addListener(listener: Consumer<AssetTransfer>, eventType: Event): Boolean {
        return listeners.addListener(listener, eventType)
    }

    fun removeListener(listener: Consumer<AssetTransfer>, eventType: Event): Boolean {
        return listeners.removeListener(listener, eventType)
    }

    fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferStore.getAssetTransfers(assetId, from, to)
    }

    fun getAccountAssetTransfers(accountId: Long, assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferStore.getAccountAssetTransfers(accountId, assetId, from, to)
    }

    fun getTransferCount(assetId: Long): Int {
        return assetTransferStore.getTransferCount(assetId)
    }

    fun addAssetTransfer(transaction: Transaction, attachment: Attachment.ColoredCoinsAssetTransfer): AssetTransfer {
        val dbKey = transferDbKeyFactory.newKey(transaction.id)
        val assetTransfer = AssetTransfer(dbKey, transaction, attachment)
        assetTransferTable.insert(assetTransfer)
        listeners.accept(assetTransfer, Event.ASSET_TRANSFER)
        return assetTransfer
    }

}
