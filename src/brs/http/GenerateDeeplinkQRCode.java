package brs.http;

import static brs.http.common.Parameters.AMOUNT_NQT_PARAMETER;
import static brs.http.common.Parameters.FEE_NQT_PARAMETER;
import static brs.http.common.Parameters.MESSAGE_PARAMETER;
import static brs.http.common.Parameters.RECEIVER_ID_PARAMETER;

import brs.Constants;
import brs.deeplink.DeeplinkQRCodeGenerator;
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
      final Long amount = Convert.parseUnsignedLong(Convert.emptyToNull(req.getParameter(AMOUNT_NQT_PARAMETER)));
      final Long transactionCost = Convert.parseUnsignedLong(Convert.emptyToNull(req.getParameter(FEE_NQT_PARAMETER)));
      final String message = Convert.emptyToNull(req.getParameter(MESSAGE_PARAMETER));

      if(StringUtils.isEmpty(receiverId) || amount == null || transactionCost == null
          || amount < 0 || amount > Constants.MAX_BALANCE_NQT || transactionCost <= 0 || transactionCost >= Constants.MAX_BALANCE_NQT) {
        try (Writer writer = resp.getWriter()) {
          resp.setContentType("text/plain; charset=UTF-8");
          resp.setStatus(500);
          writer.write("Missing arguments, required: receiverId, amountNQT, feeNQT; AmountNQT, feeNQT should be positive numbers, less than the max balance");
          return;
        }
      }

      resp.setContentType("image/jpeg");

      final BufferedImage qrImage = deeplinkQRCodeGenerator.generateRequestBurstDeepLinkQRCode(receiverId, amount, transactionCost, message);
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
