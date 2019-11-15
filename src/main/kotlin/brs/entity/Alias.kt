package brs.entity

import brs.db.BurstKey
import brs.transaction.appendix.Attachment

open class Alias {

    var accountId: Long = 0
    val id: Long
    val dbKey: BurstKey
    val aliasName: String
    var aliasURI: String? = null
    var timestamp: Int = 0

    private constructor(
        dbKey: BurstKey,
        id: Long,
        accountId: Long,
        aliasName: String,
        aliasURI: String,
        timestamp: Int
    ) {
        this.id = id
        this.dbKey = dbKey
        this.accountId = accountId
        this.aliasName = aliasName
        this.aliasURI = aliasURI
        this.timestamp = timestamp
    }

    protected constructor(
        id: Long,
        accountId: Long,
        aliasName: String,
        aliasURI: String,
        timestamp: Int,
        dbKey: BurstKey
    ) {
        this.id = id
        this.dbKey = dbKey
        this.accountId = accountId
        this.aliasName = aliasName
        this.aliasURI = aliasURI
        this.timestamp = timestamp
    }

    constructor(
        aliasId: Long,
        dbKey: BurstKey,
        transaction: Transaction,
        attachment: Attachment.MessagingAliasAssignment
    ) : this(
        dbKey, aliasId, transaction.senderId, attachment.aliasName, attachment.aliasURI,
        transaction.blockTimestamp
    )

    open class Offer {

        var pricePlanck: Long = 0
        var buyerId: Long = 0
        val id: Long
        val dbKey: BurstKey

        constructor(dbKey: BurstKey, aliasId: Long, pricePlanck: Long, buyerId: Long) {
            this.dbKey = dbKey
            this.pricePlanck = pricePlanck
            this.buyerId = buyerId
            this.id = aliasId
        }

        protected constructor(aliasId: Long, pricePlanck: Long, buyerId: Long, nxtKey: BurstKey) {
            this.pricePlanck = pricePlanck
            this.buyerId = buyerId
            this.id = aliasId
            this.dbKey = nxtKey
        }
    }

}
