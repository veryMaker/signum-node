package brs.db.sql

import brs.DependencyProvider
import brs.db.BlockDb
import brs.db.PeerDb
import brs.db.TransactionDb
import brs.db.store.Dbs

class SqlDbs(dp: DependencyProvider) : Dbs {

    override val blockDb: BlockDb
    override val transactionDb: TransactionDb
    override val peerDb: PeerDb

    init {
        this.blockDb = SqlBlockDb(dp)
        this.transactionDb = SqlTransactionDb(dp)
        this.peerDb = SqlPeerDb()
    }
}
