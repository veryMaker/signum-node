package brs.db

interface PeerDb : Table {
    /**
     * TODO
     */
    fun loadPeers(): List<String>

    /**
     * TODO
     */
    fun deletePeers(peers: Collection<String>)

    /**
     * TODO
     */
    fun addPeers(peers: Collection<String>)
}
