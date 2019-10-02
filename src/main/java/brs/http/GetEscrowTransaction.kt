package brs.http

import brs.http.common.Parameters.ESCROW_PARAMETER
import brs.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.services.EscrowService
import brs.util.Convert
import brs.util.parseUnsignedLong
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

internal class GetEscrowTransaction(private val escrowService: EscrowService) : APIServlet.JsonRequestHandler(arrayOf(APITag.ACCOUNTS), ESCROW_PARAMETER) {
    override suspend fun processRequest(request: HttpServletRequest): JsonElement {
        val escrowId: Long
        try {
            escrowId = Convert.emptyToNull(request.getParameter(ESCROW_PARAMETER)).parseUnsignedLong()
        } catch (e: Exception) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 3)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid or not specified escrow")
            return response
        }

        val escrow = escrowService.getEscrowTransaction(escrowId)
        if (escrow == null) {
            val response = JsonObject()
            response.addProperty(ERROR_CODE_RESPONSE, 5)
            response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Escrow transaction not found")
            return response
        }

        return JSONData.escrowTransaction(escrow)
    }
}
