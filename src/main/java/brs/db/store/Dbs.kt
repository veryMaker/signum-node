package brs.db.store

import brs.db.BlockDb
import brs.db.PeerDb
import brs.db.TransactionDb

interface Dbs { // TODO remove, integrate with dependencyProvider
    val blockDb: BlockDb

    val transactionDb: TransactionDb

    val peerDb: PeerDb
}
