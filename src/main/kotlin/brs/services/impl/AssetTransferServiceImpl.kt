package brs.services.impl

import brs.entity.AssetTransfer
import brs.entity.AssetTransfer.Event
import brs.db.AssetTransferStore
import brs.services.AssetTransferService
import brs.entity.Transaction
import brs.transaction.appendix.Attachment
import brs.util.Listeners

internal class AssetTransferServiceImpl(private val assetTransferStore: AssetTransferStore) :
    AssetTransferService {

    override val listeners = Listeners<AssetTransfer, Event>()
    override val assetTransferTable = assetTransferStore.assetTransferTable
    override val transferDbKeyFactory = assetTransferStore.transferDbKeyFactory

    override val assetTransferCount get() = assetTransferTable.count

    override fun addListener(eventType: Event, listener: (AssetTransfer) -> Unit) {
        listeners.addListener(eventType, listener)
    }

    override fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer> {
        return assetTransferStore.getAssetTransfers(assetId, from, to)
    }

    override fun getAccountAssetTransfers(
        accountId: Long,
        assetId: Long,
        from: Int,
        to: Int
    ): Collection<AssetTransfer> {
        return assetTransferStore.getAccountAssetTransfers(accountId, assetId, from, to)
    }

    override fun getTransferCount(assetId: Long): Int {
        return assetTransferStore.getTransferCount(assetId)
    }

    override fun addAssetTransfer(
        transaction: Transaction,
        attachment: Attachment.ColoredCoinsAssetTransfer
    ): AssetTransfer {
        val dbKey = transferDbKeyFactory.newKey(transaction.id)
        val assetTransfer = AssetTransfer(dbKey, transaction, attachment)
        assetTransferTable.insert(assetTransfer)
        listeners.accept(Event.ASSET_TRANSFER, assetTransfer)
        return assetTransfer
    }
}
