package brs.peer;

import brs.Block;
import brs.Blockchain;
import brs.Constants;
import brs.util.Convert;
import brs.util.JSON;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class GetNextBlocks implements PeerServlet.PeerRequestHandler {

  private final Blockchain blockchain;
  private final int maxLength = 1048576;
  private final int maxBlocks = 1440 / 2; // maxRollback must be at least 1440 and we are using half of that

  GetNextBlocks(Blockchain blockchain) {
    this.blockchain = blockchain;
  }


  @Override
  public JsonElement processRequest(JsonObject request, Peer peer) {

    JsonObject response = new JsonObject();

    List<Block> nextBlocks = new ArrayList<>();
    int totalLength = 0;
    long blockId = Convert.parseUnsignedLong(JSON.getAsString(request.get("blockId")));
    
    while(totalLength < maxLength && nextBlocks.size() < maxBlocks) {
      Collection<? extends Block> blocks = blockchain.getBlocksAfter(blockId, 100);
      if (blocks.size() == 0)
    	break;
      
      for (Block block : blocks) {
        int length = Constants.BLOCK_HEADER_LENGTH + block.getPayloadLength();
        totalLength += length;
        nextBlocks.add(block);
        if (totalLength >= maxLength || nextBlocks.size() >= maxBlocks) {
          break;
        }
        blockId = block.getId();
      }      
    }

    JsonArray nextBlocksArray = new JsonArray();
    for (Block nextBlock : nextBlocks) {
      nextBlocksArray.add(nextBlock.getJsonObject());
    }
    response.add("nextBlocks", nextBlocksArray);

    return response;
  }

}
