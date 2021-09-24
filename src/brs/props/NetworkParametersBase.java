package brs.props;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import brs.Transaction;
import brs.TransactionType;
import brs.fluxcapacitor.FluxValue;
import brs.fluxcapacitor.FluxValue.ValueChange;
import brs.fluxcapacitor.HistoricalMoments;
import brs.http.APIServlet.HttpRequestHandler;
import brs.http.APITransactionManager;
import brs.services.ParameterService;

public class NetworkParametersBase implements NetworkParameters {
  
  private final Properties properties = new Properties();
  protected ParameterService parameterService;
  protected APITransactionManager apiTransactionManager;
  
  @Override
  public void initialize(ParameterService parameterService, APITransactionManager apiTransactionManager) {
    this.parameterService = parameterService;
    this.apiTransactionManager = apiTransactionManager;
  }
  
  protected <T> void setProperty(Prop<T> prop, String value) {
    properties.setProperty(prop.getName(), value);
  }
  
  protected <T> void setFluxValue(FluxValue<T> fluxValue, HistoricalMoments moment, T value){
    ValueChange<T> valueChange = new FluxValue.ValueChange<T>(moment, value);
    List<ValueChange<T>> valueChages = new ArrayList<>();
    valueChages.add(valueChange);
    fluxValue.updateValueChanges(valueChages);
  }

  @Override
  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  @Override
  public Map<Long, Integer> getBlockRewardDistribution(int height) {
    return null;
  }

  @Override
  public void adjustTransactionTypes(Map<TransactionType.Type, Map<Byte, TransactionType>> types) {
  }

  @Override
  public void adjustAPIs(Map<String, HttpRequestHandler> map) {
  }

  @Override
  public void unconfirmedTransactionAdded(Transaction transaction) {
  }

  @Override
  public void unconfirmedTransactionRemoved(Transaction transaction) {
  }
  
}
