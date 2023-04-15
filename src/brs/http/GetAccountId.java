package brs.http;

import static brs.http.JSONResponses.MISSING_SECRET_PHRASE_OR_PUBLIC_KEY;
import static brs.http.common.Parameters.PUBLIC_KEY_PARAMETER;
import static brs.http.common.Parameters.SECRET_PHRASE_PARAMETER;
import static brs.http.common.ResultFields.ACCOUNT_RESPONSE;
import static brs.http.common.ResultFields.PUBLIC_KEY_RESPONSE;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import brs.crypto.Crypto;
import brs.util.Convert;
import signumj.crypto.SignumCrypto;
import signumj.entity.SignumAddress;

public final class GetAccountId extends APIServlet.JsonRequestHandler {

  static final GetAccountId instance = new GetAccountId();

  public GetAccountId() {
    super(new APITag[] {APITag.ACCOUNTS}, SECRET_PHRASE_PARAMETER, PUBLIC_KEY_PARAMETER);
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    SignumAddress address;
    String secretPhrase = Convert.emptyToNull(req.getParameter(SECRET_PHRASE_PARAMETER));
    String publicKeyString = Convert.emptyToNull(req.getParameter(PUBLIC_KEY_PARAMETER));
    if (secretPhrase != null) {
      byte[] publicKey = Crypto.getPublicKey(secretPhrase);
      address = SignumCrypto.getInstance().getAddressFromPublic(publicKey);
      publicKeyString = Convert.toHexString(publicKey);
    } else if (publicKeyString != null) {
      address = SignumCrypto.getInstance().getAddressFromPublic(Convert.parseHexString(publicKeyString));
    } else {
      return MISSING_SECRET_PHRASE_OR_PUBLIC_KEY;
    }

    JsonObject response = new JsonObject();
    JSONData.putAccount(response, ACCOUNT_RESPONSE, address.getSignedLongId());
    response.addProperty(ACCOUNT_RESPONSE + "RSExtended", address.getExtendedAddress());
    response.addProperty(PUBLIC_KEY_RESPONSE, publicKeyString);

    return response;
  }

  @Override
  boolean requirePost() {
    return true;
  }

}
