package brs.db.sql

import brs.DependencyProvider
import brs.db.PeerDb
import brs.schema.Tables.PEER

class SqlPeerDb(private val dp: DependencyProvider) : PeerDb {
    override fun loadPeers(): List<String> {
        return dp.db.useDslContext<List<String>> { ctx -> ctx.selectFrom(PEER).fetch(PEER.ADDRESS, String::class.java) }
    }

    override fun deletePeers(peers: Collection<String>) {
        dp.db.useDslContext { ctx ->
            for (peer in peers) {
                ctx.deleteFrom(PEER).where(PEER.ADDRESS.eq(peer)).execute()
            }
        }
    }

    override fun addPeers(peers: Collection<String>) {
        dp.db.useDslContext { ctx ->
            ctx.batch(peers.map { peer -> ctx.insertInto(PEER).set(PEER.ADDRESS, peer) }).execute()
        }
    }

    override fun optimize() {
        dp.db.optimizeTable(PEER.name)
    }
}
