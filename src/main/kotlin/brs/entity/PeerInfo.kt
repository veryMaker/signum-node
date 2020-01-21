package brs.entity

import brs.api.grpc.proto.PeerApi
import brs.util.Version
import brs.util.convert.emptyToNull
import brs.util.json.getMemberAsString
import brs.util.json.mustGetMemberAsBoolean
import brs.util.json.mustGetMemberAsString
import com.google.gson.JsonObject

data class PeerInfo(
    val application: String,
    val version: Version,
    val platform: String,
    val announcedAddress: String?,
    val shareAddress: Boolean
) {
    companion object {
        fun fromJson(peerInfo: JsonObject): PeerInfo {
            return PeerInfo(
                peerInfo.mustGetMemberAsString("application"),
                Version.parse(peerInfo.mustGetMemberAsString("version")),
                peerInfo.mustGetMemberAsString("platform"),
                peerInfo.getMemberAsString("announcedAddress").emptyToNull(),
                peerInfo.mustGetMemberAsBoolean("shareAddress")
            )
        }

        fun fromProto(peerInfo: PeerApi.PeerInfo): PeerInfo {
            return PeerInfo(
                peerInfo.application,
                Version.parse(peerInfo.version),
                peerInfo.platform,
                peerInfo.announcedAddress.emptyToNull(),
                peerInfo.shareAddress
            )
        }
    }
}