package brs.deeplink;

import brs.feesuggestions.FeeSuggestionType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.apache.commons.lang.StringUtils;

public class DeeplinkQRCodeGenerator {

  private final QRCodeWriter qrCodeWriter = new QRCodeWriter();
  private final Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
  private final Optional<BufferedImage> overlay;

  private static final String OVERLAY_LOCATION = "resources/images/burst_overlay_logo.png";


  public DeeplinkQRCodeGenerator() {
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

    overlay = loadImage(OVERLAY_LOCATION);
  }

  public BufferedImage generateRequestBurstDeepLinkQRCode(String receiverId, long amountNQT, FeeSuggestionType feeSuggestionType, Long feeNQT, String message, boolean immutable)
      throws WriterException {
    final StringBuilder deeplinkBuilder = new StringBuilder("burst://requestBurst");

    deeplinkBuilder.append("&receiver=" + receiverId);
    deeplinkBuilder.append("&amountNQT=" + amountNQT);

    if (feeNQT != null) {
      deeplinkBuilder.append("&feeNQT=" + feeNQT);
    } else {
      deeplinkBuilder.append("&feeSuggestionType=" + feeSuggestionType.getType());
    }

    if (!StringUtils.isEmpty(message)) {
      deeplinkBuilder.append("&message=" + message);
    }

    deeplinkBuilder.append("&immutable=" + immutable);

    return generateBurstQRCode(deeplinkBuilder.toString());
  }

  private BufferedImage generateBurstQRCode(String url) throws WriterException {
    final BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 350, 350, hints);
    final BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, new MatrixToImageConfig());

    if (overlay.isPresent()) {
      final BufferedImage overlayImage = overlay.get();

      int deltaHeight = qrImage.getHeight() - overlayImage.getHeight();
      int deltaWidth = qrImage.getWidth() - overlayImage.getWidth();

      BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_RGB);
      Graphics2D g = (Graphics2D) combined.getGraphics();

      g.drawImage(qrImage, 0, 0, null);
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

      g.drawImage(overlayImage, Math.round(deltaWidth / 2), Math.round(deltaHeight / 2), null);

      g.dispose();
      return combined;
    }

    return qrImage;
  }

  private Optional<BufferedImage> loadImage(String imageLocation) {
    try {
      return Optional.of(ImageIO.read(new FileInputStream(imageLocation)));
    } catch (IOException ex) {
      return Optional.empty();
    }
  }
}
