package brs.assetexchange

import brs.AssetTransfer
import brs.AssetTransfer.Event
import brs.Attachment
import brs.Transaction
import brs.db.store.AssetTransferStore
import brs.util.Listeners

internal class AssetTransferServiceImpl(private val assetTransferStore: AssetTransferStore) { // TODO interface

    private val listeners = Listeners<AssetTransfer, Event>()
    private val assetTransferTable = assetTransferStore.assetTransferTable
    private val transferDbKeyFactory = assetTransferStore.transferDbKeyFactory

    suspend fun getAssetTransferCount() = assetTransferTable.getCount()

    suspend fun addListener(eventType: Event, listener: suspend (AssetTransfer) -> Unit) {
        listeners.addListener(eventType, listener)
    }

    suspend fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferStore.getAssetTransfers(assetId, from, to)
    }

    suspend fun getAccountAssetTransfers(accountId: Long, assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferStore.getAccountAssetTransfers(accountId, assetId, from, to)
    }

    suspend fun getTransferCount(assetId: Long): Int {
        return assetTransferStore.getTransferCount(assetId)
    }

    suspend fun addAssetTransfer(transaction: Transaction, attachment: Attachment.ColoredCoinsAssetTransfer): AssetTransfer {
        val dbKey = transferDbKeyFactory.newKey(transaction.id)
        val assetTransfer = AssetTransfer(dbKey, transaction, attachment)
        assetTransferTable.insert(assetTransfer)
        listeners.accept(Event.ASSET_TRANSFER, assetTransfer)
        return assetTransfer
    }
}
