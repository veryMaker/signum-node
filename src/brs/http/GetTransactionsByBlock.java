package brs.http;

import brs.Block;
import brs.Blockchain;
import brs.BurstException;
import brs.Transaction;
import brs.util.Convert;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.Collection;

import static brs.http.JSONResponses.*;
import static brs.http.common.ResultFields.TRANSACTIONS_RESPONSE;
import static brs.http.common.Parameters.*;
import javax.servlet.http.HttpServletRequest;

final class GetTransactionsByBlock extends APIServlet.JsonRequestHandler {

  private final Blockchain blockchain;

  GetTransactionsByBlock(Blockchain blockchain) {
    super(new APITag[] {APITag.TRANSACTIONS}, BLOCK_PARAMETER, HEIGHT_PARAMETER, TIMESTAMP_PARAMETER);
    this.blockchain = blockchain;
  }

  @Override
  JsonElement processRequest(HttpServletRequest req) throws BurstException {
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

    final Collection<Transaction> transactionsFound = blockchain.getAllTransactionsAtBlock(blockData.getId());

    JsonArray transactions = new JsonArray();
    for (Transaction transaction : transactionsFound) {
      transactions.add(JSONData.transaction(transaction, blockData.getHeight()));
    }

    JsonObject response = new JsonObject();
    response.add(TRANSACTIONS_RESPONSE, transactions);
    return response;
  }
}
