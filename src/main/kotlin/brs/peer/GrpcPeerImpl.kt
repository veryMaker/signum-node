package brs.peer

import brs.entity.Block
import brs.entity.PeerInfo
import brs.entity.Transaction
import brs.util.Version
import java.math.BigInteger

class GrpcPeerImpl : Peer {
    override val remoteAddress: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override var address: PeerAddress
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var state: Peer.State
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var version: Version
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var application: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override var platform: String
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}
    override val isWellKnown: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val isRebroadcastTarget: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val isBlacklisted: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val isAtLeastMyVersion: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val downloadedVolume: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val uploadedVolume: Long
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override var lastUpdated: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override fun connect(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateUploadedVolume(volume: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isHigherOrEqualVersionThan(version: Version): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun blacklist(cause: Exception, description: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun blacklist(description: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun blacklist() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unBlacklist() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateBlacklistedStatus(curTime: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateDownloadedVolume(volume: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun exchangeInfo(): PeerInfo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCumulativeDifficulty(): Pair<BigInteger, Int> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUnconfirmedTransactions(): Collection<Transaction> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMilestoneBlockIds(): Pair<Collection<Long>, Boolean> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMilestoneBlockIds(lastMilestoneBlockId: Long): Pair<Collection<Long>, Boolean> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendUnconfirmedTransactions(transactions: Collection<Transaction>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNextBlocks(lastBlockId: Long): Collection<Block> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNextBlockIds(lastBlockId: Long): Collection<Long> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addPeers(announcedAddresses: Collection<PeerAddress>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPeers(): Collection<PeerAddress> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendBlock(block: Block): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override var shareAddress: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override fun updateAddress(newAnnouncedAddress: PeerAddress) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override val readyToSend: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}