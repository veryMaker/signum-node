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

  public Collection<AccountAsset> getAssetAccounts(Asset asset, boolean ignoreTreasury, long minimumQuantity, int from, int to) {
    return accountStore.getAssetAccounts(asset, ignoreTreasury, minimumQuantity, from, to);
  }

  public int getAssetAccountsCount(Asset asset, long minimumQuantity, boolean ignoreTreasury) {
    return accountStore.getAssetAccountsCount(asset, minimumQuantity, ignoreTreasury);
  }
  
  public long getAssetCirculatingSupply(Asset asset) {
    return accountStore.getAssetCirculatingSupply(asset);
  }

}
