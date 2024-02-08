package brs.services;

import brs.SignumException;
import brs.Transaction;

public interface TransactionService {

  boolean verifyPublicKey(Transaction transaction);

  void validate(Transaction transaction) throws SignumException.ValidationException;
  
  void startNewBlock();

  boolean applyUnconfirmed(Transaction transaction);

  void apply(Transaction transaction);

  void undoUnconfirmed(Transaction transaction);
}
