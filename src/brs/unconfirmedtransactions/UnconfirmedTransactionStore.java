package brs.unconfirmedtransactions;

import brs.BurstException;
import brs.Transaction;
import brs.peer.Peer;
import java.util.List;
import java.util.function.Consumer;

public interface UnconfirmedTransactionStore {

  void put(Transaction transaction, Peer peer) throws BurstException.ValidationException;

  Transaction get(Long transactionId);

  boolean exists(Long transactionId);

  List<Transaction> getAll();

  List<Transaction> getAllFor(Peer peer);

  ///TimedUnconfirmedTransactionOverview getAllSince(long timestampInMillis, long maxAmount);

  void forEach(Consumer<Transaction> consumer);

  void remove(Transaction transaction);

  void clear();


  /**
   * Review which transactions are still eligible to stay
   * @return The list of removed transactions
   */
  List<Transaction> resetAccountBalances();

  void markFingerPrintsOf(Peer peer, List<Transaction> transactions);

}
