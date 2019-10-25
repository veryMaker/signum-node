package brs.services

import brs.entity.AssetTransfer
import brs.db.BurstKey
import brs.db.sql.EntitySqlTable
import brs.entity.Transaction
import brs.transaction.appendix.Attachment
import brs.util.Listeners

interface AssetTransferService {
    val listeners: Listeners<AssetTransfer, AssetTransfer.Event>
    val assetTransferTable: EntitySqlTable<AssetTransfer>
    val transferDbKeyFactory: BurstKey.LongKeyFactory<AssetTransfer>
    val assetTransferCount: Int
    fun addListener(eventType: AssetTransfer.Event, listener: (AssetTransfer) -> Unit)
    fun getAssetTransfers(assetId: Long, from: Int, to: Int): Collection<AssetTransfer>
    fun getAccountAssetTransfers(accountId: Long, assetId: Long, from: Int, to: Int): Collection<AssetTransfer>
    fun getTransferCount(assetId: Long): Int
    fun addAssetTransfer(transaction: Transaction, attachment: Attachment.ColoredCoinsAssetTransfer): AssetTransfer
}
