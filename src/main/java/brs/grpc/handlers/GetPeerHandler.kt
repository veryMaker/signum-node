package brs.grpc.handlers

import brs.DependencyProvider
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi

class GetPeerHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.GetPeerRequest, BrsApi.Peer> {
    @Throws(Exception::class)
    override fun handleRequest(request: BrsApi.GetPeerRequest): BrsApi.Peer {
        val peer = dp.peers.getPeer(request.peerAddress) ?: throw ApiException("Could not find peer")
        return BrsApi.Peer.newBuilder()
                .setState(peer.state.toProtobuf())
                .setAnnouncedAddress(peer.announcedAddress)
                .setShareAddress(peer.shareAddress)
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
