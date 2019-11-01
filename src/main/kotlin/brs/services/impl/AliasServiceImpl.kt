package brs.services.impl

import brs.db.BurstKey
import brs.db.VersionedEntityTable
import brs.entity.Alias
import brs.entity.Alias.Offer
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.services.AliasService
import brs.transaction.appendix.Attachment

class AliasServiceImpl(dp: DependencyProvider) : AliasService {
    private val aliasStore = dp.aliasStore
    private val aliasTable: VersionedEntityTable<Alias>
    private val aliasDbKeyFactory: BurstKey.LongKeyFactory<Alias>
    private val offerTable: VersionedEntityTable<Offer>
    private val offerDbKeyFactory: BurstKey.LongKeyFactory<Offer>

    override fun getAliasCount() = aliasTable.count.toLong()

    init {
        this.aliasTable = aliasStore.aliasTable
        this.aliasDbKeyFactory = aliasStore.aliasDbKeyFactory
        this.offerTable = aliasStore.offerTable
        this.offerDbKeyFactory = aliasStore.offerDbKeyFactory
    }

    override fun getAlias(aliasName: String): Alias? {
        return aliasStore.getAlias(aliasName)
    }

    override fun getAlias(aliasId: Long): Alias? {
        return aliasTable[aliasDbKeyFactory.newKey(aliasId)]
    }

    override fun getOffer(alias: Alias): Offer? {
        return offerTable[offerDbKeyFactory.newKey(alias.id)]
    }

    override fun getAliasesByOwner(accountId: Long, from: Int, to: Int): Collection<Alias> {
        return aliasStore.getAliasesByOwner(accountId, from, to)
    }

    override fun addOrUpdateAlias(transaction: Transaction, attachment: Attachment.MessagingAliasAssignment) {
        var alias = getAlias(attachment.aliasName)
        if (alias == null) {
            val aliasDBId = aliasDbKeyFactory.newKey(transaction.id)
            alias = Alias(transaction.id, aliasDBId, transaction, attachment)
        } else {
            alias.accountId = transaction.senderId
            alias.aliasURI = attachment.aliasURI
            alias.timestamp = transaction.blockTimestamp
        }
        aliasTable.insert(alias)
    }

    override fun sellAlias(transaction: Transaction, attachment: Attachment.MessagingAliasSell) {
        val aliasName = attachment.aliasName
        val pricePlanck = attachment.pricePlanck
        val buyerId = transaction.recipientId
        if (pricePlanck > 0) {
            val alias = getAlias(aliasName) ?: error("Could not find alias with name \"$aliasName\"")
            val offer = getOffer(alias)
            if (offer == null) {
                val dbKey = offerDbKeyFactory.newKey(alias.id)
                offerTable.insert(Offer(dbKey, alias.id, pricePlanck, buyerId))
            } else {
                offer.pricePlanck = pricePlanck
                offer.buyerId = buyerId
                offerTable.insert(offer)
            }
        } else {
            changeOwner(buyerId, aliasName, transaction.blockTimestamp)
        }
    }

    override fun changeOwner(newOwnerId: Long, aliasName: String, timestamp: Int) {
        val alias = getAlias(aliasName) ?: error("Could not find alias with name \"$aliasName\"")
        alias.accountId = newOwnerId
        alias.timestamp = timestamp
        aliasTable.insert(alias)

        val offer = getOffer(alias)
        if (offer != null) {
            offerTable.delete(offer)
        }
    }
}
