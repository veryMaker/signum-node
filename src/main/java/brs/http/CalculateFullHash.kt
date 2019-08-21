package brs.http

import brs.crypto.Crypto
import brs.util.Convert
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import java.security.MessageDigest

import brs.http.JSONResponses.MISSING_SIGNATURE_HASH
import brs.http.JSONResponses.MISSING_UNSIGNED_BYTES
import brs.http.common.Parameters.*

internal class CalculateFullHash : APIServlet.JsonRequestHandler(arrayOf(APITag.TRANSACTIONS), UNSIGNED_TRANSACTION_BYTES_PARAMETER, SIGNATURE_HASH_PARAMETER) {

    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val unsignedBytesString = Convert.emptyToNull(req.getParameter(UNSIGNED_TRANSACTION_BYTES_PARAMETER))
        val signatureHashString = Convert.emptyToNull(req.getParameter(SIGNATURE_HASH_PARAMETER))

        if (unsignedBytesString == null) {
            return MISSING_UNSIGNED_BYTES
        } else if (signatureHashString == null) {
            return MISSING_SIGNATURE_HASH
        }

        val digest = Crypto.sha256()
        digest.update(Convert.parseHexString(unsignedBytesString)!!)
        val fullHash = digest.digest(Convert.parseHexString(signatureHashString))
        val response = JsonObject()
        response.addProperty(FULL_HASH_RESPONSE, Convert.toHexString(fullHash))

        return response

    }

}
