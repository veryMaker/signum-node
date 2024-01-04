package brs.web.api.http.handler;

import brs.Account;
import brs.Attachment;
import brs.Burst;
import brs.BurstException;
import brs.fluxcapacitor.FluxValues;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static brs.web.api.http.common.Parameters.*;

public abstract class CreateTransaction extends ApiServlet.JsonRequestHandler {

  private static final String[] commonParameters = new String[] {
    SECRET_PHRASE_PARAMETER, PUBLIC_KEY_PARAMETER, FEE_NQT_PARAMETER,
    DEADLINE_PARAMETER, REFERENCED_TRANSACTION_FULL_HASH_PARAMETER, BROADCAST_PARAMETER,
    MESSAGE_PARAMETER, MESSAGE_IS_TEXT_PARAMETER,
    MESSAGE_TO_ENCRYPT_PARAMETER, MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, ENCRYPTED_MESSAGE_DATA_PARAMETER, ENCRYPTED_MESSAGE_NONCE_PARAMETER,
    MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER, MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER, ENCRYPT_TO_SELF_MESSAGE_DATA, ENCRYPT_TO_SELF_MESSAGE_NONCE,
    RECIPIENT_PUBLIC_KEY_PARAMETER};

  private final APITransactionManager apiTransactionManager;

  private static String[] addCommonParameters(String[] parameters) {
    String[] result = Arrays.copyOf(parameters, parameters.length + commonParameters.length);
    System.arraycopy(commonParameters, 0, result, parameters.length, commonParameters.length);
    return result;
  }

  protected CreateTransaction(LegacyDocTag[] legacyDocTags, APITransactionManager apiTransactionManager, boolean replaceParameters, String... parameters) {
    super(legacyDocTags, replaceParameters ? parameters : addCommonParameters(parameters));
    this.apiTransactionManager = apiTransactionManager;
  }

  protected CreateTransaction(LegacyDocTag[] legacyDocTags, APITransactionManager apiTransactionManager, String... parameters) {
    super(legacyDocTags, addCommonParameters(parameters));
    this.apiTransactionManager = apiTransactionManager;
  }

  public final JsonElement createTransaction(HttpServletRequest req, Account senderAccount, Attachment attachment)
    throws BurstException {
    return createTransaction(req, senderAccount, null, 0, attachment);
  }

  public final JsonElement createTransaction(HttpServletRequest req, Account senderAccount, Long recipientId, long amountNQT)
    throws BurstException {
    return createTransaction(req, senderAccount, recipientId, amountNQT, Attachment.ORDINARY_PAYMENT);
  }

  public final JsonElement createTransaction(HttpServletRequest req, Account senderAccount, Long recipientId, long amountNQT, Attachment attachment) throws BurstException {
    return apiTransactionManager.createTransaction(req, senderAccount, recipientId, amountNQT, attachment, minimumFeeNQT());
  }

  final boolean requirePost() {
    return true;
  }

  private long minimumFeeNQT() {
    return Burst.getFluxCapacitor().getValue(FluxValues.FEE_QUANT);
  }

}
