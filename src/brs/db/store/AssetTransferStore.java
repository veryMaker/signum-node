package brs.db.store;

import brs.AssetTransfer;
import brs.db.SignumKey;
import brs.db.sql.EntitySqlTable;

import java.util.Collection;

public interface AssetTransferStore {
  SignumKey.LinkKeyFactory<AssetTransfer> getTransferDbKeyFactory();

  EntitySqlTable<AssetTransfer> getAssetTransferTable();

  Collection<AssetTransfer> getAssetTransfers(long assetId, int from, int to);

  Collection<AssetTransfer> getAccountAssetTransfers(long accountId, int from, int to);

  Collection<AssetTransfer> getAccountAssetTransfers(long accountId, long assetId, int from, int to);

  int getTransferCount(long assetId);
}
