package brs.unconfirmedtransactions;

import brs.BurstException;
import brs.Transaction;
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
}
