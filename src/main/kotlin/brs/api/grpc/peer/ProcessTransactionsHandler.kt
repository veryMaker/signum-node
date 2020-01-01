package brs.api.grpc.peer

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.PeerApi
import brs.entity.DependencyProvider

internal class ProcessTransactionsHandler(private val dp: DependencyProvider) : GrpcApiHandler<PeerApi.RawTransactions, PeerApi.ProcessResult> {
    override fun handleRequest(request: PeerApi.RawTransactions): PeerApi.ProcessResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
