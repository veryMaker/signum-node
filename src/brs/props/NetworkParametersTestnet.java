package brs.props;

public class NetworkParametersTestnet extends NetworkParametersBase {
  
  @Override
  public void initialize() {
    setProperty(Props.NETWORK_NAME, "Signum-TESTNET");

    setProperty(Props.DEV_REWARD_RECIPIENT_ENABLE_BLOCK_HEIGHT, "0");
    setProperty(Props.DEV_DIGITAL_GOODS_STORE_BLOCK_HEIGHT, "0");
    setProperty(Props.DEV_AUTOMATED_TRANSACTION_BLOCK_HEIGHT, "0");
    setProperty(Props.DEV_AT_FIX_BLOCK_2_BLOCK_HEIGHT, "0");
    setProperty(Props.DEV_AT_FIX_BLOCK_3_BLOCK_HEIGHT, "0");
    setProperty(Props.DEV_AT_FIX_BLOCK_4_BLOCK_HEIGHT, "0");
    setProperty(Props.DEV_PRE_POC2_BLOCK_HEIGHT, "0");
    setProperty(Props.DEV_POC2_BLOCK_HEIGHT, "0");
    
    setProperty(Props.DEV_SODIUM_BLOCK_HEIGHT, "160620");
    setProperty(Props.DEV_SIGNUM, "269100");
    setProperty(Props.DEV_POC_PLUS, "269700");
    setProperty(Props.DEV_SPEEDWAY, "338090");
  }  
}
