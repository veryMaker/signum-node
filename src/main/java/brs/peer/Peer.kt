package brs.peer

import brs.Version
import brs.grpc.proto.BrsApi
import com.google.gson.JsonElement
import com.google.gson.JsonObject

interface Peer : Comparable<Peer> {

    val peerAddress: String

    val announcedAddress: String

    var state: State

    val version: Version

    val application: String

    val platform: String

    val software: String

    val port: Int

    val isWellKnown: Boolean

    val isRebroadcastTarget: Boolean

    val isBlacklisted: Boolean

    val isAtLeastMyVersion: Boolean

    val downloadedVolume: Long

    val uploadedVolume: Long

    val lastUpdated: Int

    fun connect(currentTime: Int)

    enum class State {
        NON_CONNECTED, CONNECTED, DISCONNECTED;

        fun toProtobuf(): BrsApi.PeerState {
            when (this) {
                NON_CONNECTED -> return BrsApi.PeerState.NON_CONNECTED
                CONNECTED -> return BrsApi.PeerState.CONNECTED
                DISCONNECTED -> return BrsApi.PeerState.NON_CONNECTED
                else -> return BrsApi.PeerState.UNRECOGNIZED
            }
        }

        companion object {

            fun fromProtobuf(peer: BrsApi.PeerState): State? {
                when (peer) {
                    BrsApi.PeerState.NON_CONNECTED -> return NON_CONNECTED
                    BrsApi.PeerState.CONNECTED -> return CONNECTED
                    BrsApi.PeerState.DISCONNECTED -> return DISCONNECTED
                    else -> return null
                }
            }
        }
    }

    fun updateUploadedVolume(volume: Long)

    fun shareAddress(): Boolean

    fun isHigherOrEqualVersionThan(version: Version): Boolean

    fun blacklist(cause: Exception, description: String)

    fun blacklist(description: String)

    fun blacklist()

    fun unBlacklist()

    fun updateBlacklistedStatus(curTime: Long)

    fun remove()

    fun isState(cmpState: State): Boolean

    fun updateDownloadedVolume(volume: Long)

    fun send(request: JsonElement): JsonObject?

    companion object {

        fun isHigherOrEqualVersion(ourVersion: Version?, possiblyLowerVersion: Version?): Boolean {
            return if (ourVersion == null || possiblyLowerVersion == null) {
                false
            } else possiblyLowerVersion.isGreaterThanOrEqualTo(ourVersion)

        }
    }
}
