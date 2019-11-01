package brs.api.grpc.handlers

import brs.entity.DependencyProvider
import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.service.ApiException
import brs.api.grpc.proto.BrsApi

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
