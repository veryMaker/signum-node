package brs.http;

import brs.BlockchainProcessor;
import brs.TransactionProcessor;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

public final class GetMyPeerInfo extends APIServlet.APIRequestHandler {

  private final BlockchainProcessor blockchainProcessor;
  private final TransactionProcessor transactionProcessor;

  public GetMyPeerInfo(BlockchainProcessor blockchainProcessor, TransactionProcessor transactionProcessor) {
    super(new APITag[]{APITag.PEER_INFO});
    this.blockchainProcessor = blockchainProcessor;
    this.transactionProcessor = transactionProcessor;
  }

  @Override
  JSONStreamAware processRequest(HttpServletRequest req) {

    JSONObject response = new JSONObject();
    response.put("walletTTSD", blockchainProcessor.getWalletTTSD());
    response.put("utsInStore", transactionProcessor.getAmountUnconfirmedTransactions());
    return response;
  }

}
