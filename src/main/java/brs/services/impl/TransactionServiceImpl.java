package brs.services.impl;

import brs.*;
import brs.services.AccountService;
import brs.services.TransactionService;

public class TransactionServiceImpl implements TransactionService {
  private final DependencyProvider dp;

  public TransactionServiceImpl(DependencyProvider dp) {
    this.dp = dp;
  }

  @Override
  public boolean verifyPublicKey(Transaction transaction) {
    Account account = dp.accountService.getAccount(transaction.getSenderId());
    if (account == null) {
      return false;
    }
    if (transaction.getSignature() == null) {
      return false;
    }
    return account.setOrVerify(dp, transaction.getSenderPublicKey(), transaction.getHeight());
  }

  @Override
  public void validate(Transaction transaction) throws BurstException.ValidationException {
    for (Appendix.AbstractAppendix appendage : transaction.getAppendages()) {
      appendage.validate(transaction);
    }
    long minimumFeeNQT = transaction.getType().minimumFeeNQT(dp.blockchain.getHeight(), transaction.getAppendagesSize());
    if (transaction.getFeeNQT() < minimumFeeNQT) {
      throw new BurstException.NotCurrentlyValidException(String.format("Transaction fee %d less than minimum fee %d at height %d",
          transaction.getFeeNQT(), minimumFeeNQT, dp.blockchain.getHeight()));
    }
  }

  @Override
  public boolean applyUnconfirmed(Transaction transaction) {
    Account senderAccount = dp.accountService.getAccount(transaction.getSenderId());
    return senderAccount != null && transaction.getType().applyUnconfirmed(transaction, senderAccount);
  }

  @Override
  public void apply(Transaction transaction) {
    Account senderAccount = dp.accountService.getAccount(transaction.getSenderId());
    senderAccount.apply(dp, transaction.getSenderPublicKey(), transaction.getHeight());
    Account recipientAccount = dp.accountService.getOrAddAccount(transaction.getRecipientId());
    for (Appendix.AbstractAppendix appendage : transaction.getAppendages()) {
      appendage.apply(transaction, senderAccount, recipientAccount);
    }
  }

  @Override
  public void undoUnconfirmed(Transaction transaction) {
    final Account senderAccount = dp.accountService.getAccount(transaction.getSenderId());
    transaction.getType().undoUnconfirmed(transaction, senderAccount);
  }

}
