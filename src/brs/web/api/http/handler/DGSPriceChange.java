package brs.web.api.http.handler;

import brs.*;
import brs.services.ParameterService;
import brs.web.api.http.common.APITransactionManager;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.UNKNOWN_GOODS;
import static brs.web.api.http.common.Parameters.GOODS_PARAMETER;
import static brs.web.api.http.common.Parameters.PRICE_NQT_PARAMETER;

public final class DGSPriceChange extends CreateTransaction {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  public DGSPriceChange(ParameterService parameterService, Blockchain blockchain, APITransactionManager apiTransactionManager) {
    super(new LegacyDocTag[]{LegacyDocTag.DGS, LegacyDocTag.CREATE_TRANSACTION}, apiTransactionManager, GOODS_PARAMETER, PRICE_NQT_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
    Account account = parameterService.getSenderAccount(req);
    DigitalGoodsStore.Goods goods = parameterService.getGoods(req);
    long priceNQT = ParameterParser.getPriceNQT(req);
    if (goods.isDelisted() || goods.getSellerId() != account.getId()) {
      return UNKNOWN_GOODS;
    }
    Attachment attachment = new Attachment.DigitalGoodsPriceChange(goods.getId(), priceNQT, blockchain.getHeight());
    return createTransaction(req, account, attachment);
  }

}
