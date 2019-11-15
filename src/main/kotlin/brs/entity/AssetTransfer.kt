package brs.entity

import brs.db.BurstKey
import brs.transaction.appendix.Attachment

open class AssetTransfer {
    val id: Long
    val dbKey: BurstKey
    val assetId: Long
    val height: Int
    val senderId: Long
    val recipientId: Long
    val quantity: Long
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
        this.quantity = attachment.quantity
        this.timestamp = transaction.blockTimestamp
    }

    protected constructor(
        id: Long,
        dbKey: BurstKey,
        assetId: Long,
        height: Int,
        senderId: Long,
        recipientId: Long,
        quantity: Long,
        timestamp: Int
    ) {
        this.id = id
        this.dbKey = dbKey
        this.assetId = assetId
        this.height = height
        this.senderId = senderId
        this.recipientId = recipientId
        this.quantity = quantity
        this.timestamp = timestamp
    }
}
