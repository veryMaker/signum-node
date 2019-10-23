package brs.db.sql

import brs.DependencyProvider
import brs.db.PeerDb
import brs.schema.Tables.PEER

class SqlPeerDb(private val dp: DependencyProvider) : PeerDb {
    override suspend fun loadPeers(): List<String> {
        return dp.db.getUsingDslContext<List<String>> { ctx -> ctx.selectFrom(PEER).fetch(PEER.ADDRESS, String::class.java) }
    }

    override suspend fun deletePeers(peers: Collection<String>) {
        dp.db.useDslContext { ctx ->
            for (peer in peers) {
                ctx.deleteFrom(PEER).where(PEER.ADDRESS.eq(peer)).execute()
            }
        }
    }

    override suspend fun addPeers(peers: Collection<String>) {
        dp.db.useDslContext { ctx ->
            ctx.batch(peers.map { peer -> ctx.insertInto(PEER).set(PEER.ADDRESS, peer) }).execute()
        }
    }

    override suspend fun optimize() {
        dp.db.optimizeTable(PEER.name)
    }
}
