package brs.db.store;

import brs.Account;
import brs.Asset;
import brs.db.SignumKey;
import brs.db.VersionedBatchEntityTable;
import brs.db.VersionedEntityTable;

import java.util.Collection;

/**
 * Interface for Database operations related to Accounts
 */
public interface AccountStore {

  VersionedBatchEntityTable<Account> getAccountTable();

  VersionedBatchEntityTable<Account.Balance> getAccountBalanceTable();

  long getAllAccountsBalance();

  VersionedEntityTable<Account.RewardRecipientAssignment> getRewardRecipientAssignmentTable();

  SignumKey.LongKeyFactory<Account.RewardRecipientAssignment> getRewardRecipientAssignmentKeyFactory();

  SignumKey.LinkKeyFactory<Account.AccountAsset> getAccountAssetKeyFactory();

  VersionedEntityTable<Account.AccountAsset> getAccountAssetTable();

  int getAssetAccountsCount(Asset asset, long minimumQuantity, boolean ignoreTreasury, boolean unconfirmed);

  long getAssetCirculatingSupply(Asset asset, boolean ignoreTreasury, boolean unconfirmed);

  SignumKey.LongKeyFactory<Account> getAccountKeyFactory();

  SignumKey.LongKeyFactory<Account.Balance> getAccountBalanceKeyFactory();

  Collection<Account.RewardRecipientAssignment> getAccountsWithRewardRecipient(Long recipientId);

  Collection<Account.AccountAsset> getAssets(int from, int to, Long id);

  Account.AccountAsset getAccountAsset(Long accountId, Long assetId);

  Collection<Account.AccountAsset> getAssetAccounts(Asset asset, boolean ignoreTreasury, long minimumQuantity, boolean unconfirmed, int from, int to);

  // returns true iff:
  // this.publicKey is set to null (in which case this.publicKey also gets set to key)
  // or
  // this.publicKey is already set to an array equal to key
  boolean setOrVerify(Account acc, byte[] key, int height);
}
