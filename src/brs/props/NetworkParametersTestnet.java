package brs.props;

public class NetworkParametersTestnet extends NetworkParametersBase {
  
  @Override
  public void initialize() {
    setProperty(Props.NETWORK_NAME, "Signum-TESTNET");

    setProperty(Props.REWARD_RECIPIENT_ENABLE_BLOCK_HEIGHT, "0");
    setProperty(Props.DIGITAL_GOODS_STORE_BLOCK_HEIGHT, "0");
    setProperty(Props.AUTOMATED_TRANSACTION_BLOCK_HEIGHT, "0");
    setProperty(Props.AT_FIX_BLOCK_2_BLOCK_HEIGHT, "0");
    setProperty(Props.AT_FIX_BLOCK_3_BLOCK_HEIGHT, "0");
    setProperty(Props.AT_FIX_BLOCK_4_BLOCK_HEIGHT, "0");
    setProperty(Props.PRE_POC2_BLOCK_HEIGHT, "0");
    setProperty(Props.POC2_BLOCK_HEIGHT, "0");
    
    setProperty(Props.SODIUM_BLOCK_HEIGHT, "160620");
    setProperty(Props.SIGNUM_HEIGHT, "269100");
    setProperty(Props.POC_PLUS_HEIGHT, "269700");
    setProperty(Props.SPEEDWAY_HEIGHT, "338090");
    
    setProperty(Props.BRS_CHECKPOINT_HEIGHT, "249000");
    setProperty(Props.BRS_CHECKPOINT_HASH, "41e28a9068369bd528f7da97951b07e42d64c263e8b190dd56287ad2b0c63a38");
    
    setProperty(Props.ADDRESS_PREFIX, "TS");
    setProperty(Props.VALUE_SUFIX, "TSIGNA");
    setProperty(Props.EXPERIMENTAL, "true");
    
    setProperty(Props.P2P_PORT, "7123");
    setProperty(Props.API_PORT, "6876");
    setProperty(Props.API_V2_PORT, "6878");
    
    setProperty(Props.P2P_REBROADCAST_TO, "");
    setProperty(Props.P2P_BOOTSTRAP_PEERS, "nivbox.co.uk; testnet.burstcoin.network; 77.66.65.240; 2.29.196.249; 176.9.197.183;");
    setProperty(Props.P2P_NUM_BOOTSTRAP_CONNECTIONS, "1");
    
  }  
}
