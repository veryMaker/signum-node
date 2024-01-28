package brs.web.api.http.handler;

import brs.Account;
import brs.Block;
import brs.Blockchain;
import brs.SignumException;
import brs.services.ParameterService;
import brs.util.CollectionWithIndex;

import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.ParameterParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.BLOCK_IDS_RESPONSE;
import static brs.web.api.http.common.ResultFields.NEXT_INDEX_RESPONSE;

public final class GetAccountBlockIds extends ApiServlet.JsonRequestHandler {

  private final ParameterService parameterService;
  private final Blockchain blockchain;

  public GetAccountBlockIds(ParameterService parameterService, Blockchain blockchain) {
    super(new LegacyDocTag[] {LegacyDocTag.ACCOUNTS}, ACCOUNT_PARAMETER, TIMESTAMP_PARAMETER, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER);
    this.parameterService = parameterService;
    this.blockchain = blockchain;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) throws SignumException {
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
