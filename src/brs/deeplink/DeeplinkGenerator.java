package brs.deeplink;

import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class DeeplinkGenerator {

    private static final String Version = "v1";
    private static final Charset DefaultCharset = StandardCharsets.UTF_8;
    private static final Integer MaxPayloadLength = 2048;

    private final String[] validDomains = new String[]{"payment", "message", "contract", "asset", "market", "generic"};

    private final Map<EncodeHintType, ErrorCorrectionLevel> hints = new EnumMap<>(EncodeHintType.class);

    public DeeplinkGenerator() {
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
    }

    public String generateDeepLink(String domain, String action, String base64Payload) throws UnsupportedEncodingException, IllegalArgumentException {

        Arrays.stream(validDomains)
                .filter(d -> d.equals(domain))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid domain:" + domain));

        final StringBuilder deeplinkBuilder = new StringBuilder("burst.");
        deeplinkBuilder.append(domain);
        deeplinkBuilder.append("://");
        deeplinkBuilder.append(DeeplinkGenerator.Version);
        if (action != null) {
            deeplinkBuilder.append("?action=");
            deeplinkBuilder.append(action);
            if (base64Payload != null) {
                deeplinkBuilder.append("&payload=");
                String encodedPayload = URLEncoder.encode(base64Payload, DefaultCharset.toString());
                if(encodedPayload.length() > MaxPayloadLength){
                  throw new IllegalArgumentException("Maximum Payload Length (" + MaxPayloadLength + ") exceeded");
                }
                deeplinkBuilder.append(encodedPayload);
            }
        }
        return deeplinkBuilder.toString();
    }

//  public BufferedImage generateRequestBurstDeepLinkQRCode(String receiverId, long amountNQT, FeeSuggestionType feeSuggestionType, Long feeNQT, String message, boolean immutable)
//      throws WriterException {
//    final StringBuilder deeplinkBuilder = new StringBuilder("burst://requestBurst");
//
//    deeplinkBuilder.append("&receiver=").append(receiverId);
//    deeplinkBuilder.append("&amountNQT=").append(amountNQT);
//
//    if (feeNQT != null) {
//      deeplinkBuilder.append("&feeNQT=").append(feeNQT);
//    } else {
//      deeplinkBuilder.append("&feeSuggestionType=").append(feeSuggestionType.getType());
//    }
//
//    if (!StringUtils.isEmpty(message)) {
//      deeplinkBuilder.append("&message=").append(message);
//    }
//
//    deeplinkBuilder.append("&immutable=").append(immutable);
//
//    return generateBurstQRCode(deeplinkBuilder.toString());
//  }
//
//  private BufferedImage generateBurstQRCode(String url) throws WriterException {
//    return MatrixToImageWriter.toBufferedImage(qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 350, 350, hints), new MatrixToImageConfig());
//  }
}
