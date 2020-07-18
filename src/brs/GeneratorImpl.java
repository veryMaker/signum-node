package brs;

import brs.crypto.Crypto;
import brs.fluxcapacitor.FluxCapacitor;
import brs.fluxcapacitor.FluxValues;
import brs.props.PropertyService;
import brs.props.Props;
import brs.services.AccountService;
import brs.services.TimeService;
import brs.util.Convert;
import brs.util.Listener;
import brs.util.Listeners;
import brs.util.ThreadPool;
import burst.kit.crypto.BurstCrypto;
import burst.kit.entity.BurstID;
import burst.kit.entity.BurstValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class GeneratorImpl implements Generator {
  private static final Logger logger = LoggerFactory.getLogger(GeneratorImpl.class);

  private final Listeners<GeneratorState, Event> listeners = new Listeners<>();
  private final ConcurrentMap<Long, GeneratorStateImpl> generators = new ConcurrentHashMap<>();
  private final BurstCrypto burstCrypto = BurstCrypto.getInstance();
  private final Blockchain blockchain;
  private final AccountService accountService;
  private final TimeService timeService;
  private final FluxCapacitor fluxCapacitor;
  
  private static final double LN_SCALE = ((double) Constants.BURST_BLOCK_TIME) / Math.log((double) Constants.BURST_BLOCK_TIME);
  // private static final double LN_SCALE = 49d; // value that would keep the legacy network size estimation close to real capacity

  public GeneratorImpl(Blockchain blockchain, AccountService accountService, TimeService timeService, FluxCapacitor fluxCapacitor) {
    this.blockchain = blockchain;
    this.accountService = accountService;
    this.timeService = timeService;
    this.fluxCapacitor = fluxCapacitor;
  }

  private Runnable generateBlockThread(BlockchainProcessor blockchainProcessor) {
    return () -> {
      if (blockchainProcessor.isScanning()) {
        return;
      }
      try {
        long currentBlock = blockchain.getLastBlock().getHeight();
        Iterator<Entry<Long, GeneratorStateImpl>> it = generators.entrySet().iterator();
        while (it.hasNext() && !Thread.currentThread().isInterrupted() && ThreadPool.running.get()) {
          Entry<Long, GeneratorStateImpl> generator = it.next();
          if (currentBlock < generator.getValue().getBlock()) {
            generator.getValue().forge(blockchainProcessor);
          } else {
            it.remove();
          }
        }
      } catch (BlockchainProcessor.BlockNotAcceptedException e) {
        logger.debug("Error in block generation thread", e);
      }
    };
  }

  @Override
  public void generateForBlockchainProcessor(ThreadPool threadPool, BlockchainProcessor blockchainProcessor) {
    threadPool.scheduleThread("GenerateBlocks", generateBlockThread(blockchainProcessor), 500, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean addListener(Listener<GeneratorState> listener, Event eventType) {
    return listeners.addListener(listener, eventType);
  }

  @Override
  public boolean removeListener(Listener<GeneratorState> listener, Event eventType) {
    return listeners.removeListener(listener, eventType);
  }

  @Override
  public GeneratorState addNonce(String secretPhrase, Long nonce) {
    byte[] publicKey = Crypto.getPublicKey(secretPhrase);
    return addNonce(secretPhrase, nonce, publicKey);
  }

  @Override
  public GeneratorState addNonce(String secretPhrase, Long nonce, byte[] publicKey) {
    byte[] publicKeyHash = Crypto.sha256().digest(publicKey);
    long id = Convert.fullHashToId(publicKeyHash);

    GeneratorStateImpl generator = new GeneratorStateImpl(secretPhrase, nonce, publicKey, id);
    GeneratorStateImpl curGen = generators.get(id);
    if (curGen == null || generator.getBlock() > curGen.getBlock() || generator.getDeadline().compareTo(curGen.getDeadline()) < 0) {
      generators.put(id, generator);
      listeners.notify(generator, Event.NONCE_SUBMITTED);
      if (logger.isDebugEnabled()) {
        logger.debug("Account {} started mining, deadline {} seconds", Convert.toUnsignedLong(id), generator.getDeadline());
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Account {} already has a better nonce", Convert.toUnsignedLong(id));
      }
    }

    return generator;
  }

  @Override
  public Collection<GeneratorState> getAllGenerators() {
    return Collections.unmodifiableCollection(generators.values());
  }

  @Override
  public byte[] calculateGenerationSignature(byte[] lastGenSig, long lastGenId) {
    return burstCrypto.calculateGenerationSignature(lastGenSig, lastGenId);
  }

  @Override
  public int calculateScoop(byte[] genSig, long height) {
    return burstCrypto.calculateScoop(genSig, height);
  }

  private int getPocVersion(int blockHeight) {
    return fluxCapacitor.getValue(FluxValues.POC2, blockHeight) ? 2 : 1;
  }

  @Override
  public BigInteger calculateHit(long accountId, long nonce, byte[] genSig, int scoop, int blockHeight) {
    return burstCrypto.calculateHit(accountId, nonce, genSig, scoop, getPocVersion(blockHeight));
  }

  @Override
  public BigInteger calculateHit(byte[] genSig, byte[] scoopData) {
    return burstCrypto.calculateHit(genSig, scoopData);
  }

  @Override
  public BigInteger calculateDeadline(BigInteger hit, long baseTarget, long commitment, long commitmentBaseTarget, int blockHeight) {
    BigInteger deadline = hit.divide(BigInteger.valueOf(baseTarget));
    if(fluxCapacitor.getValue(FluxValues.NEXT_FORK, blockHeight)) {
      double commitmentFactor = ((double)commitment)/commitmentBaseTarget;
      commitmentFactor = Math.pow(commitmentFactor, 0.1505);
      commitmentFactor = Math.min(4.0, commitmentFactor);
      commitmentFactor = Math.max(0.25, commitmentFactor);
      
      double nextDeadline = deadline.doubleValue()*commitmentFactor;
      if(nextDeadline > 0) {
        // Avoid zero logarithm
        nextDeadline = Math.log(nextDeadline) * LN_SCALE;
        deadline = BigInteger.valueOf((long)(nextDeadline));
      }
    }
    else if(fluxCapacitor.getValue(FluxValues.SODIUM, blockHeight)) {
      if(deadline.bitLength() < 100 && deadline.longValue() > 0L) {
    	  // Avoid the double precision limit for extremely large numbers (of no value) and zero logarithm
    	  double sodiumDeadline = Math.log(deadline.doubleValue()) * LN_SCALE;
    	  deadline = BigInteger.valueOf((long)sodiumDeadline);
      }
    }
    return deadline;
  }

  public class GeneratorStateImpl implements GeneratorState {
    private final Long accountId;
    private final String secretPhrase;
    private final byte[] publicKey;
    private final BigInteger deadline;
    private final BigInteger hit;
    private final long baseTarget;
    private final long nonce;
    private final long block;

    private GeneratorStateImpl(String secretPhrase, Long nonce, byte[] publicKey, Long account) {
      this.secretPhrase = secretPhrase;
      this.publicKey = publicKey;
      // need to store publicKey in addition to accountId, because the account may not have had its publicKey set yet
      this.accountId = account;
      this.nonce = nonce;

      Block lastBlock = blockchain.getLastBlock();

      this.block = (long) lastBlock.getHeight() + 1;

      byte[] lastGenSig = lastBlock.getGenerationSignature();
      Long lastGenerator = lastBlock.getGeneratorId();

      byte[] newGenSig = calculateGenerationSignature(lastGenSig, lastGenerator);

      int scoopNum = calculateScoop(newGenSig, lastBlock.getHeight() + 1L);

      baseTarget = lastBlock.getBaseTarget();
      hit = calculateHit(accountId, nonce, newGenSig, scoopNum, lastBlock.getHeight() + 1);
      long commitmment = 0L;
      if(fluxCapacitor.getValue(FluxValues.NEXT_FORK, lastBlock.getHeight() + 1)) {
        commitmment = calculateCommitment(accountId, baseTarget, lastBlock.getHeight() + 1);
      }
      
      deadline = calculateDeadline(hit, baseTarget, commitmment, lastBlock.getAverageCommitment(), lastBlock.getHeight() + 1);
    }

    @Override
    public byte[] getPublicKey() {
      return publicKey;
    }

    @Override
    public Long getAccountId() {
      return accountId;
    }

    @Override
    public BigInteger getDeadline() {
      return deadline;
    }
    
    @Override
    public BigInteger getDeadlineLegacy() {
      return hit.divide(BigInteger.valueOf(baseTarget));
    }

    @Override
    public long getBlock() {
      return block;
    }

    private void forge(BlockchainProcessor blockchainProcessor) throws BlockchainProcessor.BlockNotAcceptedException {
      Block lastBlock = blockchain.getLastBlock();

      int elapsedTime = timeService.getEpochTime() - lastBlock.getTimestamp();
      if (BigInteger.valueOf(elapsedTime).compareTo(deadline) > 0) {
        blockchainProcessor.generateBlock(secretPhrase, publicKey, nonce);
      }
    }
  }

  public static class MockGenerator extends GeneratorImpl {
    private final PropertyService propertyService;
    public MockGenerator(PropertyService propertyService, Blockchain blockchain, TimeService timeService, FluxCapacitor fluxCapacitor) {
      super(blockchain, null, timeService, fluxCapacitor);
      this.propertyService = propertyService;
    }

    @Override
    public BigInteger calculateHit(long accountId, long nonce, byte[] genSig, int scoop, int blockHeight) {
      return BigInteger.valueOf(propertyService.getInt(Props.DEV_MOCK_MINING_DEADLINE));
    }

    @Override
    public BigInteger calculateHit(byte[] genSig, byte[] scoopData) {
      return BigInteger.valueOf(propertyService.getInt(Props.DEV_MOCK_MINING_DEADLINE));
    }

    @Override
    public BigInteger calculateDeadline(BigInteger hit, long baseTarget, long commitment, long commitmentBaseTarget, int blockHeight) {
      return BigInteger.valueOf(propertyService.getInt(Props.DEV_MOCK_MINING_DEADLINE));
    }
  }

  @Override
  public long calculateCommitment(long generatorId, long baseTarget, int height) {
    // Check on the number of blocks mined to estimate the capacity and also the committed balance
    int nBlocksMined = 0;
    long committedBalance = 0;
    int capacityEstimationBlocks = Constants.CAPACITY_ESTIMATION_BLOCKS;
    // TODO: consider making the getAccount already return the committed balance
    Account account = accountService.getAccount(generatorId);
    if (account != null) {
        committedBalance = account.getBalanceNQT();
        Account accountPast = accountService.getAccount(generatorId, height - Constants.MIN_MAX_ROLLBACK/2);
        if(accountPast == null) {
            committedBalance = 0;
        }
        else {
            committedBalance = Math.min(committedBalance, accountPast.getBalanceNQT());
        }
        if(committedBalance > 0) {
          // First we try to estimate the capacity using recent blocks
          nBlocksMined = blockchain.getBlocksCount(account, capacityEstimationBlocks - 1);
          if(nBlocksMined < 3) {
            // Use more blocks in the past to make the estimation if that is necessary
            capacityEstimationBlocks = Constants.CAPACITY_ESTIMATION_BLOCKS_MAX;
            nBlocksMined = blockchain.getBlocksCount(account, capacityEstimationBlocks - 1);        
          }
        }
    }
    nBlocksMined++; // The current block being mined
    
    long genesisTarget = Constants.INITIAL_BASE_TARGET;
    if (Burst.getFluxCapacitor().getValue(FluxValues.SODIUM)) {
      genesisTarget = (long)(genesisTarget / 1.83d);
    }
    long estimatedCapacityGb = genesisTarget*nBlocksMined*1000L/(baseTarget * capacityEstimationBlocks);
    if(estimatedCapacityGb < 1000L) {
      estimatedCapacityGb = 1000L;
    }
    // Commitment being the committed balance per TiB
    long commitment = (committedBalance/estimatedCapacityGb) * 1000L;
    
    logger.info("Block {}, Network {} TiB, miner {}, forged {}/{} blocks, {} TiB, commitment {}/TiB",
        height,
        (double)genesisTarget/baseTarget,
        BurstID.fromLong(generatorId).getID(),
        nBlocksMined, capacityEstimationBlocks, estimatedCapacityGb/1000D,
        BurstValue.fromPlanck(commitment).toFormattedString());
    
    return commitment;
  }
}
