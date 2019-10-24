package brs.db

interface PeerDb : Table {
    fun loadPeers(): List<String>

    fun deletePeers(peers: Collection<String>)

    fun addPeers(peers: Collection<String>)
}
