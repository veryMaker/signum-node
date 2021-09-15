package brs.props;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import brs.TransactionType;
import brs.fluxcapacitor.FluxValue;
import brs.fluxcapacitor.HistoricalMoments;
import brs.fluxcapacitor.FluxValue.ValueChange;
import brs.http.APIServlet.HttpRequestHandler;

public class NetworkParametersBase implements NetworkParameters {
  
  private final Properties properties = new Properties();
  
  @Override
  public void initialize() {
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
  public Map<Byte, Map<Byte, TransactionType>> getExtraTransactionSubtypes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, HttpRequestHandler> getExtraAPIs() {
    // TODO Auto-generated method stub
    return null;
  }
  
}
