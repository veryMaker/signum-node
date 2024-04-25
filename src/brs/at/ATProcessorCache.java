package brs.at;

import brs.SignumException;
import brs.Transaction;
import brs.db.TransactionDb;
import brs.db.sql.Db;
import brs.schema.tables.records.TransactionRecord;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.stream.Collectors;

import static brs.schema.Tables.TRANSACTION;

public final class ATProcessorCache {

  public static class CacheMissException extends Exception {
  }

  private static final Logger logger = LoggerFactory.getLogger(ATProcessorCache.class);

  private static ATProcessorCache instance;
  private static final int CostOfOneAT = AtConstants.AT_ID_SIZE + 16;
  private final LinkedHashMap<Long, ATContext> atMap = new LinkedHashMap<>();
  private int currentBlockHeight = Integer.MIN_VALUE;
  private int lowestBlockHeight = Integer.MAX_VALUE;
  private long minimumActivationAmount = Long.MAX_VALUE;

  public static class ATContext {
    public byte[] md5;
    public AT at;
    public ArrayList<Transaction> transactions = new ArrayList<>();
  }

  private ATProcessorCache() {

  }

  public int getLowestBlockHeight() {
    return lowestBlockHeight;
  }

  public HashMap<Long, ATContext> getAtMap() {
    return this.atMap;
  }

  public static ATProcessorCache getInstance() {
    if (instance == null) {
      instance = new ATProcessorCache();
    }
    return instance;
  }

  public void reset() {
    atMap.clear();
    currentBlockHeight = Integer.MIN_VALUE;
    lowestBlockHeight = Integer.MAX_VALUE;
    minimumActivationAmount = Long.MAX_VALUE;
  }

  public LinkedHashMap<Long, ATContext> load(byte[] ats, int blockHeight) throws AtException {

    this.reset();
    this.currentBlockHeight = blockHeight;
    this.lowestBlockHeight = blockHeight - 1000;
    if (ats == null || ats.length == 0) {
      return this.atMap;
    }
    long startTime = System.nanoTime();
    parseATBytes(ats);
    // if this is impacts on db access, we might add a multi AT fetch -> getATs
    this.atMap.forEach((atId, proxy) -> {
      AT at = AT.getAT(atId);
      logger.debug("Cached AT {}", atId);
      this.minimumActivationAmount = Math.min(this.minimumActivationAmount, at.minActivationAmount());
      proxy.at = at;
    });
    loadRelevantTransactions();
    long executionTime = (System.nanoTime() - startTime) / 1000000;
    logger.debug("Cache Duration for {} ATs: {} milliseconds", atMap.size(), executionTime);
    return this.atMap;
  }

  private void parseATBytes(byte[] ats) throws AtException {
    if (ats.length % (CostOfOneAT) != 0) {
      throw new AtException("ATs must be a multiple of cost of one AT ( " + CostOfOneAT + " )");
    }

    ByteBuffer b = ByteBuffer.wrap(ats);
    b.order(ByteOrder.LITTLE_ENDIAN);

    byte[] atId = new byte[AtConstants.AT_ID_SIZE];
    byte[] md5 = new byte[16];

    while (b.remaining() >= CostOfOneAT) {
      b.get(atId);
      b.get(md5);
      long atIdLong = AtApiHelper.getLong(atId);
      if (atMap.containsKey(atIdLong)) {
        throw new AtException("AT included in block multiple times");
      }
      ATContext atContext = new ATContext();
      atContext.md5 = md5.clone();
      atMap.put(atIdLong, atContext);
    }
  }

  // TODO: make this smarter, instead of loading them all
  private void loadRelevantTransactions() {
    logger.debug("Loading tx for lo: {}, hi: {}, amount: {}, no ATs: {}", lowestBlockHeight, currentBlockHeight, minimumActivationAmount, getAtMap().size());
    Result<TransactionRecord> result = Db.useDSLContext(ctx -> {
      return ctx.selectFrom(TRANSACTION)
        .where(TRANSACTION.HEIGHT.between(lowestBlockHeight, currentBlockHeight))
        .and(TRANSACTION.RECIPIENT_ID.in(getAtMap().keySet()))
        .and(TRANSACTION.AMOUNT.greaterOrEqual(minimumActivationAmount))
        .orderBy(TRANSACTION.HEIGHT, TRANSACTION.ID)
        .fetch();
    });
    logger.debug("Fetched {} tx", result.size());

    TransactionDb db = Db.getDbsByDatabaseType().getTransactionDb();
    for (TransactionRecord r : result) {
      try {
        ATContext context = this.atMap.get(r.getRecipientId());
        if (context != null) {
          Transaction tx = db.loadTransaction(r);
          context.transactions.add(tx);
        }
      } catch (SignumException.ValidationException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Long findTransactionId(int startHeight, int endHeight, Long atID, int numOfTx, long minAmount) throws CacheMissException {
    long startTime = System.nanoTime();

    ATContext atContext = this.atMap.get(atID);
    if (atContext == null) {
      logger.debug("AT {} not found", atID);
      throw new CacheMissException();
    }

    // can be -1
    if (startHeight <= 0) {
      startHeight = atContext.at.getCreationBlockHeight();
    }

    if (startHeight < lowestBlockHeight || endHeight > currentBlockHeight) {
      logger.debug("Out of range (start: {}, end: {} - wanted block: {})", lowestBlockHeight, currentBlockHeight, startHeight);
      throw new CacheMissException();
    }

    long id = 0;
    final int finalStartHeight = startHeight;
    List<Transaction> collected = atContext.transactions.stream()
      .filter(t ->
        t.getHeight() >= finalStartHeight &&
          t.getHeight() < endHeight &&
          t.getAmountNqt() >= minAmount)
      .collect(Collectors.toList());

    if (collected.size() > numOfTx) {
      id = collected.get(numOfTx).getId();
    }

    long executionTime = (System.nanoTime() - startTime) / 1000000;
    logger.debug("txId: {} - Duration: {} milliseconds", id, executionTime);
    return id;
  }


  public int findTransactionHeight(Long transactionId, int height, Long atID, long minAmount) throws CacheMissException {
    long startTime = System.nanoTime();
    ATContext atContext = this.atMap.get(atID);
    if (atContext == null) {
      logger.debug("AT {} not found", atID);
      throw new CacheMissException();
    }

    int count = 0;
    ArrayList<Transaction> transactions = atContext.transactions;
    for (Transaction t : transactions) {
      if (t.getHeight() == height && t.getAmountNqt() >= minAmount) {
        ++count;
        if (t.getId() == transactionId) {
          break;
        }
      }
    }

    long executionTime = (System.nanoTime() - startTime) / 1000000;
    logger.debug("Cache Hit: {}, Duration: {} milliseconds", count, executionTime);
    if (count == 0 || count >= transactions.size()) {
      throw new CacheMissException();
    }
    return count;
  }
}

