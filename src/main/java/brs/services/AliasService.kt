package brs.services

import brs.Alias
import brs.Alias.Offer
import brs.Attachment
import brs.Transaction

interface AliasService {
    suspend fun getAliasCount(): Long

    suspend fun getAlias(aliasId: Long): Alias?

    suspend fun getAlias(aliasName: String): Alias?

    suspend fun getOffer(alias: Alias): Offer?

    suspend fun getAliasesByOwner(accountId: Long, from: Int, to: Int): Collection<Alias>

    suspend fun addOrUpdateAlias(transaction: Transaction, attachment: Attachment.MessagingAliasAssignment)

    suspend fun sellAlias(transaction: Transaction, attachment: Attachment.MessagingAliasSell)

    suspend fun changeOwner(newOwnerId: Long, aliasName: String, timestamp: Int)
}
