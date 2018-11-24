package brs.unconfirmedtransactions;

import brs.BurstException;
import brs.Transaction;
import java.util.List;
import java.util.function.Consumer;

public interface UnconfirmedTransactionStore {

  void put(Transaction transaction) throws BurstException.ValidationException;

  Transaction get(Long transactionId);

  boolean exists(Long transactionId);

  TimedUnconfirmedTransactionOverview getAll(long maxAmount);

  TimedUnconfirmedTransactionOverview getAllSince(long timestampInMillis, long maxAmount);

  void forEach(Consumer<Transaction> consumer);

  void remove(Transaction transaction);

  void clear();


  /**
   * Review which transactions are still eligible to stay
   * @return The list of removed transactions
   */
  List<Transaction> resetAccountBalances();

}
