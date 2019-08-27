package brs;

import brs.crypto.Crypto;
import brs.fluxcapacitor.FluxCapacitor;
import brs.fluxcapacitor.FluxValues;
import brs.props.PropertyService;
import brs.props.Props;
import brs.services.TimeService;
import brs.util.Convert;
import brs.util.Listeners;
import brs.util.MiningPlot;
import brs.util.ThreadPool;
import brs.util.Convert;
import brs.util.Listeners;
import brs.util.ThreadPool;
import burst.kit.crypto.BurstCrypto;
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
import java.util.function.Consumer;

public class GeneratorImpl implements Generator {
  private static final Logger logger = LoggerFactory.getLogger(GeneratorImpl.class);

  private final Listeners<GeneratorState, Event> listeners = new Listeners<>();
  private final ConcurrentMap<Long, GeneratorStateImpl> generators = new ConcurrentHashMap<>();
  private final BurstCrypto burstCrypto = BurstCrypto.getInstance();
  private final DependencyProvider dp;

  public GeneratorImpl(DependencyProvider dp) {
    this.dp = dp;
  }

  private Runnable generateBlockThread(BlockchainProcessor blockchainProcessor) {
    return () -> {
      if (blockchainProcessor.isScanning()) {
        return;
      }
      try {
        long currentBlock = dp.blockchain.getLastBlock().getHeight();
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
  public void generateForBlockchainProcessor(DependencyProvider dp) {
    dp.threadPool.scheduleThread("GenerateBlocks", generateBlockThread(dp.blockchainProcessor), 500, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean addListener(Consumer<GeneratorState> listener, Event eventType) {
    return listeners.addListener(listener, eventType);
  }

  @Override
  public boolean removeListener(Consumer<GeneratorState> listener, Event eventType) {
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
    long id = Convert.INSTANCE.fullHashToId(publicKeyHash);

    GeneratorStateImpl generator = new GeneratorStateImpl(secretPhrase, nonce, publicKey, id);
    GeneratorStateImpl curGen = generators.get(id);
    if (curGen == null || generator.getBlock() > curGen.getBlock() || generator.getDeadline().compareTo(curGen.getDeadline()) < 0) {
      generators.put(id, generator);
      listeners.accept(generator, Event.NONCE_SUBMITTED);
      if (logger.isDebugEnabled()) {
        logger.debug("Account {} started mining, deadline {} seconds", Convert.INSTANCE.toUnsignedLong(id), generator.getDeadline());
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Account {} already has a better nonce", Convert.INSTANCE.toUnsignedLong(id));
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
    return dp.fluxCapacitor.getValue(FluxValues.POC2, blockHeight) ? 2 : 1;
  }

  @Override
  public BigInteger calculateHit(long accountId, long nonce, byte[] genSig, int scoop, int blockHeight) {
    return burstCrypto.calculateHit(accountId, nonce, genSig, scoop, getPocVersion(blockHeight));
  }

  @Override
  public BigInteger calculateHit(long accountId, long nonce, byte[] genSig, byte[] scoopData) {
    return burstCrypto.calculateHit(accountId, nonce, genSig, scoopData);
  }

  @Override
  public BigInteger calculateDeadline(long accountId, long nonce, byte[] genSig, int scoop, long baseTarget, int blockHeight) {
    return burstCrypto.calculateDeadline(accountId, nonce, genSig, scoop, baseTarget, getPocVersion(blockHeight));
  }

  public class GeneratorStateImpl implements GeneratorState {
    private final Long accountId;
    private final String secretPhrase;
    private final byte[] publicKey;
    private final BigInteger deadline;
    private final long nonce;
    private final long block;

    private GeneratorStateImpl(String secretPhrase, Long nonce, byte[] publicKey, Long account) {
      this.secretPhrase = secretPhrase;
      this.publicKey = publicKey;
      // need to store publicKey in addition to accountId, because the account may not have had its publicKey set yet
      this.accountId = account;
      this.nonce = nonce;

      Block lastBlock = dp.blockchain.getLastBlock();

      this.block = (long) lastBlock.getHeight() + 1;

      byte[] lastGenSig = lastBlock.getGenerationSignature();
      Long lastGenerator = lastBlock.getGeneratorId();

      byte[] newGenSig = calculateGenerationSignature(lastGenSig, lastGenerator);

      int scoopNum = calculateScoop(newGenSig, lastBlock.getHeight() + 1L);

      deadline = calculateDeadline(accountId, nonce, newGenSig, scoopNum, lastBlock.getBaseTarget(), lastBlock.getHeight() + 1);
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
    public long getBlock() {
      return block;
    }

    private void forge(BlockchainProcessor blockchainProcessor) throws BlockchainProcessor.BlockNotAcceptedException {
      Block lastBlock = dp.blockchain.getLastBlock();

      int elapsedTime = dp.timeService.getEpochTime() - lastBlock.getTimestamp();
      if (BigInteger.valueOf(elapsedTime).compareTo(deadline) > 0) {
        blockchainProcessor.generateBlock(secretPhrase, publicKey, nonce);
      }
    }
  }

  public static class MockGenerator extends GeneratorImpl {
    private final DependencyProvider dp;
    public MockGenerator(DependencyProvider dp) {
      super(dp);
      this.dp = dp;
    }

    @Override
    public BigInteger calculateHit(long accountId, long nonce, byte[] genSig, int scoop, int blockHeight) {
      return BigInteger.valueOf(dp.propertyService.get(Props.DEV_MOCK_MINING_DEADLINE));
    }

    @Override
    public BigInteger calculateHit(long accountId, long nonce, byte[] genSig, byte[] scoopData) {
      return BigInteger.valueOf(dp.propertyService.get(Props.DEV_MOCK_MINING_DEADLINE));
    }

    @Override
    public BigInteger calculateDeadline(long accountId, long nonce, byte[] genSig, int scoop, long baseTarget, int blockHeight) {
      return BigInteger.valueOf(dp.propertyService.get(Props.DEV_MOCK_MINING_DEADLINE));
    }
  }
}
