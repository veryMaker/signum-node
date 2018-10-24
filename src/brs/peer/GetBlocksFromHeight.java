package brs.peer;

import brs.Block;
import brs.Blockchain;
import brs.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.util.ArrayList;
import java.util.List;

final class GetBlocksFromHeight extends PeerServlet.PeerRequestHandler {

  private final Blockchain blockchain;

  GetBlocksFromHeight(Blockchain blockchain) {
    this.blockchain = blockchain;
  }


  @Override
  JSONStreamAware processRequest(JSONObject request, Peer peer) {
    JSONObject response = new JSONObject();
    List<Block> nextBlocks = new ArrayList<>();
    int blockHeight = Convert.parseInteger(request.get("height").toString());
    int numBlocks = 100;

    try {
      numBlocks = Convert.parseInteger(request.get("numBlocks").toString());
    } catch (Exception e) {}

    //small failsafe
    if(numBlocks < 1 || numBlocks > 1400) {
    	numBlocks = 100;
    }
    if(blockHeight < 0) {
    	blockHeight = 0;
    }
    	    
    long blockId =  blockchain.getBlockIdAtHeight(blockHeight);
    List<? extends Block> blocks = blockchain.getBlocksAfter(blockId, numBlocks);
    for (Block block : blocks) {
      nextBlocks.add(block);
    }

    JSONArray nextBlocksArray = new JSONArray();
    for (Block nextBlock : nextBlocks) {
      nextBlocksArray.add(nextBlock.getJSONObject());
    }
    response.put("nextBlocks", nextBlocksArray);
    return response;
  }

}
