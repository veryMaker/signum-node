package brs.api.http

import brs.api.http.APIServlet.HttpRequestHandler
import brs.api.http.JSONResponses.MISSING_DOMAIN
import brs.api.http.JSONResponses.PAYLOAD_WITHOUT_ACTION
import brs.api.http.JSONResponses.incorrect
import brs.api.http.common.Parameters.ACTION_PARAMETER
import brs.api.http.common.Parameters.DOMAIN_PARAMETER
import brs.api.http.common.Parameters.PAYLOAD_PARAMETER
import brs.services.DeeplinkGeneratorService
import brs.util.convert.emptyToNull
import com.google.zxing.WriterException
import org.eclipse.jetty.http.HttpStatus
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.imageio.ImageIO
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

internal class GenerateDeeplinkQR(private val deeplinkGeneratorService: DeeplinkGeneratorService) :
    HttpRequestHandler
        (arrayOf(APITag.UTILS), DOMAIN_PARAMETER, ACTION_PARAMETER, PAYLOAD_PARAMETER) {
    private val logger = LoggerFactory.getLogger(GenerateDeeplinkQR::class.java)

    override fun processRequest(request: HttpServletRequest, resp: HttpServletResponse) {
        try {
            val domain = request.getParameter(DOMAIN_PARAMETER).emptyToNull()
            if (domain.isNullOrEmpty()) {
                addErrorMessage(resp, MISSING_DOMAIN)
                return
            }

            val action = request.getParameter(ACTION_PARAMETER).emptyToNull()
            val payload = request.getParameter(PAYLOAD_PARAMETER).emptyToNull()

            if (action.isNullOrEmpty() && !payload.isNullOrEmpty()) {
                addErrorMessage(resp, PAYLOAD_WITHOUT_ACTION)
                return
            }

            try {
                val qrImage = deeplinkGeneratorService.generateDeepLinkQrCode(
                    domain,
                    action,
                    payload
                )
                resp.contentType = "image/jpeg"
                ImageIO.write(qrImage, "jpg", resp.outputStream)
                resp.outputStream.close()
            } catch (e: IllegalArgumentException) {
                logger.error("Problem with arguments", e)
                addErrorMessage(resp, incorrect("arguments", e.message))
            }

        } catch (e: WriterException) {
            logger.error("Could not generate Deeplink QR code", e)
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
        } catch (e: IOException) {
            logger.error("Could not generate Deeplink QR code", e)
            resp.status = HttpStatus.INTERNAL_SERVER_ERROR_500
        }

    }
}
