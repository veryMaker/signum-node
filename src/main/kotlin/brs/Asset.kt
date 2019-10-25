package brs

import brs.db.BurstKey

open class Asset {
    val id: Long
    val dbKey: BurstKey
    val accountId: Long
    val name: String?
    val description: String
    val quantity: Long
    val decimals: Byte

    protected constructor(assetId: Long, dbKey: BurstKey, accountId: Long, name: String, description: String, quantity: Long, decimals: Byte) {
        this.id = assetId
        this.dbKey = dbKey
        this.accountId = accountId
        this.name = name
        this.description = description
        this.quantity = quantity
        this.decimals = decimals
    }

    constructor(dbKey: BurstKey, transaction: Transaction, attachment: Attachment.ColoredCoinsAssetIssuance) {
        this.dbKey = dbKey
        this.id = transaction.id
        this.accountId = transaction.senderId
        this.name = attachment.name
        this.description = attachment.description
        this.quantity = attachment.quantity
        this.decimals = attachment.decimals
    }
}
