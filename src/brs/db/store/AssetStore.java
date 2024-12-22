package brs.db.store;

import brs.Asset;
import brs.db.SignumKey;
import brs.db.sql.EntitySqlTable;

import java.util.Collection;

public interface AssetStore {
  SignumKey.LongKeyFactory<Asset> getAssetDbKeyFactory();

  EntitySqlTable<Asset> getAssetTable();

  Collection<Asset> getAssetsIssuedBy(long accountId, int from, int to);

  Asset getAsset(long assetId);

  Collection<Asset> getAssetsByName(String name, int from, int to);
}
