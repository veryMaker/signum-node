package brs.db

interface PeerDb : Table {
    /**
     * Get all peer addresses stored in the database
     */
    fun loadPeers(): List<String>

    /**
     * Removes all peers in the DB that are not in [peers] and adds peers that are in [peers] but not in the DB
     */
    fun updatePeers(peers: List<String>)
}
