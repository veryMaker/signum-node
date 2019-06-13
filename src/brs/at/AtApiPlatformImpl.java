package brs.at;

import brs.Appendix;
import brs.Burst;
import brs.Transaction;
import brs.crypto.Crypto;
import brs.fluxcapacitor.FluxValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.Arrays;

//NXT API IMPLEMENTATION

public class AtApiPlatformImpl extends AtApiImpl {

  private static final Logger logger = LoggerFactory.getLogger(AtApiPlatformImpl.class);

  private static final AtApiPlatformImpl instance = new AtApiPlatformImpl();


  private AtApiPlatformImpl() {
  }

  public static AtApiPlatformImpl getInstance() {
    return instance;
  }

  @Override
  public long get_Block_Timestamp( AtMachineState state ) {
    int height = state.getHeight();
    return AtApiHelper.getLongTimestamp( height, 0 );
  }

  public long get_Creation_Timestamp( AtMachineState state ) {
    return AtApiHelper.getLongTimestamp( state.getCreationBlockHeight(), 0 );
  }

  @Override
  public long get_Last_Block_Timestamp( AtMachineState state ) {
    int height = state.getHeight() - 1;
    return AtApiHelper.getLongTimestamp( height, 0 );
  }

  @Override
  public void put_Last_Block_Hash_In_A( AtMachineState state ) {
    ByteBuffer b = ByteBuffer.allocate( state.get_A1().length * 4 );
    b.order( ByteOrder.LITTLE_ENDIAN );

    b.put( Burst.getBlockchain().getBlockAtHeight(state.getHeight() - 1).getBlockHash() );
		
    b.clear();

    byte[] temp = new byte[ 8 ];

    b.get( temp, 0, 8 );
    state.set_A1( temp );

    b.get( temp, 0, 8 );
    state.set_A2( temp );

    b.get( temp, 0, 8 );
    state.set_A3( temp );

    b.get( temp, 0, 8 );
    state.set_A4( temp );
  }

  @Override
  public void A_to_Tx_after_Timestamp( long val, AtMachineState state ) {

    int height = AtApiHelper.longToHeight( val );
    int numOfTx = AtApiHelper.longToNumOfTx( val );

    byte[] b = state.getId();

    long tx = findTransaction( height, state.getHeight(), AtApiHelper.getLong( b ), numOfTx, state.minActivationAmount() );
    logger.debug("tx with id "+tx+" found");
    clear_A( state );
    state.set_A1( AtApiHelper.getByteArray( tx ) );

  }

  @Override
  public long get_Type_for_Tx_in_A( AtMachineState state ) {
    long txid = AtApiHelper.getLong( state.get_A1() );

    Transaction tx = Burst.getBlockchain().getTransaction( txid );
		
    if ( tx == null || (tx.getHeight() >= state.getHeight()) ) {
      return -1;
    }

    if (tx.getMessage() != null ) {
      return 1;
    }

    return 0;
  }

  @Override
  public long get_Amount_for_Tx_in_A( AtMachineState state ) {
    long txId = AtApiHelper.getLong( state.get_A1() );

    Transaction tx = Burst.getBlockchain().getTransaction( txId );
		
    if ( tx == null || (tx.getHeight() >= state.getHeight()) ) {
      return -1;
    }
		
    if( (tx.getMessage() == null || Burst.getFluxCapacitor().getValue(FluxValues.AT_FIX_BLOCK_2, state.getHeight())) && state.minActivationAmount() <= tx.getAmountNQT() ) {
      return tx.getAmountNQT() - state.minActivationAmount();
    }

    return 0;
  }

  @Override
  public long get_Timestamp_for_Tx_in_A( AtMachineState state ) {
    long txId = AtApiHelper.getLong( state.get_A1() );
    logger.debug("get timestamp for tx with id " + txId + " found");
    Transaction tx = Burst.getBlockchain().getTransaction( txId );
		
    if ( tx == null || (tx.getHeight() >= state.getHeight()) ) {
      return -1;
    }

    byte[] b        = state.getId();
    int blockHeight = tx.getHeight();
    int txHeight    = findTransactionHeight( txId, blockHeight, AtApiHelper.getLong(b), state.minActivationAmount() );

    return AtApiHelper.getLongTimestamp(blockHeight, txHeight);
  }

  @Override
  public long get_Random_Id_for_Tx_in_A( AtMachineState state ) {
    long txId = AtApiHelper.getLong( state.get_A1() );

    Transaction tx = Burst.getBlockchain().getTransaction( txId );
		
    if ( tx == null || (tx.getHeight() >= state.getHeight()) ) {
      return -1;
    }

    int txBlockHeight = tx.getHeight();
    int blockHeight = state.getHeight();

    if ( blockHeight - txBlockHeight < AtConstants.getInstance().BLOCKS_FOR_RANDOM( blockHeight ) ){ //for tests - for real case 1440
      state.setWaitForNumberOfBlocks( (int) AtConstants.getInstance().BLOCKS_FOR_RANDOM( blockHeight ) - ( blockHeight - txBlockHeight ) );
      state.getMachineState().pc -= 7;
      state.getMachineState().stopped = true;
      return 0;
    }

    MessageDigest digest = Crypto.sha256();

    byte[] senderPublicKey = tx.getSenderPublicKey();

    ByteBuffer bf = ByteBuffer.allocate( 32 + Long.SIZE + senderPublicKey.length );
    bf.order( ByteOrder.LITTLE_ENDIAN );
    bf.put(Burst.getBlockchain().getBlockAtHeight(blockHeight - 1).getGenerationSignature());
    bf.putLong( tx.getId() );
    bf.put( senderPublicKey);

    digest.update(bf.array());
    byte[] byteRandom = digest.digest();

      //System.out.println( "info: random for txid: " + Convert.toUnsignedLong( tx.getId() ) + "is: " + random );
    return Math.abs( AtApiHelper.getLong( Arrays.copyOfRange(byteRandom, 0, 8) ) );
  }

  @Override
  public void message_from_Tx_in_A_to_B( AtMachineState state ) {
    long txid = AtApiHelper.getLong( state.get_A1() );

    Transaction tx = Burst.getBlockchain().getTransaction( txid );
    if ( tx != null && tx.getHeight() >= state.getHeight() ) {
      tx = null;
    }

    ByteBuffer b = ByteBuffer.allocate( state.get_A1().length * 4 );
    b.order( ByteOrder.LITTLE_ENDIAN );
    if( tx != null ) {
      Appendix.Message txMessage = tx.getMessage();
      if (txMessage != null) {
        byte[] message = txMessage.getMessage();
        if ( message.length <= state.get_A1().length * 4 ) {
          b.put( message );
        }
      }
    }

    b.clear();

    byte[] temp = new byte[ 8 ];

    b.get( temp, 0, 8 );
    state.set_B1( temp );

    b.get( temp, 0, 8 );
    state.set_B2( temp );

    b.get( temp, 0, 8 );
    state.set_B3( temp );

    b.get( temp, 0, 8 );
    state.set_B4( temp );

  }
  @Override
  public void B_to_Address_of_Tx_in_A( AtMachineState state ) {
    long txId = AtApiHelper.getLong( state.get_A1() );
		
    clear_B( state );
		
    Transaction tx = Burst.getBlockchain().getTransaction( txId );
    if ( tx != null && tx.getHeight() >= state.getHeight() ) {
      tx = null;
    }
    if( tx != null ) {
        long address = tx.getSenderId();
        state.set_B1( AtApiHelper.getByteArray( address ) );
      }
  }

  @Override
  public void B_to_Address_of_Creator( AtMachineState state ) {
    long creator = AtApiHelper.getLong( state.getCreator() );

    clear_B( state );

    state.set_B1( AtApiHelper.getByteArray( creator ) );

  }
	
  @Override
  public void put_Last_Block_Generation_Signature_In_A( AtMachineState state ) {
    ByteBuffer b = ByteBuffer.allocate( state.get_A1().length * 4 );
    b.order( ByteOrder.LITTLE_ENDIAN );

    b.put( Burst.getBlockchain().getBlockAtHeight(state.getHeight() - 1).getGenerationSignature() );

    byte[] temp = new byte[ 8 ];

    b.get( temp, 0, 8 );
    state.set_A1( temp );

    b.get( temp, 0, 8 );
    state.set_A2( temp );

    b.get( temp, 0, 8 );
    state.set_A3( temp );

    b.get( temp, 0, 8 );
    state.set_A4( temp );


  }

  @Override
  public long get_Current_Balance( AtMachineState state ) {
    if(! Burst.getFluxCapacitor().getValue(FluxValues.AT_FIX_BLOCK_2, state.getHeight())) {
      return 0;
    }
		
    //long balance = Account.getAccount( AtApiHelper.getLong(state.getId()) ).getBalanceNQT();
    return state.getG_balance();
  }

  @Override
  public long get_Previous_Balance( AtMachineState state ) {
    if(! Burst.getFluxCapacitor().getValue(FluxValues.AT_FIX_BLOCK_2, state.getHeight())) {
      return 0;
    }
		
    return state.getP_balance();
  }

  @Override
  public void send_to_Address_in_B( long val, AtMachineState state ) {
    /*ByteBuffer b = ByteBuffer.allocate( state.get_B1().length * 4 );
      b.order( ByteOrder.LITTLE_ENDIAN );

      b.put( state.get_B1() );
      b.put( state.get_B2() );
      b.put( state.get_B3() );
      b.put( state.get_B4() );
    */
		
    if ( val < 1 )
      return;
		
    if ( val < state.getG_balance() ) {
      AtTransaction tx = new AtTransaction( state.getId(), state.get_B1().clone(), val, null );
      state.addTransaction( tx );

      state.setG_balance( state.getG_balance() - val );
    }
    else {
      AtTransaction tx = new AtTransaction( state.getId(), state.get_B1().clone(), state.getG_balance(), null );
      state.addTransaction( tx );
		
      state.setG_balance( 0L );
    }
  }

  @Override
  public void send_All_to_Address_in_B( AtMachineState state ) {
    /*ByteBuffer b = ByteBuffer.allocate( state.get_B1().length * 4 );
      b.order( ByteOrder.LITTLE_ENDIAN );

      b.put( state.get_B1() );
      b.put( state.get_B2() );
      b.put( state.get_B3() );
      b.put( state.get_B4() );
    */
    /*byte[] bId = state.getId();
      byte[] temp = new byte[ 8 ];
      for ( int i = 0; i < 8; i++ )
      {
      temp[ i ] = bId[ i ];
      }*/

    AtTransaction tx = new AtTransaction( state.getId(), state.get_B1().clone(), state.getG_balance(), null );
    state.addTransaction( tx );
    state.setG_balance( 0L );
  }

  @Override
  public void send_Old_to_Address_in_B( AtMachineState state ) {
    if ( state.getP_balance() > state.getG_balance() ) {
      AtTransaction tx = new AtTransaction( state.getId(), state.get_B1(), state.getG_balance(), null );
      state.addTransaction( tx );
			
      state.setG_balance( 0L );
      state.setP_balance( 0L );
    }
    else {
      AtTransaction tx = new AtTransaction( state.getId(), state.get_B1(), state.getP_balance(), null );
      state.addTransaction( tx );
			
      state.setG_balance( state.getG_balance() - state.getP_balance() );
      state.setP_balance( 0L );
    }
  }

  @Override
  public void send_A_to_Address_in_B( AtMachineState state ) {
		
    ByteBuffer b = ByteBuffer.allocate(32);
    b.order( ByteOrder.LITTLE_ENDIAN );
    b.put(state.get_A1());
    b.put(state.get_A2());
    b.put(state.get_A3());
    b.put(state.get_A4());
    b.clear();

    AtTransaction tx = new AtTransaction( state.getId(), state.get_B1(), 0L, b.array() );
    state.addTransaction(tx);		
  }

  public long add_Minutes_to_Timestamp( long val1, long val2, AtMachineState state) {
    int height = AtApiHelper.longToHeight( val1 );
    int numOfTx = AtApiHelper.longToNumOfTx( val1 );
    int addHeight = height + (int) (val2 / AtConstants.getInstance().AVERAGE_BLOCK_MINUTES( state.getHeight() ));

    return AtApiHelper.getLongTimestamp( addHeight, numOfTx );
  }

  private static Long findTransaction(int startHeight, int endHeight, Long atID, int numOfTx, long minAmount){
    return Burst.getStores().getAtStore().findTransaction(startHeight, endHeight, atID, numOfTx, minAmount);
  }

  private static int findTransactionHeight(Long transactionId, int height, Long atID, long minAmount){
    return Burst.getStores().getAtStore().findTransactionHeight(transactionId, height,atID, minAmount);
  }


}
