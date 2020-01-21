package brs.api.grpc.peer

import brs.api.grpc.ApiException
import brs.api.grpc.ProtoBuilder
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import brs.peer.Peer
import com.google.protobuf.Empty

internal class AddBlockHandler(private val dp: DependencyProvider) : GrpcPeerApiHandler<PeerApi.ProcessBlockRequest, Empty>(dp) {
    override fun handleRequest(peer: Peer, request: PeerApi.ProcessBlockRequest): Empty {
        try {
            if (dp.blockchainService.lastBlock.id != request.previousBlockId) {
                // do this check first to avoid validation failures of future blocks and transactions
                // when loading blockchain from scratch
                throw ApiException("Previous block mismatch")
            }
            dp.blockchainProcessorService.processPeerBlock(ProtoBuilder.parseRawBlock(dp, request.block), peer)
            return Empty.getDefaultInstance()
        } catch (e: Exception) {
            peer.blacklist(e, "Received invalid block: $request")
            throw e
        }
    }
}
