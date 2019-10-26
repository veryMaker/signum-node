package brs.services

import brs.db.BurstKey
import brs.db.EntityTable
import brs.entity.AssetTransfer
import brs.entity.Transaction
import brs.transaction.appendix.Attachment
import brs.util.Listeners

interface AssetTransferService {
    /**
     * TODO
     */
    val listeners: Listeners<AssetTransfer, AssetTransfer.Event>

    /**
     * TODO
     */
    val assetTransferTable: EntityTable<AssetTransfer>

    /**
     * TODO
     */
    val transferDbKeyFactory: BurstKey.LongKeyFactory<AssetTransfer>

    /**
     * TODO
     */
    val assetTransferCount: Int

    /**
     * TODO
     */
    fun addListener(eventType: AssetTransfer.Event, listener: (AssetTransfer) -> Unit)

    /**
     * TODO
     */
    fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    /**
     * TODO
     */
    fun getAccountAssetTransfers(accountId: Long, assetId: Long, from: Int, to: Int): Collection<AssetTransfer>

    /**
     * TODO
     */
    fun getTransferCount(assetId: Long): Int

    /**
     * TODO
     */
    fun addAssetTransfer(transaction: Transaction, attachment: Attachment.ColoredCoinsAssetTransfer): AssetTransfer
}
