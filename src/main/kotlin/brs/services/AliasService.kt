package brs.services

import brs.entity.Alias
import brs.entity.Alias.Offer
import brs.transaction.appendix.Attachment
import brs.entity.Transaction

interface AliasService {
    fun getAliasCount(): Long

    fun getAlias(aliasId: Long): Alias?

    fun getAlias(aliasName: String): Alias?

    fun getOffer(alias: Alias): Offer?

    fun getAliasesByOwner(accountId: Long, from: Int, to: Int): Collection<Alias>

    fun addOrUpdateAlias(transaction: Transaction, attachment: Attachment.MessagingAliasAssignment)

    fun sellAlias(transaction: Transaction, attachment: Attachment.MessagingAliasSell)

    fun changeOwner(newOwnerId: Long, aliasName: String, timestamp: Int)
}
