package brs.services

import brs.Alias
import brs.Alias.Offer
import brs.Attachment
import brs.Transaction

interface AliasService {
    val aliasCount: Long

    fun getAlias(aliasId: Long): Alias?

    fun getAlias(aliasName: String): Alias?

    fun getOffer(alias: Alias): Offer?

    fun getAliasesByOwner(accountId: Long, from: Int, to: Int): Collection<Alias>

    fun addOrUpdateAlias(transaction: Transaction, attachment: Attachment.MessagingAliasAssignment)

    fun sellAlias(transaction: Transaction, attachment: Attachment.MessagingAliasSell)

    fun changeOwner(newOwnerId: Long, aliasName: String, timestamp: Int)
}
