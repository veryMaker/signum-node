package brs.peer

import brs.api.grpc.proto.BrsApi
import brs.util.Version
import com.google.gson.JsonElement
import com.google.gson.JsonObject

interface Peer : Comparable<Peer> {

    val peerAddress: String

    var announcedAddress: String?

    var state: State

    val version: Version

    var application: String

    var platform: String

    val software: String

    val port: Int

    val isWellKnown: Boolean

    val isRebroadcastTarget: Boolean

    val isBlacklisted: Boolean

    val isAtLeastMyVersion: Boolean

    val downloadedVolume: Long

    val uploadedVolume: Long

    var lastUpdated: Int

    fun connect(currentTime: Int)

    enum class State {
        NON_CONNECTED, CONNECTED, DISCONNECTED;

        fun toProtobuf(): BrsApi.PeerState {
            return when (this) {
                NON_CONNECTED -> BrsApi.PeerState.NON_CONNECTED
                CONNECTED -> BrsApi.PeerState.CONNECTED
                DISCONNECTED -> BrsApi.PeerState.NON_CONNECTED
                else -> BrsApi.PeerState.UNRECOGNIZED
            }
        }

        companion object {

            fun fromProtobuf(peer: BrsApi.PeerState): State? {
                return when (peer) {
                    BrsApi.PeerState.NON_CONNECTED -> NON_CONNECTED
                    BrsApi.PeerState.CONNECTED -> CONNECTED
                    BrsApi.PeerState.DISCONNECTED -> DISCONNECTED
                    else -> null
                }
            }
        }
    }

    fun updateUploadedVolume(volume: Long)

    fun isHigherOrEqualVersionThan(version: Version): Boolean

    fun blacklist(cause: Exception, description: String)

    fun blacklist(description: String)

    fun blacklist()

    fun unBlacklist()

    fun updateBlacklistedStatus(curTime: Long)

    fun remove()

    fun updateDownloadedVolume(volume: Long)

    fun send(request: JsonElement): JsonObject?

    companion object {
        fun isHigherOrEqualVersion(ourVersion: Version?, possiblyLowerVersion: Version?): Boolean {
            return if (ourVersion == null || possiblyLowerVersion == null) false else possiblyLowerVersion.isGreaterThanOrEqualTo(
                ourVersion
            )
        }
    }

    fun setVersion(version: String?)

    var shareAddress: Boolean
}
