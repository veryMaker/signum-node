package brs;

import brs.BurstException.ValidationException;
import brs.db.sql.Db;
import brs.db.store.Dbs;
import brs.fluxcapacitor.FluxValues;
import brs.peer.Peer;
import brs.peer.Peers;
import brs.props.PropertyService;
import brs.props.Props;
import brs.services.AccountService;
import brs.services.TimeService;
import brs.services.TransactionService;
import brs.unconfirmedtransactions.UnconfirmedTransactionStore;
import brs.util.JSON;
import brs.util.Listeners;
import brs.util.ThreadPool;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static brs.http.common.ResultFields.UNCONFIRMED_TRANSACTIONS_RESPONSE;

public class TransactionProcessorImpl implements TransactionProcessor {

  private static final Logger logger = LoggerFactory.getLogger(TransactionProcessorImpl.class);

  private final boolean testUnconfirmedTransactions;

  private final Object unconfirmedTransactionsSyncObj = new Object();
  private final DependencyProvider dp;
  private final Listeners<List<? extends Transaction>,Event> transactionListeners = new Listeners<>();
  private final Function<Peer, List<Transaction>> foodDispenser;
  private final BiConsumer<Peer, List<Transaction>> doneFeedingLog;

  public TransactionProcessorImpl(DependencyProvider dp) {
    this.dp = dp;

    this.testUnconfirmedTransactions = dp.propertyService.get(Props.BRS_TEST_UNCONFIRMED_TRANSACTIONS);

    this.foodDispenser = (dp.unconfirmedTransactionStore::getAllFor);
    this.doneFeedingLog = (dp.unconfirmedTransactionStore::markFingerPrintsOf);

      Runnable getUnconfirmedTransactions = () -> {
          try {
              try {
                  synchronized (unconfirmedTransactionsSyncObj) {
                      Peer peer = Peers.getAnyPeer(Peer.State.CONNECTED);
                      if (peer == null) {
                          return;
                      }
                      JsonObject response = Peers.readUnconfirmedTransactionsNonBlocking(peer).get();
                      if (response == null) {
                          return;
                      }

                      JsonArray transactionsData = JSON.getAsJsonArray(response.get(UNCONFIRMED_TRANSACTIONS_RESPONSE));

                      if (transactionsData == null || transactionsData.size() == 0) {
                          return;
                      }

                      try {
                          List<Transaction> addedTransactions = processPeerTransactions(transactionsData, peer);
                          Peers.feedingTime(peer, foodDispenser, doneFeedingLog);

                          if (!addedTransactions.isEmpty()) {
                              List<Peer> activePrioPlusExtra = Peers.getAllActivePriorityPlusSomeExtraPeers();
                              activePrioPlusExtra.remove(peer);

                              List<CompletableFuture<?>> expectedResults = new ArrayList<>();

                              for (Peer otherPeer : activePrioPlusExtra) {
                                  CompletableFuture<JsonObject> unconfirmedTransactionsResult = Peers.readUnconfirmedTransactionsNonBlocking(otherPeer);

                                  unconfirmedTransactionsResult.whenComplete((jsonObject, throwable) -> {
                                      try {
                                          processPeerTransactions(transactionsData, otherPeer);
                                          Peers.feedingTime(otherPeer, foodDispenser, doneFeedingLog);
                                      } catch (ValidationException | RuntimeException e) {
                                          peer.blacklist(e, "pulled invalid data using getUnconfirmedTransactions");
                                      }
                                  });

                                  expectedResults.add(unconfirmedTransactionsResult);
                              }

                              CompletableFuture.allOf(expectedResults.toArray(new CompletableFuture[0])).join();
                          }
                      } catch (ValidationException | RuntimeException e) {
                          peer.blacklist(e, "pulled invalid data using getUnconfirmedTransactions");
                      }
                  }
              } catch (Exception e) {
                  logger.debug("Error processing unconfirmed transactions", e);
              }

          } catch (Exception t) {
              logger.info("CRITICAL ERROR. PLEASE REPORT TO THE DEVELOPERS.\n" + t.toString(), t);
              System.exit(1);
          }
      };
    dp.threadPool.scheduleThread("PullUnconfirmedTransactions", getUnconfirmedTransactions, 5);
  }

    @Override
  public boolean addListener(Consumer<List<? extends Transaction>> listener, Event eventType) {
    return transactionListeners.addListener(listener, eventType);
  }

  @Override
  public boolean removeListener(Consumer<List<? extends Transaction>> listener, Event eventType) {
    return transactionListeners.removeListener(listener, eventType);
  }

  @Override
  public void notifyListeners(List<? extends Transaction> transactions, Event eventType) {
    transactionListeners.accept(transactions, eventType);
  }

  public Object getUnconfirmedTransactionsSyncObj() {
    return unconfirmedTransactionsSyncObj;
  }

  @Override
  public List<Transaction> getAllUnconfirmedTransactions() {
    return dp.unconfirmedTransactionStore.getAll();
  }

  @Override
  public int getAmountUnconfirmedTransactions() {
    return dp.unconfirmedTransactionStore.getAmount();
  }

  @Override
  public List<Transaction> getAllUnconfirmedTransactionsFor(Peer peer) {
    return dp.unconfirmedTransactionStore.getAllFor(peer);
  }

  @Override
  public void markFingerPrintsOf(@NotNull Peer peer, List<? extends Transaction> transactions) {
    dp.unconfirmedTransactionStore.markFingerPrintsOf(peer, transactions);
  }

  @Override
  public Transaction getUnconfirmedTransaction(long transactionId) {
    return dp.unconfirmedTransactionStore.get(transactionId);
  }

  @Override
  public Transaction.Builder newTransactionBuilder(byte[] senderPublicKey, long amountNQT, long feeNQT, short deadline, Attachment attachment) {
    byte version = (byte) getTransactionVersion(dp.blockchain.getHeight());
    int timestamp = dp.timeService.getEpochTime();
    Transaction.Builder builder = new Transaction.Builder(dp, version, senderPublicKey, amountNQT, feeNQT, timestamp, deadline, (Attachment.AbstractAttachment)attachment);
    if (version > 0) {
      Block ecBlock = dp.economicClustering.getECBlock(timestamp);
      builder.ecBlockHeight(ecBlock.getHeight());
      builder.ecBlockId(ecBlock.getId());
    }
    return builder;
  }

  @Override
  public Integer broadcast(Transaction transaction) throws BurstException.ValidationException {
    if (! transaction.verifySignature()) {
      throw new BurstException.NotValidException("Transaction signature verification failed");
    }
    List<Transaction> processedTransactions;
    if (dp.dbs.getTransactionDb().hasTransaction(transaction.getId())) {
      if (logger.isInfoEnabled()) {
        logger.info("Transaction {} already in blockchain, will not broadcast again", transaction.getStringId());
      }
      return null;
    }

    if (dp.unconfirmedTransactionStore.exists(transaction.getId())) {
      if (logger.isInfoEnabled()) {
        logger.info("Transaction {} already in unconfirmed pool, will not broadcast again", transaction.getStringId());
      }
      return null;
    }

    processedTransactions = processTransactions(Collections.singleton(transaction), null);

    if(! processedTransactions.isEmpty()) {
      return broadcastToPeers(true);
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug("Could not accept new transaction {}", transaction.getStringId());
      }
      throw new BurstException.NotValidException("Invalid transaction " + transaction.getStringId());
    }
  }

  @Override
  public void processPeerTransactions(JsonObject request, Peer peer) throws BurstException.ValidationException {
    JsonArray transactionsData = JSON.getAsJsonArray(request.get("transactions"));
    List<Transaction> processedTransactions = processPeerTransactions(transactionsData, peer);

    if(! processedTransactions.isEmpty()) {
      broadcastToPeers(false);
    }
  }

  @Override
  public Transaction parseTransaction(byte[] bytes) throws BurstException.ValidationException {
    return Transaction.parseTransaction(dp, bytes);
  }

  @Override
  public Transaction parseTransaction(JsonObject transactionData) throws BurstException.NotValidException {
    return Transaction.parseTransaction(dp, transactionData, dp.blockchain.getHeight());
  }
    
  @Override
  public void clearUnconfirmedTransactions() {
    synchronized (unconfirmedTransactionsSyncObj) {
      List<Transaction> removed;
      try {
        Db.beginTransaction();
        removed = dp.unconfirmedTransactionStore.getAll();
        dp.accountService.flushAccountTable();
        dp.unconfirmedTransactionStore.clear();
        Db.commitTransaction();
      } catch (Exception e) {
        logger.error(e.toString(), e);
        Db.rollbackTransaction();
        throw e;
      } finally {
        Db.endTransaction();
      }

      transactionListeners.accept(removed, Event.REMOVED_UNCONFIRMED_TRANSACTIONS);
    }
  }

  @Override
  public void requeueAllUnconfirmedTransactions() {
    synchronized (unconfirmedTransactionsSyncObj) {
      dp.unconfirmedTransactionStore.resetAccountBalances();
    }
  }

  @Override
  public int getTransactionVersion(int previousBlockHeight) {
    return dp.fluxCapacitor.getValue(FluxValues.DIGITAL_GOODS_STORE, previousBlockHeight) ? 1 : 0;
  }

  // Watch: This is not really clean
  @Override
  public void processLater(Collection<? extends Transaction> transactions) {
    for ( Transaction transaction : transactions ) {
      try {
        dp.unconfirmedTransactionStore.put(transaction, null);
      }
      catch ( BurstException.ValidationException e ) {
        logger.debug("Discarding invalid transaction in for later processing: " + JSON.toJsonString(transaction.getJsonObject()), e);
      }
    }
  }

  private List<Transaction> processPeerTransactions(JsonArray transactionsData, Peer peer) throws BurstException.ValidationException {
	  if (dp.blockchain.getLastBlock().getTimestamp() < dp.timeService.getEpochTime() - 60 * 1440 && ! testUnconfirmedTransactions) {
      return new ArrayList<>();
    }
    if (dp.blockchain.getHeight() <= Constants.NQT_BLOCK) {
      return new ArrayList<>();
    }
    List<Transaction> transactions = new ArrayList<>();
    for (JsonElement transactionData : transactionsData) {
      try {
        Transaction transaction = parseTransaction(JSON.getAsJsonObject(transactionData));
        dp.transactionService.validate(transaction);
        if(!dp.economicClustering.verifyFork(transaction)) {
          continue;
        }
        transactions.add(transaction);
      } catch (BurstException.NotCurrentlyValidException ignore) {
      } catch (BurstException.NotValidException e) {
        if (logger.isDebugEnabled()) {
          logger.debug("Invalid transaction from peer: {}", JSON.toJsonString(transactionData));
        }
        throw e;
      }
    }
    return processTransactions(transactions, peer);
  }

  private List<Transaction> processTransactions(Collection<Transaction> transactions, Peer peer) throws BurstException.ValidationException {
    synchronized (unconfirmedTransactionsSyncObj) {
      if (transactions.isEmpty()) {
        return Collections.emptyList();
      }

      List<Transaction> addedUnconfirmedTransactions = new ArrayList<>();

      for (Transaction transaction : transactions) {

        try {
          int curTime = dp.timeService.getEpochTime();
          if (transaction.getTimestamp() > curTime + 15 || transaction.getExpiration() < curTime
              || transaction.getDeadline() > 1440) {
            continue;
          }

          Db.beginTransaction();
          if (dp.blockchain.getHeight() < Constants.NQT_BLOCK) {
            break; // not ready to process transactions
          }
          try {
            if (dp.dbs.getTransactionDb().hasTransaction(transaction.getId()) || dp.unconfirmedTransactionStore.exists(transaction.getId())) {
              dp.unconfirmedTransactionStore.markFingerPrintsOf(peer, Collections.singletonList(transaction));
            } else if (!(transaction.verifySignature() && dp.transactionService.verifyPublicKey(transaction))) {
              if (dp.accountService.getAccount(transaction.getSenderId()) != null && logger.isDebugEnabled()) {
                logger.debug("Transaction {} failed to verify", JSON.toJsonString(transaction.getJsonObject()));
              }
            } else if (dp.unconfirmedTransactionStore.put(transaction, peer)) {
              addedUnconfirmedTransactions.add(transaction);
            }
            Db.commitTransaction();
          } catch (Exception e) {
            Db.rollbackTransaction();
            throw e;
          } finally {
            Db.endTransaction();
          }
        } catch (RuntimeException e) {
          logger.info("Error processing transaction", e);
        }
      }

      if (!addedUnconfirmedTransactions.isEmpty()) {
        transactionListeners.accept(addedUnconfirmedTransactions, Event.ADDED_UNCONFIRMED_TRANSACTIONS);
      }

      return addedUnconfirmedTransactions;
    }
  }

  private int broadcastToPeers(boolean toAll) {
    List<? extends Peer> peersToSendTo = toAll ? Peers.getActivePeers().stream().limit(100).collect(Collectors.toList()) : Peers.getAllActivePriorityPlusSomeExtraPeers();

    logger.trace("Queueing up {} Peers for feeding", peersToSendTo.size());

    for(Peer p: peersToSendTo) {
      Peers.feedingTime(p, foodDispenser, doneFeedingLog);
    }

    return peersToSendTo.size();
  }

  public void revalidateUnconfirmedTransactions() {
    final List<Transaction> invalidTransactions = new ArrayList<>();

    for(Transaction t: dp.unconfirmedTransactionStore.getAll()) {
      try {
        dp.transactionService.validate(t);
      } catch (ValidationException e) {
        invalidTransactions.add(t);
      }
    }

    for(Transaction t:invalidTransactions) {
      dp.unconfirmedTransactionStore.remove(t);
    }
  }

  public void removeForgedTransactions(List<Transaction> transactions) {
    dp.unconfirmedTransactionStore.removeForgedTransactions(transactions);
  }
}
