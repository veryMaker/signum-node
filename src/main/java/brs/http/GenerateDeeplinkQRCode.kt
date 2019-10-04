package brs.http

import brs.Constants
import brs.deeplink.DeeplinkQRCodeGenerator
import brs.feesuggestions.FeeSuggestionType
import brs.http.APIServlet.HttpRequestHandler
import brs.http.JSONResponses.FEE_OR_FEE_SUGGESTION_REQUIRED
import brs.http.JSONResponses.FEE_SUGGESTION_TYPE_INVALID
import brs.http.JSONResponses.INCORRECT_AMOUNT
import brs.http.JSONResponses.INCORRECT_FEE
import brs.http.JSONResponses.INCORRECT_MESSAGE_LENGTH
import brs.http.JSONResponses.MISSING_AMOUNT
import brs.http.JSONResponses.MISSING_RECEIVER_ID
import brs.http.common.Parameters
import brs.http.common.Parameters.AMOUNT_NQT_PARAMETER
import brs.http.common.Parameters.FEE_NQT_PARAMETER
import brs.http.common.Parameters.FEE_SUGGESTION_TYPE_PARAMETER
import brs.http.common.Parameters.IMMUTABLE_PARAMETER
import brs.http.common.Parameters.MESSAGE_PARAMETER
import brs.http.common.Parameters.RECEIVER_ID_PARAMETER
import brs.util.convert.emptyToNull
import brs.util.logging.safeError
import com.google.zxing.WriterException
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class GenerateDeeplinkQRCode(private val deeplinkQRCodeGenerator: DeeplinkQRCodeGenerator) : HttpRequestHandler(arrayOf(APITag.CREATE_TRANSACTION, APITag.TRANSACTIONS), IMMUTABLE_PARAMETER, RECEIVER_ID_PARAMETER, AMOUNT_NQT_PARAMETER, FEE_NQT_PARAMETER, FEE_SUGGESTION_TYPE_PARAMETER, MESSAGE_PARAMETER) {

    private val logger = LoggerFactory.getLogger(GenerateDeeplinkQRCode::class.java)

    override suspend fun processRequest(request: HttpServletRequest, resp: HttpServletResponse) {
        try {
            val immutable = Parameters.isTrue(request.getParameter(IMMUTABLE_PARAMETER))

            val receiverId = request.getParameter(RECEIVER_ID_PARAMETER).emptyToNull()

            if (receiverId.isNullOrBlank()) {
                addErrorMessage(resp, MISSING_RECEIVER_ID)
                return
            }

            val amountNQTString = request.getParameter(AMOUNT_NQT_PARAMETER).emptyToNull()
            if (amountNQTString.isNullOrBlank()) {
                addErrorMessage(resp, MISSING_AMOUNT)
                return
            }

            val amountNQT = amountNQTString.toLong()
            if (immutable && (amountNQT < 0 || amountNQT > Constants.MAX_BALANCE_NQT)) {
                addErrorMessage(resp, INCORRECT_AMOUNT)
                return
            }

            val feeNQTString = request.getParameter(FEE_NQT_PARAMETER).emptyToNull()

            var feeNQT: Long? = null

            if (!feeNQTString.isNullOrBlank()) {
                feeNQT = java.lang.Long.parseLong(feeNQTString)

                if (feeNQT <= 0 || feeNQT >= Constants.MAX_BALANCE_NQT) {
                    addErrorMessage(resp, INCORRECT_FEE)
                    return
                }
            }

            var feeSuggestionType: FeeSuggestionType? = null

            if (feeNQT == null) {
                val feeSuggestionTypeString = request.getParameter(FEE_SUGGESTION_TYPE_PARAMETER).emptyToNull()

                if (feeSuggestionTypeString.isNullOrBlank()) {
                    addErrorMessage(resp, FEE_OR_FEE_SUGGESTION_REQUIRED)
                    return
                } else {
                    feeSuggestionType = FeeSuggestionType.getByType(feeSuggestionTypeString)

                    if (feeSuggestionType == null) {
                        addErrorMessage(resp, FEE_SUGGESTION_TYPE_INVALID)
                        return
                    }
                }
            }

            val message = request.getParameter(MESSAGE_PARAMETER).emptyToNull()

            if (!message.isNullOrBlank() && message.length > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
                addErrorMessage(resp, INCORRECT_MESSAGE_LENGTH)
                return
            }

            resp.contentType = "image/jpeg"

            val qrImage = deeplinkQRCodeGenerator.generateRequestBurstDeepLinkQRCode(receiverId, amountNQT, feeSuggestionType, feeNQT, message, immutable)
            ImageIO.write(qrImage, "jpg", resp.outputStream)
            resp.outputStream.close()
        } catch (e: WriterException) {
            logger.safeError(e) { "Could not generate Deeplink QR code" }
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
        } catch (e: IOException) {
            logger.safeError(e) { "Could not generate Deeplink QR code" }
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
        } catch (e: IllegalArgumentException) {
            logger.safeError(e) { "Problem with arguments" }
        }

    }
}
