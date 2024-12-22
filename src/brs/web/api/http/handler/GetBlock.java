package brs.web.api.http.handler;

import brs.Block;
import brs.Blockchain;
import brs.web.api.http.common.LegacyDocTag;
import brs.web.api.http.common.Parameters;
import brs.services.BlockService;
import brs.util.Convert;
import brs.web.api.http.ApiServlet;
import brs.web.api.http.common.JSONData;
import com.google.gson.JsonElement;

import javax.servlet.http.HttpServletRequest;

import static brs.web.api.http.common.JSONResponses.*;
import static brs.web.api.http.common.Parameters.*;

public final class GetBlock extends ApiServlet.JsonRequestHandler {

  private final Blockchain blockchain;

  private final BlockService blockService;

  public GetBlock(Blockchain blockchain, BlockService blockService) {
    super(new LegacyDocTag[] {LegacyDocTag.BLOCKS}, BLOCK_PARAMETER, HEIGHT_PARAMETER, TIMESTAMP_PARAMETER, INCLUDE_TRANSACTIONS_PARAMETER);
    this.blockchain = blockchain;
    this.blockService = blockService;
  }

  @Override
  protected
  JsonElement processRequest(HttpServletRequest req) {
    String blockValue = Convert.emptyToNull(req.getParameter(BLOCK_PARAMETER));
    String heightValue = Convert.emptyToNull(req.getParameter(HEIGHT_PARAMETER));
    String timestampValue = Convert.emptyToNull(req.getParameter(TIMESTAMP_PARAMETER));

    Block blockData;
    if (blockValue != null) {
      try {
        blockData = blockchain.getBlock(Convert.parseUnsignedLong(blockValue));
      } catch (RuntimeException e) {
        return INCORRECT_BLOCK;
      }
    } else if (heightValue != null) {
      try {
        int height = Integer.parseInt(heightValue);
        if (height < 0 || height > blockchain.getHeight()) {
          return INCORRECT_HEIGHT;
        }
        blockData = blockchain.getBlockAtHeight(height);
      } catch (RuntimeException e) {
        return INCORRECT_HEIGHT;
      }
    } else if (timestampValue != null) {
      try {
        int timestamp = Integer.parseInt(timestampValue);
        if (timestamp < 0) {
          return INCORRECT_TIMESTAMP;
        }
        blockData = blockchain.getLastBlock(timestamp);
      } catch (RuntimeException e) {
        return INCORRECT_TIMESTAMP;
      }
    } else {
      blockData = blockchain.getLastBlock();
    }

    if (blockData == null) {
      return UNKNOWN_BLOCK;
    }

    boolean includeTransactions = Parameters.isTrue(req.getParameter(INCLUDE_TRANSACTIONS_PARAMETER));

    return JSONData.block(blockData, includeTransactions, blockchain.getHeight(), blockService.getBlockReward(blockData), blockService.getScoopNum(blockData));
  }
}
