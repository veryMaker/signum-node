package brs.unconfirmedtransactions;

import brs.BurstException.ValidationException;
import brs.Constants;
import brs.Transaction;
import brs.db.store.AccountStore;
import brs.peer.Peer;
import brs.props.PropertyService;
import brs.props.Props;
import brs.services.TimeService;
import brs.transactionduplicates.TransactionDuplicatesCheckerImpl;
import brs.transactionduplicates.TransactionDuplicationResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnconfirmedTransactionStoreImpl implements UnconfirmedTransactionStore {

  private static final Logger logger = LoggerFactory.getLogger(UnconfirmedTransactionStoreImpl.class);

  private final TimeService timeService;
  private final ReservedBalanceCache reservedBalanceCache;
  private final TransactionDuplicatesCheckerImpl transactionDuplicatesChecker = new TransactionDuplicatesCheckerImpl();

  private HashMap<Transaction, HashSet<Peer>> fingerPrintsOverview = new HashMap<>();

  private final SortedMap<Long, List<Transaction>> internalStore;

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  private int totalSize;
  private final int maxSize;

  private int numberUnconfirmedTransactionsFullHash;
  private final int maxPercentageUnconfirmedTransactionsFullHash;

  final Runnable cleanupExpiredTransactions = new Runnable() {
    @Override
    public void run() {
      synchronized (internalStore) {
        final List<Transaction> expiredTransactions = getAll().stream().filter(t -> timeService.getEpochTime() > t.getExpiration()).collect(Collectors.toList());

        expiredTransactions.stream().forEach(t -> removeTransaction(t));
      }
    }
  };

  public UnconfirmedTransactionStoreImpl(TimeService timeService, PropertyService propertyService, AccountStore accountStore) {
    this.timeService = timeService;

    this.reservedBalanceCache = new ReservedBalanceCache(accountStore);

    this.maxSize = propertyService.getInt(Props.P2P_MAX_UNCONFIRMED_TRANSACTIONS);
    this.totalSize = 0;

    this.maxPercentageUnconfirmedTransactionsFullHash = propertyService.getInt(Props.P2P_MAX_PERCENTAGE_UNCONFIRMED_TRANSACTIONS_FULL_HASH_REFERENCE);
    this.numberUnconfirmedTransactionsFullHash = 0;

    internalStore = new TreeMap<>();

    scheduler.scheduleWithFixedDelay(cleanupExpiredTransactions, 1, 1, TimeUnit.MINUTES);
  }

  @Override
  public boolean put(Transaction transaction, Peer peer) throws ValidationException {
    synchronized (internalStore) {
      if(transactionIsCurrentlyInCache(transaction)) {
        if(peer != null) {
          logger.info("Transaction {}: Added fingerprint of {}", transaction.getId(), peer.getPeerAddress());
          fingerPrintsOverview.get(transaction).add(peer);
        }
      } else {
        if (transactionCanBeAddedToCache(transaction)) {
          final TransactionDuplicationResult duplicationInformation = transactionDuplicatesChecker.removeCheaperDuplicate(transaction);

          if (duplicationInformation.isDuplicate()) {
            final Transaction duplicatedTransaction = duplicationInformation.getTransaction();

            if (duplicatedTransaction != null && duplicatedTransaction != transaction) {
              logger.info("Transaction {}: Adding more expensive duplicate transaction", transaction.getId());
              removeTransaction(duplicationInformation.getTransaction());

              addTransaction(transaction, peer);

              if (totalSize > maxSize) {
                removeCheapestFirstToExpireTransaction();
              }
            } else {
              logger.info("Transaction {}: Will not add a cheaper duplicate UT", transaction.getId());
            }
          } else {
            addTransaction(transaction, peer);
            logger.debug(
                "Cache size: " + totalSize + "/" + maxSize + " Added UT " + transaction.getId() + " " + transaction.getSenderId() + " " + transaction.getAmountNQT() + " " + transaction.getFeeNQT());
            if (totalSize > maxSize) {
              removeCheapestFirstToExpireTransaction();
            }
          }

          return true;
        } else {
          logger.info("Transaction {}: Will not add UT due to duplication, or too full", transaction.getId());
        }
      }

      return false;
    }
  }

  private boolean transactionCanBeAddedToCache(Transaction transaction) {
    return transactionIsCurrentlyValid(transaction)
        && ! cacheFullAndTransactionCheaperThanAllTheRest(transaction)
        && ! tooManyTransactionsWithReferencedFullHash(transaction)
        && ! tooManyTransactionsForSlotSize(transaction);
  }

  private boolean tooManyTransactionsForSlotSize(Transaction transaction) {
    final long slotHeight = this.amountSlotForTransaction(transaction);

    return this.internalStore.containsKey(slotHeight) && this.internalStore.get(slotHeight).size() == slotHeight * 360;
  }

  private boolean tooManyTransactionsWithReferencedFullHash(Transaction transaction) {
    return ! StringUtils.isEmpty(transaction.getReferencedTransactionFullHash()) && maxPercentageUnconfirmedTransactionsFullHash <= (((numberUnconfirmedTransactionsFullHash + 1) * 100) / maxSize);
  }

  private boolean cacheFullAndTransactionCheaperThanAllTheRest(Transaction transaction) {
    return totalSize == maxSize && internalStore.firstKey() > amountSlotForTransaction(transaction);
  }

  @Override
  public Transaction get(Long transactionId) {
    synchronized (internalStore) {
      for (List<Transaction> amountSlot : internalStore.values()) {
        for (Transaction t : amountSlot) {
          if (t.getId() == transactionId) {
            return t;
          }
        }
      }

      return null;
    }
  }

  @Override
  public boolean exists(Long transactionId) {
    synchronized (internalStore) {
      return get(transactionId) != null;
    }
  }

  @Override
  public List<Transaction> getAll() {
    synchronized (internalStore) {
      final ArrayList<Transaction> flatTransactionList = new ArrayList<>();

      for (List<Transaction> amountSlot : internalStore.values()) {
        flatTransactionList.addAll(amountSlot);
      }

      return flatTransactionList;
    }
  }

  @Override
  public List<Transaction> getAllFor(Peer peer) {
    synchronized (internalStore) {
      final List<Transaction> untouchedTransactions = fingerPrintsOverview.entrySet().stream()
          .filter(e -> ! e.getValue().contains(peer))
          .map(e -> e.getKey()).collect(Collectors.toList());

      final ArrayList<Transaction> resultList = new ArrayList<>();

      long roomLeft = 175000;

      for (Transaction t : untouchedTransactions) {
        roomLeft -= t.getSize();

        if (roomLeft > 0) {
          resultList.add(t);
        } else {
          break;
        }
      }

      return resultList;
    }
  }

  @Override
  public void forEach(Consumer<Transaction> consumer) {
    synchronized (internalStore) {
      for (List<Transaction> amountSlot : internalStore.values()) {
        amountSlot.stream().forEach(consumer);
      }
    }
  }

  @Override
  public void remove(Transaction transaction) {
    synchronized (internalStore) {
      logger.debug("Removing " + transaction.getId());
      if (exists(transaction.getId())) {
        removeTransaction(transaction);
      }
    }
  }

  @Override
  public void clear() {
    synchronized (internalStore) {
      logger.debug("Clearing UTStore");
      totalSize = 0;
      internalStore.clear();
      reservedBalanceCache.clear();
      transactionDuplicatesChecker.clear();
    }
  }

  @Override
  public List<Transaction> resetAccountBalances() {
    synchronized (internalStore) {
      return reservedBalanceCache.rebuild(getAll());
    }
  }

  @Override
  public void markFingerPrintsOf(Peer peer, List<Transaction> transactions) {
    synchronized (internalStore) {
      for (Transaction transaction : transactions) {
        fingerPrintsOverview.get(transaction).add(peer);
      }
    }
  }

  private boolean transactionIsCurrentlyInCache(Transaction transaction) {
    return fingerPrintsOverview.containsKey(transaction);
  }

  private void addTransaction(Transaction transaction, Peer peer) throws ValidationException {
    this.reservedBalanceCache.reserveBalanceAndPut(transaction);

    final List<Transaction> slot = getOrCreateAmountSlotForTransaction(transaction);
    slot.add(transaction);
    totalSize++;

    fingerPrintsOverview.put(transaction, new HashSet<>());

    if(peer != null) {
      fingerPrintsOverview.get(transaction).add(peer);
    }

    logger.info("Adding Transaction {} from Peer {}", transaction.getId(), (peer == null ? "Ourself" : peer.getPeerAddress()));

    if(! StringUtils.isEmpty(transaction.getReferencedTransactionFullHash())) {
      numberUnconfirmedTransactionsFullHash++;
    }
  }

  private List<Transaction> getOrCreateAmountSlotForTransaction(Transaction transaction) {
    final long amountSlotNumber = amountSlotForTransaction(transaction);

    if (!this.internalStore.containsKey(amountSlotNumber)) {
      this.internalStore.put(amountSlotNumber, new ArrayList<>());
    }

    return this.internalStore.get(amountSlotNumber);
  }


  private long amountSlotForTransaction(Transaction transaction) {
    return transaction.getFeeNQT() / Constants.FEE_QUANT;
  }

  private void removeCheapestFirstToExpireTransaction() {
    this.internalStore.get(this.internalStore.firstKey()).stream()
        //.map(UnconfirmedTransactionTiming::getTransaction)
        .sorted(Comparator.comparingLong(Transaction::getFeeNQT).thenComparing(Transaction::getExpiration).thenComparing(Transaction::getId))
        .findFirst().ifPresent(t -> removeTransaction(t));
  }

  private boolean transactionIsCurrentlyValid(Transaction transaction) {
    if(timeService.getEpochTime() < transaction.getExpiration()) {
      return true;
    } else {
      logger.debug("Transaction {} past expiration: {}", transaction.getId(), transaction.getExpiration());
      return false;
    }
  }

  private void removeTransaction(Transaction transaction) {
    final long amountSlotNumber = amountSlotForTransaction(transaction);

    final List<Transaction> amountSlot = internalStore.get(amountSlotNumber);

    final Iterator<Transaction> transactionSlotIterator = amountSlot.iterator();

    fingerPrintsOverview.remove(transaction);

    while (transactionSlotIterator.hasNext()) {
      final Transaction utt = transactionSlotIterator.next();
      if (utt.getId() == transaction.getId()) {
        transactionSlotIterator.remove();
        transactionDuplicatesChecker.removeTransaction(transaction);
        this.reservedBalanceCache.refundBalance(transaction);
        totalSize--;

        if(! StringUtils.isEmpty(transaction.getReferencedTransactionFullHash())) {
          numberUnconfirmedTransactionsFullHash--;
        }
        return;
      }
    }

    if (amountSlot.isEmpty()) {
      this.internalStore.remove(amountSlotNumber);
    }
  }

}
