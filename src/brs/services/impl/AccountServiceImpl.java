package brs.services.impl;

import brs.Account;
import brs.Account.AccountAsset;
import brs.Account.Event;
import brs.Account.RewardRecipientAssignment;
import brs.AssetTransfer;
import brs.Signum;
import brs.Constants;
import brs.crypto.Crypto;
import brs.db.SignumKey;
import brs.db.SignumKey.LinkKeyFactory;
import brs.db.SignumKey.LongKeyFactory;
import brs.db.VersionedBatchEntityTable;
import brs.db.VersionedEntityTable;
import brs.db.store.AccountStore;
import brs.db.store.AssetTransferStore;
import brs.services.AccountService;
import brs.util.CollectionWithIndex;
import brs.util.Convert;
import brs.util.Listener;
import brs.util.Listeners;

import java.util.Arrays;
import java.util.Collection;

import static brs.schema.Tables.ACCOUNT;

public class AccountServiceImpl implements AccountService {

  private final AccountStore accountStore;
  private final VersionedBatchEntityTable<Account> accountTable;
  private final VersionedBatchEntityTable<Account.Balance> accountBalanceTable;
  private final LongKeyFactory<Account> accountSignumKeyFactory;
  private final LongKeyFactory<Account.Balance> accountBalanceSignumKeyFactory;
  private final VersionedEntityTable<AccountAsset> accountAssetTable;
  private final LinkKeyFactory<AccountAsset> accountAssetKeyFactory;
  private final VersionedEntityTable<RewardRecipientAssignment> rewardRecipientAssignmentTable;
  private final LongKeyFactory<RewardRecipientAssignment> rewardRecipientAssignmentKeyFactory;

  private final AssetTransferStore assetTransferStore;

  private final Listeners<Account, Event> listeners = new Listeners<>();
  private final Listeners<AccountAsset, Event> assetListeners = new Listeners<>();

  public AccountServiceImpl(AccountStore accountStore, AssetTransferStore assetTransferStore) {
    this.accountStore = accountStore;
    this.accountTable = accountStore.getAccountTable();
    this.accountBalanceTable = accountStore.getAccountBalanceTable();
    this.accountSignumKeyFactory = accountStore.getAccountKeyFactory();
    this.accountBalanceSignumKeyFactory = accountStore.getAccountBalanceKeyFactory();
    this.assetTransferStore = assetTransferStore;
    this.accountAssetTable = accountStore.getAccountAssetTable();
    this.accountAssetKeyFactory = accountStore.getAccountAssetKeyFactory();
    this.rewardRecipientAssignmentTable = accountStore.getRewardRecipientAssignmentTable();
    this.rewardRecipientAssignmentKeyFactory = accountStore.getRewardRecipientAssignmentKeyFactory();
  }

  @Override
  public boolean addListener(Listener<Account> listener, Event eventType) {
    return listeners.addListener(listener, eventType);
  }

  @Override
  public boolean addAssetListener(Listener<AccountAsset> listener, Event eventType) {
    return assetListeners.addListener(listener, eventType);
  }

  @Override
  public Account getAccount(long id) {
    return id == 0 ? null : accountTable.get(accountSignumKeyFactory.newKey(id));
  }

  @Override
  public Account.Balance getAccountBalance(long id) {
    Account.Balance account = accountBalanceTable.get(accountBalanceSignumKeyFactory.newKey(id));
    if(account == null){
      account = new Account.Balance(id);
    }
    return account;
  }

  @Override
  public Account getNullAccount() {
    return accountTable.get(accountSignumKeyFactory.newKey(0L));
  }

  @Override
  public Account getAccount(long id, int height) {
    return id == 0 ? null : accountTable.get(accountSignumKeyFactory.newKey(id), height);
  }

  @Override
  public Account getAccount(byte[] publicKey) {
    final Account account = accountTable.get(accountSignumKeyFactory.newKey(getId(publicKey)));

    if (account == null) {
      return null;
    }

    if (account.getPublicKey() == null || Arrays.equals(account.getPublicKey(), publicKey)) {
      return account;
    }

    throw new RuntimeException("DUPLICATE KEY for account " + Convert.toUnsignedLong(account.getId())
        + " existing key " + Convert.toHexString(account.getPublicKey()) + " new key " + Convert.toHexString(publicKey));
  }

  @Override
  public CollectionWithIndex<AssetTransfer> getAssetTransfers(long accountId, int from, int to) {
    return new CollectionWithIndex<AssetTransfer>(assetTransferStore.getAccountAssetTransfers(accountId, from, to), from, to);
  }

  @Override
  public CollectionWithIndex<AccountAsset> getAssets(long accountId, int from, int to) {
    return new CollectionWithIndex<AccountAsset>(accountStore.getAssets(from, to, accountId), from, to);
  }

  @Override
  public Collection<RewardRecipientAssignment> getAccountsWithRewardRecipient(Long recipientId) {
    return accountStore.getAccountsWithRewardRecipient(recipientId);
  }

  @Override
  public Collection<Account> getAccountsWithName(String name) {
    return accountTable.getManyBy(ACCOUNT.NAME.equalIgnoreCase(name), 0, -1);
  }

  @Override
  public CollectionWithIndex<Account> getAllAccounts(int from, int to) {
    return new CollectionWithIndex<Account>(accountTable.getAll(from, to), from, to);
  }

  @Override
  public long getAllAccountsBalance() {
    return accountStore.getAllAccountsBalance();
  }

  @Override
  public Account getOrAddAccount(long id) {
    Account account = accountTable.get(accountSignumKeyFactory.newKey(id));
    if (account == null) {
      account = new Account(id);
      accountTable.insert(account);
    }
    return account;
  }

  public static long getId(byte[] publicKey) {
    byte[] publicKeyHash = Crypto.sha256().digest(publicKey);
    return Convert.fullHashToId(publicKeyHash);
  }

  @Override
  public void flushAccountTable() {
    accountTable.finish();
  }

  @Override
  public int getCount() {
    return accountTable.getCount();
  }

  @Override
  public int getBatchedAccountsCount() {
    return accountTable.getBatch().size();
  }

  @Override
  public void addToForgedBalanceNQT(Account account, long amountNQT) {
    if (amountNQT == 0) {
      return;
    }
    Account.Balance accountBalance = getAccountBalance(account.getId());
    accountBalance.setForgedBalanceNqt(Convert.safeAdd(accountBalance.getForgedBalanceNqt(), amountNQT));
    accountBalanceTable.insert(accountBalance);
  }

  @Override
  public void setAccountInfo(Account account, String name, String description) {
    account.setName(Convert.emptyToNull(name.trim()));
    account.setDescription(Convert.emptyToNull(description.trim()));
    accountTable.insert(account);
  }

  @Override
  public void addToAssetBalanceQNT(Account account, long assetId, long quantityQNT) {
    if (quantityQNT == 0) {
      return;
    }
    AccountAsset accountAsset;

    SignumKey newKey = accountAssetKeyFactory.newKey(account.getId(), assetId);
    accountAsset = accountAssetTable.get(newKey);
    long assetBalance = accountAsset == null ? 0 : accountAsset.getQuantityQnt();
    assetBalance = Convert.safeAdd(assetBalance, quantityQNT);
    if (accountAsset == null) {
      accountAsset = new AccountAsset(newKey, account.getId(), assetId, assetBalance, 0);
    } else {
      accountAsset.setQuantityQnt(assetBalance);
    }
    saveAccountAsset(accountAsset);
    listeners.notify(account, Event.ASSET_BALANCE);
    assetListeners.notify(accountAsset, Event.ASSET_BALANCE);
  }

  @Override
  public void addToUnconfirmedAssetBalanceQNT(Account account, long assetId, long quantityQNT) {
    if (quantityQNT == 0) {
      return;
    }
    AccountAsset accountAsset;
    SignumKey newKey = accountAssetKeyFactory.newKey(account.getId(), assetId);
    accountAsset = accountAssetTable.get(newKey);
    long unconfirmedAssetBalance = accountAsset == null ? 0 : accountAsset.getUnconfirmedQuantityQnt();
    unconfirmedAssetBalance = Convert.safeAdd(unconfirmedAssetBalance, quantityQNT);
    if (accountAsset == null) {
      accountAsset = new AccountAsset(newKey, account.getId(), assetId, 0, unconfirmedAssetBalance);
    } else {
      accountAsset.setUnconfirmedQuantityQnt(unconfirmedAssetBalance);
    }
    saveAccountAsset(accountAsset);
    listeners.notify(account, Event.UNCONFIRMED_ASSET_BALANCE);
    assetListeners.notify(accountAsset, Event.UNCONFIRMED_ASSET_BALANCE);
  }

  @Override
  public void addToAssetAndUnconfirmedAssetBalanceQNT(Account account, long assetId, long quantityQNT) {
    if (quantityQNT == 0) {
      return;
    }
    AccountAsset accountAsset;
    SignumKey newKey = accountAssetKeyFactory.newKey(account.getId(), assetId);
    accountAsset = accountAssetTable.get(newKey);
    long assetBalance = accountAsset == null ? 0 : accountAsset.getQuantityQnt();
    assetBalance = Convert.safeAdd(assetBalance, quantityQNT);
    long unconfirmedAssetBalance = accountAsset == null ? 0 : accountAsset.getUnconfirmedQuantityQnt();
    unconfirmedAssetBalance = Convert.safeAdd(unconfirmedAssetBalance, quantityQNT);
    if (accountAsset == null) {
      accountAsset = new AccountAsset(newKey, account.getId(), assetId, assetBalance, unconfirmedAssetBalance);
    } else {
      accountAsset.setQuantityQnt(assetBalance);
      accountAsset.setUnconfirmedQuantityQnt(unconfirmedAssetBalance);
    }
    saveAccountAsset(accountAsset);
    listeners.notify(account, Event.ASSET_BALANCE);
    listeners.notify(account, Event.UNCONFIRMED_ASSET_BALANCE);
    assetListeners.notify(accountAsset, Event.ASSET_BALANCE);
    assetListeners.notify(accountAsset, Event.UNCONFIRMED_ASSET_BALANCE);
  }

  @Override
  public void addToBalanceNQT(Account account, long amountNQT) {
    if (amountNQT == 0) {
      return;
    }
    Account.Balance accountBalance = getAccountBalance(account.getId());

    accountBalance.setBalanceNqt(Convert.safeAdd(accountBalance.getBalanceNqt(), amountNQT));
    accountBalance.checkBalance();
    accountBalanceTable.insert(accountBalance);
    listeners.notify(account, Event.BALANCE);
  }

  @Override
  public void addToUnconfirmedBalanceNQT(Account account, long amountNQT) {
    if (amountNQT == 0) {
      return;
    }
    Account.Balance accountBalance = getAccountBalance(account.getId());

    accountBalance.setUnconfirmedBalanceNqt(Convert.safeAdd(accountBalance.getUnconfirmedBalanceNqt(), amountNQT));
    accountBalance.checkBalance();
    accountBalanceTable.insert(accountBalance);
    listeners.notify(account, Event.UNCONFIRMED_BALANCE);
  }

  @Override
  public void addToBalanceAndUnconfirmedBalanceNQT(Account account, long amountNQT) {
    if (amountNQT == 0) {
      return;
    }
    Account.Balance accountBalance = getAccountBalance(account.getId());

    accountBalance.setBalanceNqt(Convert.safeAdd(accountBalance.getBalanceNqt(), amountNQT));
    accountBalance.setUnconfirmedBalanceNqt(Convert.safeAdd(accountBalance.getUnconfirmedBalanceNqt(), amountNQT));
    accountBalance.checkBalance();
    accountBalanceTable.insert(accountBalance);
    listeners.notify(account, Event.BALANCE);
    listeners.notify(account, Event.UNCONFIRMED_BALANCE);
  }

  @Override
  public RewardRecipientAssignment getRewardRecipientAssignment(Account account) {
    return getRewardRecipientAssignment(account.getId());
  }

  private RewardRecipientAssignment getRewardRecipientAssignment(Long id) {
    return rewardRecipientAssignmentTable.get(rewardRecipientAssignmentKeyFactory.newKey(id));
  }

  @Override
  public void setRewardRecipientAssignment(Account account, Long recipient) {
    int currentHeight = Signum.getBlockchain().getLastBlock().getHeight();
    RewardRecipientAssignment assignment = getRewardRecipientAssignment(account.getId());
    if (assignment == null) {
      SignumKey signumKey = rewardRecipientAssignmentKeyFactory.newKey(account.getId());
      assignment = new RewardRecipientAssignment(account.getId(), account.getId(), recipient, (int) (currentHeight + Constants.SIGNA_REWARD_RECIPIENT_ASSIGNMENT_WAIT_TIME), signumKey);
    } else {
      assignment.setRecipient(recipient, (int) (currentHeight + Constants.SIGNA_REWARD_RECIPIENT_ASSIGNMENT_WAIT_TIME));
    }
    rewardRecipientAssignmentTable.insert(assignment);
  }

  @Override
  public long getUnconfirmedAssetBalanceQNT(Account account, long assetId) {
    SignumKey newKey = Signum.getStores().getAccountStore().getAccountAssetKeyFactory().newKey(account.getId(), assetId);
    AccountAsset accountAsset = accountAssetTable.get(newKey);
    return accountAsset == null ? 0 : accountAsset.getUnconfirmedQuantityQnt();
  }


  private void saveAccountAsset(AccountAsset accountAsset) {
    accountAsset.checkBalance();
    if (accountAsset.getQuantityQnt() > 0 || accountAsset.getUnconfirmedQuantityQnt() > 0) {
      accountAssetTable.insert(accountAsset);
    } else {
      accountAssetTable.delete(accountAsset);
    }
  }
}
