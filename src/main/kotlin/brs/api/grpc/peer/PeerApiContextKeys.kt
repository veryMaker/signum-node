package brs.api.grpc.peer

import io.grpc.*

internal object PeerApiContextKeys {
    val REMOTE_ADDRESS: Context.Key<String> = Context.key("remoteAddr")

    object RemoteAddressInterceptor : ServerInterceptor {
        override fun <ReqT : Any?, RespT : Any?> interceptCall(call: ServerCall<ReqT, RespT>, headers: Metadata, next: ServerCallHandler<ReqT, RespT>): ServerCall.Listener<ReqT> {
            val address = call.attributes[Grpc.TRANSPORT_ATTR_REMOTE_ADDR].toString().trimStart('/')
            val ctx = Context.current().withValue(REMOTE_ADDRESS, address)
            return Contexts.interceptCall(ctx, call, headers, next)
        }
    }
}
