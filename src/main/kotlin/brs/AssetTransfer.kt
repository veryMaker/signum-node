package brs

import brs.db.BurstKey

open class AssetTransfer {
    val id: Long
    val dbKey: BurstKey
    val assetId: Long
    val height: Int
    val senderId: Long
    val recipientId: Long
    val quantityQNT: Long
    val timestamp: Int

    enum class Event {
        ASSET_TRANSFER
    }

    constructor(dbKey: BurstKey, transaction: Transaction, attachment: Attachment.ColoredCoinsAssetTransfer) {
        this.dbKey = dbKey
        this.id = transaction.id
        this.height = transaction.height
        this.assetId = attachment.assetId
        this.senderId = transaction.senderId
        this.recipientId = transaction.recipientId
        this.quantityQNT = attachment.quantityQNT
        this.timestamp = transaction.blockTimestamp
    }

    protected constructor(id: Long, dbKey: BurstKey, assetId: Long, height: Int, senderId: Long, recipientId: Long, quantityQNT: Long, timestamp: Int) {
        this.id = id
        this.dbKey = dbKey
        this.assetId = assetId
        this.height = height
        this.senderId = senderId
        this.recipientId = recipientId
        this.quantityQNT = quantityQNT
        this.timestamp = timestamp
    }
}
