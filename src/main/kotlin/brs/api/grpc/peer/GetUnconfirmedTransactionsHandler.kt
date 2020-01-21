package brs.api.grpc.peer

import brs.api.grpc.ProtoBuilder
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import brs.peer.Peer
import com.google.protobuf.Empty

internal class GetUnconfirmedTransactionsHandler(private val dp: DependencyProvider) : GrpcPeerApiHandler<Empty, PeerApi.RawTransactions>(dp) {
    override fun handleRequest(peer: Peer, request: Empty): PeerApi.RawTransactions {
        val unconfirmedTransactions = dp.unconfirmedTransactionService.getAllFor(peer)
        dp.unconfirmedTransactionService.markFingerPrintsOf(peer, unconfirmedTransactions)
        return PeerApi.RawTransactions.newBuilder()
            .addAllTransactions(unconfirmedTransactions.map { ProtoBuilder.buildRawTransaction(it) })
            .build()
    }
}
