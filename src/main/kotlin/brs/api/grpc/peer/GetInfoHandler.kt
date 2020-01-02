package brs.api.grpc.peer

import brs.Burst
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import brs.objects.Props
import brs.peer.Peer
import brs.services.PeerService

internal class GetInfoHandler(private val dp: DependencyProvider) : GrpcPeerApiHandler<PeerApi.PeerInfo, PeerApi.PeerInfo>(dp) {
    private val myInfo = PeerApi.PeerInfo.newBuilder()
        .setApplication(Burst.APPLICATION)
        .setVersion(Burst.VERSION.toString())
        .setShareAddress(dp.propertyService.get(Props.P2P_SHARE_MY_ADDRESS) && !dp.propertyService.get(Props.DEV_OFFLINE))
        .setAnnouncedAddress(dp.peerService.announcedAddress)
        .setPlatform(dp.peerService.myPlatform)
        .build()

    override fun handleRequest(peer: Peer, request: PeerApi.PeerInfo): PeerApi.PeerInfo {
        var announcedAddress: String = request.announcedAddress
        if (announcedAddress.isNotEmpty()) {
            announcedAddress = announcedAddress.trim { it <= ' ' }
            if (announcedAddress.isNotEmpty()) {
                if (peer.announcedAddress != null && announcedAddress != peer.announcedAddress) {
                    // force verification of changed announced address
                    peer.state = Peer.State.NON_CONNECTED
                }
                peer.announcedAddress = announcedAddress
            }
        }
        var application = request.application
        if (application.isNullOrEmpty()) {
            application = "?"
        }
        peer.application = application.trim { it <= ' ' }

        var version = request.version
        if (version.isNullOrEmpty()) {
            version = "?"
        }
        peer.setVersion(version.trim { it <= ' ' })

        var platform = request.platform
        if (platform.isNullOrEmpty()) {
            platform = "?"
        }
        peer.platform = platform.trim { it <= ' ' }

        peer.shareAddress = request.shareAddress
        peer.lastUpdated = dp.timeService.epochTime

        dp.peerService.notifyListeners(peer, PeerService.Event.ADDED_ACTIVE_PEER)

        return myInfo
    }
}
