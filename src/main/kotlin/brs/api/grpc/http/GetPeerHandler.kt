package brs.api.grpc.http

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.ApiException
import brs.entity.DependencyProvider

class GetPeerHandler(private val dp: DependencyProvider) : GrpcApiHandler<BrsApi.GetPeerRequest, BrsApi.Peer> {
    override fun handleRequest(request: BrsApi.GetPeerRequest): BrsApi.Peer {
        val peer = dp.peerService.getPeer(request.peerAddress) ?: throw ApiException("Could not find peer")
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
