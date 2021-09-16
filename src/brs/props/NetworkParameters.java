package brs.props;

import java.util.Map;

import brs.TransactionType;
import brs.http.APIServlet.HttpRequestHandler;

public interface NetworkParameters {
  
  void initialize();
  
  String getProperty(String key);
  
  Map<Byte, Map<Byte, TransactionType>> getExtraTransactionSubtypes();
  
  Map<String, HttpRequestHandler> getExtraAPIs();

  /**
   * @param height
   * @return the block reward distribution in per thousand for the given account IDs
   */
  Map<Long, Integer> getBlockRewardDistribution(int height);

}
