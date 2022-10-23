package brs.http;

import brs.Block;
import brs.Blockchain;
import brs.http.common.Parameters;
import brs.services.BlockService;
import brs.util.CollectionWithIndex;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.*;
import static brs.http.common.ResultFields.NEXT_INDEX_RESPONSE;

final class GetBlocks extends APIServlet.JsonRequestHandler {

  private final Blockchain blockchain;
  private final BlockService blockService;

  GetBlocks(Blockchain blockchain, BlockService blockService) {
    super(new APITag[] {APITag.BLOCKS}, FIRST_INDEX_PARAMETER, LAST_INDEX_PARAMETER, INCLUDE_TRANSACTIONS_PARAMETER);
    this.blockchain = blockchain;
    this.blockService = blockService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {

    int firstIndex = ParameterParser.getFirstIndex(req);
    int lastIndex = ParameterParser.getLastIndex(req);
    if (lastIndex < 0 || lastIndex - firstIndex > 99) {
      lastIndex = firstIndex + 99;
    }

    boolean includeTransactions = Parameters.isTrue(req.getParameter(Parameters.INCLUDE_TRANSACTIONS_PARAMETER));

    JsonArray blocksArray = new JsonArray();
    CollectionWithIndex<Block> blocks = new CollectionWithIndex<Block>(blockchain.getBlocks(firstIndex, lastIndex), firstIndex, lastIndex);
    for (Block block : blocks) {
      blocksArray.add(JSONData.block(block, includeTransactions, blockchain.getHeight(), blockService.getBlockReward(block), blockService.getScoopNum(block)));
    }

    JsonObject response = new JsonObject();
    response.add("blocks", blocksArray);
    
    if(blocks.hasNextIndex()) {
      response.addProperty(NEXT_INDEX_RESPONSE, blocks.nextIndex());
    }

    return response;
  }
}
