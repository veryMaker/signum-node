package brs.http

import brs.crypto.Crypto
import brs.http.JSONResponses.MISSING_SIGNATURE_HASH
import brs.http.JSONResponses.MISSING_UNSIGNED_BYTES
import brs.http.common.Parameters.FULL_HASH_RESPONSE
import brs.http.common.Parameters.SIGNATURE_HASH_PARAMETER
import brs.http.common.Parameters.UNSIGNED_TRANSACTION_BYTES_PARAMETER
import brs.util.Convert
import brs.util.parseHexString
import brs.util.toHexString
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class CalculateFullHash : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), UNSIGNED_TRANSACTION_BYTES_PARAMETER, SIGNATURE_HASH_PARAMETER) {

    override suspend fun processRequest(request: HttpServletRequest): JsonElement {

        val unsignedBytesString = Convert.emptyToNull(request.getParameter(UNSIGNED_TRANSACTION_BYTES_PARAMETER))
        val signatureHashString = Convert.emptyToNull(request.getParameter(SIGNATURE_HASH_PARAMETER))

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
