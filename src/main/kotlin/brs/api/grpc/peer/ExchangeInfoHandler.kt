package brs.api.grpc.peer

import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import brs.peer.Peer
import brs.peer.PeerAddress
import brs.services.PeerService
import brs.util.Version

internal class ExchangeInfoHandler(private val dp: DependencyProvider) : GrpcPeerApiHandler<PeerApi.PeerInfo, PeerApi.PeerInfo>(dp) {
    override fun handleRequest(peer: Peer, request: PeerApi.PeerInfo): PeerApi.PeerInfo {
        val announcedAddress: String = request.announcedAddress
        if (announcedAddress.isNotBlank()) {
            val newAddress = PeerAddress.parse(dp, announcedAddress.trim())
            if (newAddress != null && newAddress != peer.address) {
                peer.updateAddress(newAddress)
            }
        }
        var application = request.application
        if (application.isNullOrEmpty()) {
            application = "?"
        }
        peer.application = application.trim()

        var version = request.version
        if (version.isNullOrEmpty()) {
            version = "?"
        }
        peer.version = Version.parse(version.trim())

        var platform = request.platform
        if (platform.isNullOrEmpty()) {
            platform = "?"
        }
        peer.platform = platform.trim()

        peer.shareAddress = request.shareAddress
        peer.lastUpdated = dp.timeService.epochTime

        dp.peerService.notifyListeners(peer, PeerService.Event.ADDED_ACTIVE_PEER)

        return dp.peerService.myProtoPeerInfo
    }
}
