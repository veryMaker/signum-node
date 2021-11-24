package brs.props;

import java.util.Arrays;

import brs.fluxcapacitor.FluxValue;
import brs.fluxcapacitor.FluxValues;
import brs.fluxcapacitor.HistoricalMoments;

public class NetworkParametersTestnet extends NetworkParametersBase {
  
  public NetworkParametersTestnet() {
    
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
    FluxValues.MIN_CAPACITY.updateValueChanges(Arrays.asList(
        new FluxValue.ValueChange<Long>(HistoricalMoments.GENESIS, 1_000L),
        new FluxValue.ValueChange<Long>(HistoricalMoments.SPEEDWAY, 8_000L)
        ));

    setProperty(Props.DEV_NEXT_FORK_BLOCK_HEIGHT, "364400");

    setProperty(Props.BRS_CHECKPOINT_HEIGHT, "364000");
    setProperty(Props.BRS_CHECKPOINT_HASH, "19095af8f50555058682a4bf79db41cabcc5500bc856231f4ad8051de2d98cff");
    
    setProperty(Props.ADDRESS_PREFIX, "TS");
    setProperty(Props.VALUE_SUFIX, "TSIGNA");
    setProperty(Props.EXPERIMENTAL, "true");
    
    setProperty(Props.P2P_PORT, "7123");
    setProperty(Props.API_PORT, "6876");
    
    // setProperty(Props.P2P_BOOTSTRAP_PEERS, "");
    setProperty(Props.P2P_REBROADCAST_TO, "nivbox.co.uk; testnet.signum.network; 77.66.65.240; 2.29.196.249; 176.9.197.183;");
    setProperty(Props.P2P_NUM_BOOTSTRAP_CONNECTIONS, "1");
    
  }  
}
