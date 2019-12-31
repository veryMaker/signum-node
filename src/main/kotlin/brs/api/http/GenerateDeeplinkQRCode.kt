package brs.api.http

import brs.api.http.APIServlet.HttpRequestHandler
import brs.api.http.JSONResponses.FEE_OR_FEE_SUGGESTION_REQUIRED
import brs.api.http.JSONResponses.FEE_SUGGESTION_TYPE_INVALID
import brs.api.http.JSONResponses.INCORRECT_AMOUNT
import brs.api.http.JSONResponses.INCORRECT_FEE
import brs.api.http.JSONResponses.INCORRECT_MESSAGE_LENGTH
import brs.api.http.JSONResponses.MISSING_AMOUNT
import brs.api.http.JSONResponses.MISSING_RECEIVER_ID
import brs.api.http.common.Parameters
import brs.api.http.common.Parameters.AMOUNT_PLANCK_PARAMETER
import brs.api.http.common.Parameters.FEE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.FEE_SUGGESTION_TYPE_PARAMETER
import brs.api.http.common.Parameters.IMMUTABLE_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_PARAMETER
import brs.api.http.common.Parameters.RECEIVER_ID_PARAMETER
import brs.entity.FeeSuggestion
import brs.objects.Constants
import brs.services.DeeplinkQRCodeGeneratorService
import brs.util.convert.emptyToNull
import brs.util.jetty.get
import brs.util.logging.safeError
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.LoggerFactory
import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Deprecated("Use DeeplinkGeneratorServiceImpl")
internal class GenerateDeeplinkQRCode(private val deeplinkQRCodeGeneratorService: DeeplinkQRCodeGeneratorService) :
    HttpRequestHandler(
        arrayOf(APITag.CREATE_TRANSACTION, APITag.TRANSACTIONS),
        IMMUTABLE_PARAMETER,
        RECEIVER_ID_PARAMETER,
        AMOUNT_PLANCK_PARAMETER,
        FEE_PLANCK_PARAMETER,
        FEE_SUGGESTION_TYPE_PARAMETER,
        MESSAGE_PARAMETER
    ) {

    private val logger = LoggerFactory.getLogger(GenerateDeeplinkQRCode::class.java)

    override fun processRequest(request: HttpServletRequest, resp: HttpServletResponse) {
        try {
            val immutable = Parameters.isTrue(request[IMMUTABLE_PARAMETER])

            val receiverId = request[RECEIVER_ID_PARAMETER].emptyToNull()

            if (receiverId.isNullOrBlank()) {
                addErrorMessage(resp, MISSING_RECEIVER_ID)
                return
            }

            val amountPlanckString = request[AMOUNT_PLANCK_PARAMETER].emptyToNull()
            if (amountPlanckString.isNullOrBlank()) {
                addErrorMessage(resp, MISSING_AMOUNT)
                return
            }

            val amountPlanck = amountPlanckString.toLong()
            if (immutable && (amountPlanck < 0 || amountPlanck > Constants.MAX_BALANCE_PLANCK)) {
                addErrorMessage(resp, INCORRECT_AMOUNT)
                return
            }

            val feePlanckString = request[FEE_PLANCK_PARAMETER].emptyToNull()

            var feePlanck: Long? = null

            if (!feePlanckString.isNullOrBlank()) {
                feePlanck = feePlanckString.toLong()

                if (feePlanck <= 0 || feePlanck >= Constants.MAX_BALANCE_PLANCK) {
                    addErrorMessage(resp, INCORRECT_FEE)
                    return
                }
            }

            var feeSuggestionType: FeeSuggestion.Type? = null

            if (feePlanck == null) {
                val feeSuggestionTypeString = request[FEE_SUGGESTION_TYPE_PARAMETER].emptyToNull()

                if (feeSuggestionTypeString.isNullOrBlank()) {
                    addErrorMessage(resp, FEE_OR_FEE_SUGGESTION_REQUIRED)
                    return
                } else {
                    feeSuggestionType = FeeSuggestion.Type.getByType(feeSuggestionTypeString)

                    if (feeSuggestionType == null) {
                        addErrorMessage(resp, FEE_SUGGESTION_TYPE_INVALID)
                        return
                    }
                }
            }

            val message = request[MESSAGE_PARAMETER].emptyToNull()

            if (!message.isNullOrBlank() && message.length > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
                addErrorMessage(resp, INCORRECT_MESSAGE_LENGTH)
                return
            }

            resp.contentType = "image/png"

            val qrImage = deeplinkQRCodeGeneratorService.generateRequestBurstDeepLinkQRCode(
                receiverId,
                amountPlanck,
                feeSuggestionType,
                feePlanck,
                message,
                immutable
            )
            ImageIO.write(qrImage, "png", resp.outputStream)
            resp.outputStream.close()
        } catch (e: IllegalArgumentException) {
            logger.safeError(e) { "Problem with arguments" }
        } catch (e: Exception) {
            logger.safeError(e) { "Could not generate Deeplink QR code" }
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
        }
    }
}
