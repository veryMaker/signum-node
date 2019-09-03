package brs.grpc.handlers

import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.peer.Peer
import brs.peer.Peers

class GetPeerHandler : GrpcApiHandler<BrsApi.GetPeerRequest, BrsApi.Peer> {
    @Throws(Exception::class)
    override fun handleRequest(getPeerRequest: BrsApi.GetPeerRequest): BrsApi.Peer {
        val peer = Peers.getPeer(getPeerRequest.peerAddress) ?: throw ApiException("Could not find peer")
        return BrsApi.Peer.newBuilder()
                .setState(peer.state.toProtobuf())
                .setAnnouncedAddress(peer.announcedAddress)
                .setShareAddress(peer.shareAddress())
                .setDownloadedVolume(peer.downloadedVolume)
                .setUploadedVolume(peer.uploadedVolume)
                .setApplication(peer.application)
                .setVersion(peer.version.toStringIfNotEmpty())
                .setPlatform(peer.platform)
                .setBlacklisted(peer.isBlacklisted)
                .setLastUpdated(peer.lastUpdated)
                .build()
    }
}
