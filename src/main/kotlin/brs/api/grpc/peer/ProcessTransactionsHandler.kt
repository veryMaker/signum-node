package brs.api.grpc.peer

import brs.api.grpc.ProtoBuilder
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider
import brs.peer.Peer
import com.google.protobuf.Empty

internal class ProcessTransactionsHandler(private val dp: DependencyProvider) : GrpcPeerApiHandler<PeerApi.RawTransactions, Empty>(dp) {
    override fun handleRequest(peer: Peer, request: PeerApi.RawTransactions): Empty {
        return try {
            dp.transactionProcessorService.processPeerTransactions(request.transactionsList.map { ProtoBuilder.parseRawTransaction(dp, it) }, peer) // TODO this is not locking sync obj...
            Empty.getDefaultInstance()
        } catch (e: Exception) {
            peer.blacklist(e, "Received invalid transactions: $request")
            throw e
        }
    }
}
