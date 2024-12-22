package brs.web.api.http.handler;

import brs.*;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.util.Convert;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.*;
import static brs.web.api.http.common.Parameters.PURCHASE_PARAMETER;
import static brs.web.api.http.common.Parameters.REFUND_NQT_PARAMETER;

public final class DGSRefund extends CreateTransaction {

  private final ParameterService parameterService;
  private final AccountService accountService;
  private final Blockchain blockchain;

  public DGSRefund(ParameterService parameterService, Blockchain blockchain, AccountService accountService, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[] {LegacyDocTag.DGS, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, PURCHASE_PARAMETER, REFUND_NQT_PARAMETER);
    this.parameterService = parameterService;
    this.accountService = accountService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    Account sellerAccount = parameterService.getSenderAccount(req);
    DigitalGoodsStore.Purchase purchase = parameterService.getPurchase(req);

    if (sellerAccount.getId() != purchase.getSellerId()) {
      return INCORRECT_PURCHASE;
    }
    if (purchase.getRefundNote() != null) {
      return DUPLICATE_REFUND;
    }
    if (purchase.getEncryptedGoods() == null) {
      return GOODS_NOT_DELIVERED;
    }

    String refundValueNQT = Convert.emptyToNull(req.getParameter(REFUND_NQT_PARAMETER));
    long refundNQT = 0;
    try {
      if (refundValueNQT != null) {
        refundNQT = Long.parseLong(refundValueNQT);
      }
    } catch (RuntimeException e) {
      return INCORRECT_DGS_REFUND;
    }
    if (refundNQT < 0 || refundNQT > Constants.MAX_BALANCE_NQT) {
      return INCORRECT_DGS_REFUND;
    }

    Account buyerAccount = accountService.getAccount(purchase.getBuyerId());

    Attachment attachment = new Attachment.DigitalGoodsRefund(purchase.getId(), refundNQT, blockchain.getHeight());
    return createTransaction(req, sellerAccount, buyerAccount.getId(), 0, attachment);

  }

}
