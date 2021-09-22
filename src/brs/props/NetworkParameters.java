package brs.props;

import java.util.Map;

import brs.TransactionType;
import brs.http.APITransactionManager;
import brs.http.APIServlet.HttpRequestHandler;
import brs.services.ParameterService;

public interface NetworkParameters {
  
  void initialize(ParameterService parameterService, APITransactionManager apiTransactionManager);
  
  String getProperty(String key);
  
  /**
   * This method can either add or remove transaction types/subtyes on the given map.
   */
  void adjustTransactionTypes(Map<Byte, Map<Byte, TransactionType>> types);
  
  /**
   * This method can either add or remove API requests from the given map.
   */
  void adjustAPIs(Map<String, HttpRequestHandler> map);

  /**
   * @param height
   * @return the block reward distribution in per thousand for the given account IDs
   */
  Map<Long, Integer> getBlockRewardDistribution(int height);

}
