package brs.http;

import brs.Account;
import brs.Block;
import brs.Blockchain;
import brs.BurstException;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.BLOCK_IDS_RESPONSE;
import static brs.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAccountBlockIds extends APIServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  GetAccountBlockIds(ParameterService parameterService, Blockchain blockchain) {
    super(new APITag[] {APITag.ACCOUNTS}, ACCOUNT_PARAMETER, TIMESTAMP_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
    Account account = parameterService.getAccount(req);

    int timestamp = ParameterParser.getTimestamp(req);
    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);

    JsonArray blockIds = new JsonArray();
    CollectionWithIndex<Block> blocks = blockchain.getBlocks(account, timestamp, firstIndex, lastIndex);
    for (Block block : blocks) {
      blockIds.add(block.getStringId());
    }

    JsonObject response = new JsonObject();
    response.add(BLOCK_IDS_RESPONSE, blockIds);
    
    if(blocks.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, blocks.nextIndex());
    }

    return response;
  }

}
