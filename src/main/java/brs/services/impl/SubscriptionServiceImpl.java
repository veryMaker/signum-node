package brs.services.impl;

import brs.*;
import brs.BurstException.NotValidException;
import brs.db.BurstKey;
import brs.db.BurstKey.LongKeyFactory;
import brs.db.TransactionDb;
import brs.db.VersionedEntityTable;
import brs.db.store.SubscriptionStore;
import brs.services.AccountService;
import brs.services.AliasService;
import brs.services.SubscriptionService;
import brs.util.Convert;

import java.util.*;

public class SubscriptionServiceImpl implements SubscriptionService {

  private final DependencyProvider dp;

  private final VersionedEntityTable<Subscription> subscriptionTable;
  private final LongKeyFactory<Subscription> subscriptionDbKeyFactory;

  private static final List<Transaction> paymentTransactions = new ArrayList<>();
  private static final List<Subscription> appliedSubscriptions = new ArrayList<>();
  private static final Set<Long> removeSubscriptions = new HashSet<>();

  public SubscriptionServiceImpl(DependencyProvider dp) {
    this.dp = dp;
    this.subscriptionTable = dp.subscriptionStore.getSubscriptionTable();
    this.subscriptionDbKeyFactory = dp.subscriptionStore.getSubscriptionDbKeyFactory();
  }

  @Override
  public Subscription getSubscription(Long id) {
    return subscriptionTable.get(subscriptionDbKeyFactory.newKey(id));
  }

  @Override
  public Collection<Subscription> getSubscriptionsByParticipant(Long accountId) {
    return dp.subscriptionStore.getSubscriptionsByParticipant(accountId);
  }

  @Override
  public Collection<Subscription> getSubscriptionsToId(Long accountId) {
    return dp.subscriptionStore.getSubscriptionsToId(accountId);
  }

  @Override
  public void addSubscription(Account sender, Account recipient, Long id, Long amountNQT, int startTimestamp, int frequency) {
    final BurstKey dbKey = subscriptionDbKeyFactory.newKey(id);
    final Subscription subscription = new Subscription(sender.getId(), recipient.getId(), id, amountNQT, frequency, startTimestamp + frequency, dbKey);

    subscriptionTable.insert(subscription);
  }

  @Override
  public boolean isEnabled() {
    if (dp.blockchain.getLastBlock().getHeight() >= Constants.BURST_SUBSCRIPTION_START_BLOCK) {
      return true;
    }

    final Alias subscriptionEnabled = dp.aliasService.getAlias("featuresubscription");
    return subscriptionEnabled != null && subscriptionEnabled.getAliasURI().equals("enabled");
  }

  @Override
  public void applyConfirmed(Block block, int blockchainHeight) {
    paymentTransactions.clear();
    for (Subscription subscription : appliedSubscriptions) {
      apply(block, blockchainHeight, subscription);
      subscriptionTable.insert(subscription);
    }
    if (!paymentTransactions.isEmpty()) {
      dp.dbs.getTransactionDb().saveTransactions(paymentTransactions);
    }
    removeSubscriptions.forEach(this::removeSubscription);
  }

  private long getFee() {
    return Constants.ONE_BURST;
  }

  @Override
  public void removeSubscription(Long id) {
    Subscription subscription = subscriptionTable.get(subscriptionDbKeyFactory.newKey(id));
    if (subscription != null) {
      subscriptionTable.delete(subscription);
    }
  }

  @Override
  public long calculateFees(int timestamp) {
    long totalFeeNQT = 0;
    List<Subscription> appliedUnconfirmedSubscriptions = new ArrayList<>();
    for (Subscription subscription : dp.subscriptionStore.getUpdateSubscriptions(timestamp)){
      if (removeSubscriptions.contains(subscription.getId())) {
        continue;
      }
      if (applyUnconfirmed(subscription)) {
        appliedUnconfirmedSubscriptions.add(subscription);
      }
    }
    if (! appliedUnconfirmedSubscriptions.isEmpty()) {
      for (Subscription subscription : appliedUnconfirmedSubscriptions) {
        totalFeeNQT = Convert.INSTANCE.safeAdd(totalFeeNQT, getFee());
        undoUnconfirmed(subscription);
      }
    }
    return totalFeeNQT;
  }

  @Override
  public void clearRemovals() {
    removeSubscriptions.clear();
  }

  @Override
  public void addRemoval(Long id) {
    removeSubscriptions.add(id);
  }

  @Override
  public long applyUnconfirmed(int timestamp) {
    appliedSubscriptions.clear();
    long totalFees = 0;
    for (Subscription subscription : dp.subscriptionStore.getUpdateSubscriptions(timestamp)) {
      if (removeSubscriptions.contains(subscription.getId())) {
        continue;
      }
      if (applyUnconfirmed(subscription)) {
        appliedSubscriptions.add(subscription);
        totalFees += getFee();
      } else {
        removeSubscriptions.add(subscription.getId());
      }
    }
    return totalFees;
  }

  private boolean applyUnconfirmed(Subscription subscription) {
    Account sender = dp.accountService.getAccount(subscription.getSenderId());
    long totalAmountNQT = Convert.INSTANCE.safeAdd(subscription.getAmountNQT(), getFee());

    if (sender == null || sender.getUnconfirmedBalanceNQT() < totalAmountNQT) {
      return false;
    }

    dp.accountService.addToUnconfirmedBalanceNQT(sender, -totalAmountNQT);

    return true;
  }

  private void undoUnconfirmed(Subscription subscription) {
    Account sender = dp.accountService.getAccount(subscription.getSenderId());
    long totalAmountNQT = Convert.INSTANCE.safeAdd(subscription.getAmountNQT(), getFee());

    if (sender != null) {
      dp.accountService.addToUnconfirmedBalanceNQT(sender, totalAmountNQT);
    }
  }

  private void apply(Block block, int blockchainHeight, Subscription subscription) {
    Account sender = dp.accountService.getAccount(subscription.getSenderId());
    Account recipient = dp.accountService.getAccount(subscription.getRecipientId());

    long totalAmountNQT = Convert.INSTANCE.safeAdd(subscription.getAmountNQT(), getFee());

    dp.accountService.addToBalanceNQT(sender, -totalAmountNQT);
    dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(recipient, subscription.getAmountNQT());

    Attachment.AbstractAttachment attachment = new Attachment.AdvancedPaymentSubscriptionPayment(subscription.getId(), blockchainHeight);
    Transaction.Builder builder = new Transaction.Builder(dp, (byte) 1,
        sender.getPublicKey(), subscription.getAmountNQT(),
        getFee(),
        subscription.getTimeNext(), (short) 1440, attachment);

    try {
      builder.senderId(subscription.getSenderId())
          .recipientId(subscription.getRecipientId())
          .blockId(block.getId())
          .height(block.getHeight())
          .blockTimestamp(block.getTimestamp())
          .ecBlockHeight(0)
          .ecBlockId(0L);
      Transaction transaction = builder.build();
      if (!dp.dbs.getTransactionDb().hasTransaction(transaction.getId())) {
        paymentTransactions.add(transaction);
      }
    } catch (NotValidException e) {
      throw new RuntimeException("Failed to build subscription payment transaction", e);
    }

    subscription.timeNextGetAndAdd(subscription.getFrequency());
  }

}
