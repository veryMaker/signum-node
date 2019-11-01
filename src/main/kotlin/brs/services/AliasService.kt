package brs.services

import brs.entity.Alias
import brs.entity.Alias.Offer
import brs.entity.Transaction
import brs.transaction.appendix.Attachment

interface AliasService {
    /**
     * TODO
     */
    fun getAliasCount(): Long

    /**
     * TODO
     */
    fun getAlias(aliasId: Long): Alias?

    /**
     * TODO
     */
    fun getAlias(aliasName: String): Alias?

    /**
     * TODO
     */
    fun getOffer(alias: Alias): Offer?

    /**
     * TODO
     */
    fun getAliasesByOwner(accountId: Long, from: Int, to: Int): Collection<Alias>

    /**
     * TODO
     */
    fun addOrUpdateAlias(transaction: Transaction, attachment: Attachment.MessagingAliasAssignment)

    /**
     * TODO
     */
    fun sellAlias(transaction: Transaction, attachment: Attachment.MessagingAliasSell)

    /**
     * TODO
     */
    fun changeOwner(newOwnerId: Long, aliasName: String, timestamp: Int)
}
