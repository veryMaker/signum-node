package brs.db

interface PeerDb : Table {
    suspend fun loadPeers(): List<String>

    suspend fun deletePeers(peers: Collection<String>)

    suspend fun addPeers(peers: Collection<String>)
}
