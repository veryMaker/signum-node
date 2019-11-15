package brs.api.http

import brs.api.http.JSONResponses.MISSING_SIGNATURE_HASH
import brs.api.http.JSONResponses.MISSING_UNSIGNED_BYTES
import brs.api.http.common.Parameters.FULL_HASH_RESPONSE
import brs.api.http.common.Parameters.SIGNATURE_HASH_PARAMETER
import brs.api.http.common.Parameters.UNSIGNED_TRANSACTION_BYTES_PARAMETER
import brs.util.convert.emptyToNull
import brs.util.convert.parseHexString
import brs.util.convert.toHexString
import brs.util.crypto.Crypto
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class CalculateFullHash : APIServlet.JsonRequestHandler(
    arrayOf(APITag.TRANSACTIONS),
    UNSIGNED_TRANSACTION_BYTES_PARAMETER,
    SIGNATURE_HASH_PARAMETER
) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val unsignedBytesString = request.getParameter(UNSIGNED_TRANSACTION_BYTES_PARAMETER).emptyToNull()
        val signatureHashString = request.getParameter(SIGNATURE_HASH_PARAMETER).emptyToNull()

        if (unsignedBytesString == null) {
            return MISSING_UNSIGNED_BYTES
        } else if (signatureHashString == null) {
            return MISSING_SIGNATURE_HASH
        }

        val digest = Crypto.sha256()
        digest.update(unsignedBytesString.parseHexString())
        val fullHash = digest.digest(signatureHashString.parseHexString())
        val response = JsonObject()
        response.addProperty(FULL_HASH_RESPONSE, fullHash.toHexString())

        return response

    }

}
