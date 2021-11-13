package brs.assetexchange;

import brs.Account.AccountAsset;
import brs.Asset;
import brs.db.store.AccountStore;

import java.util.Collection;

class AssetAccountServiceImpl {

  private final AccountStore accountStore;

  public AssetAccountServiceImpl(AccountStore accountStore) {
    this.accountStore = accountStore;
  }

  public Collection<AccountAsset> getAssetAccounts(long assetId, int from, int to) {
    return accountStore.getAssetAccounts(assetId, from, to);
  }

  public Collection<AccountAsset> getAssetAccounts(long assetId, int height, int from, int to) {
    if (height < 0) {
      return getAssetAccounts(assetId, from, to);
    }
    return accountStore.getAssetAccounts(assetId, height, from, to);
  }

  public int getAssetAccountsCount(long assetId, long minimumQuantity) {
    return accountStore.getAssetAccountsCount(assetId, minimumQuantity);
  }
  
  public long getAssetCirculatingSupply(Asset asset) {
    return accountStore.getAssetCirculatingSupply(asset);
  }

}
