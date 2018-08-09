package brs.http;

import static brs.http.JSONResponses.FEE_OR_FEE_SUGGESTION_REQUIRED;
import static brs.http.JSONResponses.FEE_SUGGESTION_TYPE_INVALID;
import static brs.http.JSONResponses.INCORRECT_AMOUNT;
import static brs.http.JSONResponses.INCORRECT_FEE;
import static brs.http.JSONResponses.INCORRECT_MESSAGE_LENGTH;
import static brs.http.JSONResponses.MISSING_AMOUNT;
import static brs.http.JSONResponses.MISSING_RECEIVER_ID;
import static brs.http.common.Parameters.AMOUNT_NQT_PARAMETER;
import static brs.http.common.Parameters.FEE_NQT_PARAMETER;
import static brs.http.common.Parameters.FEE_SUGGESTION_TYPE_PARAMETER;
import static brs.http.common.Parameters.IMMUTABLE_PARAMETER;
import static brs.http.common.Parameters.MESSAGE_PARAMETER;
import static brs.http.common.Parameters.RECEIVER_ID_PARAMETER;

import brs.Constants;
import brs.deeplink.DeeplinkQRCodeGenerator;
import brs.feesuggestions.FeeSuggestionType;
import brs.http.APIServlet.PrimitiveRequestHandler;
import brs.http.common.Parameters;
import brs.util.Convert;
import com.google.zxing.WriterException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateDeeplinkQRCode extends PrimitiveRequestHandler {

  private final Logger logger = LoggerFactory.getLogger(GenerateDeeplinkQRCode.class);

  private final DeeplinkQRCodeGenerator deeplinkQRCodeGenerator;

  public GenerateDeeplinkQRCode(DeeplinkQRCodeGenerator deeplinkQRCodeGenerator) {
    this.deeplinkQRCodeGenerator = deeplinkQRCodeGenerator;
  }

  @Override
  public void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    try {
      final boolean immutable = Parameters.isTrue(req.getParameter(IMMUTABLE_PARAMETER));

      final String receiverId = Convert.emptyToNull(req.getParameter(RECEIVER_ID_PARAMETER));

      if (StringUtils.isEmpty(receiverId)) {
        addErrorMessage(resp, MISSING_RECEIVER_ID);
        return;
      }

      final String amountNQTString = Convert.emptyToNull(req.getParameter(AMOUNT_NQT_PARAMETER));
      if (immutable && StringUtils.isEmpty(amountNQTString)) {
        addErrorMessage(resp, MISSING_AMOUNT);
        return;
      }

      final Long amountNQT = Convert.parseLong(amountNQTString);
      if (immutable && (amountNQT < 0 || amountNQT > Constants.MAX_BALANCE_NQT)) {
        addErrorMessage(resp, INCORRECT_AMOUNT);
        return;
      }

      final String feeNQTString = Convert.emptyToNull(req.getParameter(FEE_NQT_PARAMETER));

      Long feeNQT = null;

      if (!StringUtils.isEmpty(feeNQTString)) {
        feeNQT = Convert.parseLong(feeNQTString);

        if (feeNQT != null && (feeNQT <= 0 || feeNQT >= Constants.MAX_BALANCE_NQT)) {
          addErrorMessage(resp, INCORRECT_FEE);
          return;
        }
      }

      FeeSuggestionType feeSuggestionType = null;

      if (feeNQT == null) {
        final String feeSuggestionTypeString = Convert.emptyToNull(req.getParameter(FEE_SUGGESTION_TYPE_PARAMETER));

        if (StringUtils.isEmpty(feeSuggestionTypeString)) {
          addErrorMessage(resp, FEE_OR_FEE_SUGGESTION_REQUIRED);
          return;
        } else {
          feeSuggestionType = FeeSuggestionType.getByType(feeSuggestionTypeString);

          if (feeSuggestionType == null) {
            addErrorMessage(resp, FEE_SUGGESTION_TYPE_INVALID);
            return;
          }
        }
      }

      final String message = Convert.emptyToNull(req.getParameter(MESSAGE_PARAMETER));

      if (!StringUtils.isEmpty(message) && message.length() > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
        addErrorMessage(resp, INCORRECT_MESSAGE_LENGTH);
        return;
      }

      resp.setContentType("image/jpeg");

      final BufferedImage qrImage = deeplinkQRCodeGenerator.generateRequestBurstDeepLinkQRCode(receiverId, amountNQT, feeSuggestionType, feeNQT, message, immutable);
      ImageIO.write(qrImage, "jpg", resp.getOutputStream());
      resp.getOutputStream().close();
    } catch (WriterException | IOException e) {
      logger.error("Could not generate Deeplink QR code", e);
      resp.setStatus(500);
    } catch (IllegalArgumentException e) {
      logger.error("Problem with arguments", e);
      resp.setStatus(500);
    }
  }
}
