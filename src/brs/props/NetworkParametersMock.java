package brs.props;

import java.security.SecureRandom;

import brs.util.Convert;

public class NetworkParametersMock extends NetworkParametersTestnet {
  
  @Override
  public void initialize() {
    super.initialize();
    
    setProperty(Props.NETWORK_NAME, "Signum-LOCAL-MOCK");

    // We use a different one for every time we start.
    SecureRandom rand = new SecureRandom();
    long genesisBlockId = rand.nextLong();
    setProperty(Props.GENESIS_BLOCK_ID, Convert.toUnsignedLong(genesisBlockId));

    setProperty(Props.DEV_OFFLINE, "true");
    setProperty(Props.DEV_MOCK_MINING, "true");
    
    setProperty(Props.SODIUM_BLOCK_HEIGHT, "0");
    setProperty(Props.SIGNUM_HEIGHT, "0");
    setProperty(Props.POC_PLUS_HEIGHT, "0");
    setProperty(Props.SPEEDWAY_HEIGHT, "0");
    
    setProperty(Props.BRS_CHECKPOINT_HEIGHT, "-1");
    setProperty(Props.BRS_CHECKPOINT_HASH, "");
  }  
}
