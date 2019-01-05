package brs.http;

import brs.Block;
import brs.Blockchain;
import brs.BurstException;
import brs.EconomicClustering;
import brs.services.TimeService;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static brs.http.common.Parameters.TIMESTAMP_PARAMETER;
import static brs.http.common.ResultFields.*;

final class GetECBlock extends APIServlet.APIRequestHandler {

  private final Blockchain blockchain;
  private final TimeService timeService;
  private final EconomicClustering economicClustering;

  GetECBlock(Blockchain blockchain, TimeService timeService, EconomicClustering economicClustering) {
    super(new APITag[] {APITag.BLOCKS}, TIMESTAMP_PARAMETER);
    this.blockchain = blockchain;
    this.timeService = timeService;
    this.economicClustering = economicClustering;
  }

  @Override
  JSONStreamAware processRequest(HttpServletRequest req) throws BurstException {
    int timestamp = ParameterParser.getTimestamp(req);
    if (timestamp == 0) {
      timestamp = timeService.getEpochTime();
    }
    if (timestamp < blockchain.getLastBlock().getTimestamp() - 15) {
      return JSONResponses.INCORRECT_TIMESTAMP;
    }
    Block ecBlock = economicClustering.getECBlock(timestamp);
    JSONObject response = new JSONObject();
    response.put(EC_BLOCK_ID_RESPONSE, ecBlock.getStringId());
    response.put(EC_BLOCK_HEIGHT_RESPONSE, ecBlock.getHeight());
    response.put(TIMESTAMP_RESPONSE, timestamp);
    return response;
  }

}
