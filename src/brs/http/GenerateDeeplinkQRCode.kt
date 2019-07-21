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
import brs.util.Convert
import brs.util.StringUtils
import com.google.zxing.WriterException
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.awt.image.BufferedImage
import java.io.IOException


import brs.http.common.Parameters.*

internal class GenerateDeeplinkQRCode(private val deeplinkQRCodeGenerator: DeeplinkQRCodeGenerator) : HttpRequestHandler(arrayOf(APITag.CREATE_TRANSACTION, APITag.TRANSACTIONS), IMMUTABLE_PARAMETER, RECEIVER_ID_PARAMETER, AMOUNT_NQT_PARAMETER, FEE_NQT_PARAMETER, FEE_SUGGESTION_TYPE_PARAMETER, MESSAGE_PARAMETER) {

    private val logger = LoggerFactory.getLogger(GenerateDeeplinkQRCode::class.java)

    public override fun processRequest(req: HttpServletRequest, resp: HttpServletResponse) {
        try {
            val immutable = Parameters.isTrue(req.getParameter(IMMUTABLE_PARAMETER))

            val receiverId = Convert.emptyToNull(req.getParameter(RECEIVER_ID_PARAMETER))

            if (StringUtils.isEmpty(receiverId)) {
                addErrorMessage(resp, MISSING_RECEIVER_ID)
                return
            }

            val amountNQTString = Convert.emptyToNull(req.getParameter(AMOUNT_NQT_PARAMETER))
            if (immutable && StringUtils.isEmpty(amountNQTString)) {
                addErrorMessage(resp, MISSING_AMOUNT)
                return
            }

            val amountNQT = java.lang.Long.parseLong(amountNQTString!!)
            if (immutable && (amountNQT < 0 || amountNQT > Constants.MAX_BALANCE_NQT)) {
                addErrorMessage(resp, INCORRECT_AMOUNT)
                return
            }

            val feeNQTString = Convert.emptyToNull(req.getParameter(FEE_NQT_PARAMETER))

            var feeNQT: Long? = null

            if (!StringUtils.isEmpty(feeNQTString)) {
                feeNQT = java.lang.Long.parseLong(feeNQTString!!)

                if (feeNQT <= 0 || feeNQT >= Constants.MAX_BALANCE_NQT) {
                    addErrorMessage(resp, INCORRECT_FEE)
                    return
                }
            }

            var feeSuggestionType: FeeSuggestionType? = null

            if (feeNQT == null) {
                val feeSuggestionTypeString = Convert.emptyToNull(req.getParameter(FEE_SUGGESTION_TYPE_PARAMETER))

                if (StringUtils.isEmpty(feeSuggestionTypeString)) {
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

            val message = Convert.emptyToNull(req.getParameter(MESSAGE_PARAMETER))

            if (!StringUtils.isEmpty(message) && message!!.length > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
                addErrorMessage(resp, INCORRECT_MESSAGE_LENGTH)
                return
            }

            resp.contentType = "image/jpeg"

            val qrImage = deeplinkQRCodeGenerator.generateRequestBurstDeepLinkQRCode(receiverId, amountNQT, feeSuggestionType, feeNQT, message, immutable)
            ImageIO.write(qrImage, "jpg", resp.outputStream)
            resp.outputStream.close()
        } catch (e: WriterException) {
            logger.error("Could not generate Deeplink QR code", e)
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
        } catch (e: IOException) {
            logger.error("Could not generate Deeplink QR code", e)
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
        } catch (e: IllegalArgumentException) {
            logger.error("Problem with arguments", e)
        }

    }
}
