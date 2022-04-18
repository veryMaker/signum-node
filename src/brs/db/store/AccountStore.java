package brs.db.store;

import brs.Account;
import brs.Asset;
import brs.db.BurstKey;
import brs.db.VersionedBatchEntityTable;
import brs.db.VersionedEntityTable;

import java.util.Collection;

/**
 * Interface for Database operations related to Accounts
 */
public interface AccountStore {

  VersionedBatchEntityTable<Account> getAccountTable();
  
  long getAllAccountsBalance();

  VersionedEntityTable<Account.RewardRecipientAssignment> getRewardRecipientAssignmentTable();

  BurstKey.LongKeyFactory<Account.RewardRecipientAssignment> getRewardRecipientAssignmentKeyFactory();

  BurstKey.LinkKeyFactory<Account.AccountAsset> getAccountAssetKeyFactory();

  VersionedEntityTable<Account.AccountAsset> getAccountAssetTable();

  int getAssetAccountsCount(Asset asset, long minimumQuantity, boolean ignoreTreasury);

  long getAssetCirculatingSupply(Asset asset, boolean ignoreTreasury);

  BurstKey.LongKeyFactory<Account> getAccountKeyFactory();

  Collection<Account.RewardRecipientAssignment> getAccountsWithRewardRecipient(Long recipientId);

  Collection<Account.AccountAsset> getAssets(int from, int to, Long id);
  
  Account.AccountAsset getAccountAsset(Long accountId, Long assetId);

  Collection<Account.AccountAsset> getAssetAccounts(Asset asset, boolean ignoreTreasury, long minimumQuantity, int from, int to);

  // returns true iff:
  // this.publicKey is set to null (in which case this.publicKey also gets set to key)
  // or
  // this.publicKey is already set to an array equal to key
  boolean setOrVerify(Account acc, byte[] key, int height);
}
