package brs.web.api.http.handler;

import brs.crypto.Crypto;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;

import static brs.web.api.http.common.JSONResponses.MISSING_SIGNATURE_HASH;
import static brs.web.api.http.common.JSONResponses.MISSING_UNSIGNED_BYTES;
import static brs.web.api.http.common.Parameters.*;

public final class CalculateFullHash extends ApiServlet.JsonRequestHandler {

  public CalculateFullHash() {
    super(new LegacyDocTag[]{LegacyDocTag.TRANSACTIONS}, UNSIGNED_TRANSACTION_BYTES_PARAMETER, SIGNATURE_HASH_PARAMETER);
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    String unsignedBytesString = Convert.emptyToNull(req.getParameter(UNSIGNED_TRANSACTION_BYTES_PARAMETER));
    String signatureHashString = Convert.emptyToNull(req.getParameter(SIGNATURE_HASH_PARAMETER));

    if (unsignedBytesString == null) {
      return MISSING_UNSIGNED_BYTES;
    } else if (signatureHashString == null) {
      return MISSING_SIGNATURE_HASH;
    }

    MessageDigest digest = Crypto.sha256();
    digest.update(Convert.parseHexString(unsignedBytesString));
    byte[] fullHash = digest.digest(Convert.parseHexString(signatureHashString));
    JsonObject response = new JsonObject();
    response.addProperty(FULL_HASH_RESPONSE, Convert.toHexString(fullHash));

    return response;

  }

}
