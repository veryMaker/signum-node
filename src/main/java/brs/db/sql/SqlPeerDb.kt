package brs.db.sql

import brs.db.PeerDb
import brs.schema.tables.records.PeerRecord
import org.jooq.Insert
import java.util.stream.Collectors

import brs.schema.Tables.PEER

class SqlPeerDb : PeerDb {

    override fun loadPeers(): List<String> {
        return Db.useDSLContext<List<String>> { ctx -> ctx.selectFrom(PEER).fetch(PEER.ADDRESS, String::class.java) }
    }

    override fun deletePeers(peers: Collection<String>) {
        Db.useDSLContext { ctx ->
            for (peer in peers) {
                ctx.deleteFrom(PEER).where(PEER.ADDRESS.eq(peer)).execute()
            }
        }
    }

    override fun addPeers(peers: Collection<String>) {
        Db.useDSLContext { ctx ->
            val inserts = peers.stream().map<InsertSetMoreStep<PeerRecord>> { peer -> ctx.insertInto(PEER).set(PEER.ADDRESS, peer) }.collect<List<Insert<PeerRecord>>, Any>(Collectors.toList())
            ctx.batch(inserts).execute()
        }
    }

    override fun optimize() {
        Db.optimizeTable(PEER.name)
    }
}
