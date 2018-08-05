package brs.http;

import static brs.http.common.Parameters.AMOUNT_NQT_PARAMETER;
import static brs.http.common.Parameters.FEE_NQT_PARAMETER;
import static brs.http.common.Parameters.FEE_SUGGESTION_TYPE_PARAMETER;
import static brs.http.common.Parameters.MESSAGE_PARAMETER;
import static brs.http.common.Parameters.RECEIVER_ID_PARAMETER;

import brs.Constants;
import brs.deeplink.DeeplinkQRCodeGenerator;
import brs.feesuggestions.FeeSuggestionType;
import brs.http.APIServlet.PrimitiveRequestHandler;
import brs.util.Convert;
import com.google.zxing.WriterException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateDeeplinkQRCode implements PrimitiveRequestHandler {

  private final Logger logger = LoggerFactory.getLogger(GenerateDeeplinkQRCode.class);

  private final DeeplinkQRCodeGenerator deeplinkQRCodeGenerator;

  public GenerateDeeplinkQRCode(DeeplinkQRCodeGenerator deeplinkQRCodeGenerator) {
    this.deeplinkQRCodeGenerator = deeplinkQRCodeGenerator;
  }

  @Override
  public void processRequest(HttpServletRequest req, HttpServletResponse resp) {
    try {
      final String receiverId = Convert.emptyToNull(req.getParameter(RECEIVER_ID_PARAMETER));

      if(StringUtils.isEmpty(receiverId)) {
        addErrorMessage(resp, "Missing argument receiverId");
        return;
      }

      final String amountNQTString = Convert.emptyToNull(req.getParameter(AMOUNT_NQT_PARAMETER));
      if(StringUtils.isEmpty(amountNQTString)) {
        addErrorMessage(resp, "Missing argument amountNQT");
        return;
      }

      final Long amountNQT = Convert.parseLong(amountNQTString);
      if(amountNQT < 0 || amountNQT > Constants.MAX_BALANCE_NQT) {
        addErrorMessage(resp, "amountNQT should be a positive number, less than the max balance");
        return;
      }

      final String feeNQTString = Convert.emptyToNull(req.getParameter(FEE_NQT_PARAMETER));

      Long feeNQT = null;

      if(! StringUtils.isEmpty(feeNQTString)) {
        feeNQT = Convert.parseLong(feeNQTString);

        if (feeNQT != null && (feeNQT <= 0 || feeNQT >= Constants.MAX_BALANCE_NQT)) {
          addErrorMessage(resp, "feeNQT should be a positive number, less than the max balance");
          return;
        }
      }

      FeeSuggestionType feeSuggestionType = null;

      if(feeNQT == null) {
        final String feeSuggestionTypeString = Convert.emptyToNull(req.getParameter(FEE_SUGGESTION_TYPE_PARAMETER));

        if(StringUtils.isEmpty(feeSuggestionTypeString)) {
          addErrorMessage(resp, "Either feeNQT or feeSuggestionType is a required parameter");
          return;
        } else {
          feeSuggestionType = FeeSuggestionType.getByType(feeSuggestionTypeString);

          if(feeSuggestionType == null) {
            addErrorMessage(resp, "feeSuggestionType is not valid");
            return;
          }
        }
      }

      final String message = Convert.emptyToNull(req.getParameter(MESSAGE_PARAMETER));

      if(! StringUtils.isEmpty(message) && message.length() > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
        addErrorMessage(resp, "Message can have a max length of " + Constants.MAX_ARBITRARY_MESSAGE_LENGTH);
        return;
      }

      resp.setContentType("image/jpeg");

      final BufferedImage qrImage = deeplinkQRCodeGenerator.generateRequestBurstDeepLinkQRCode(receiverId, amountNQT, feeSuggestionType, feeNQT, message);
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

  public void addErrorMessage(HttpServletResponse resp, String msg) throws IOException {
    try (Writer writer = resp.getWriter()) {
      resp.setContentType("text/plain; charset=UTF-8");
      resp.setStatus(500);
      writer.write(msg);
    }
  }
}
