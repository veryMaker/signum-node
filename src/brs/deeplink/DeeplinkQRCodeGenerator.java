package brs.deeplink;

import brs.feesuggestions.FeeSuggestionType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;
import org.apache.commons.lang.StringUtils;

public class DeeplinkQRCodeGenerator {

  private final QRCodeWriter qrCodeWriter = new QRCodeWriter();

  public BufferedImage generateRequestBurstDeepLinkQRCode(String receiverId, long amountNQT, FeeSuggestionType feeSuggestionType, Long feeNQT, String message) throws WriterException {
    final StringBuilder deeplinkBuilder = new StringBuilder("burst://requestBurst");

    deeplinkBuilder.append("&receiver=" + receiverId);
    deeplinkBuilder.append("&amountNQT=" + amountNQT);

    if(feeNQT != null) {
      deeplinkBuilder.append("&feeNQT=" + feeNQT);
    } else {
      deeplinkBuilder.append("&feeSuggestionType=" + feeSuggestionType.getType());
    }

    if(! StringUtils.isEmpty(message)) {
      deeplinkBuilder.append("&message=" + message);
    }

    final BitMatrix bitMatrix = qrCodeWriter.encode(deeplinkBuilder.toString(), BarcodeFormat.QR_CODE, 350, 350);
    return MatrixToImageWriter.toBufferedImage(bitMatrix, new MatrixToImageConfig());
  }

}
