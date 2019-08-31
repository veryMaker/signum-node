package brs.db.store

import brs.Alias
import brs.db.BurstKey
import brs.db.VersionedEntityTable

interface AliasStore {
    val aliasDbKeyFactory: BurstKey.LongKeyFactory<Alias>
    val offerDbKeyFactory: BurstKey.LongKeyFactory<Alias.Offer>

    val aliasTable: VersionedEntityTable<Alias>

    val offerTable: VersionedEntityTable<Alias.Offer>

    fun getAliasesByOwner(accountId: Long, from: Int, to: Int): Collection<Alias>

    fun getAlias(aliasName: String): Alias
}
