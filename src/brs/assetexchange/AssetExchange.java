package brs.assetexchange;

import brs.Account.AccountAsset;
import brs.*;
import brs.Attachment.ColoredCoinsAskOrderPlacement;
import brs.Attachment.ColoredCoinsAssetIssuance;
import brs.Attachment.ColoredCoinsBidOrderPlacement;
import brs.Order.Ask;
import brs.Order.Bid;
import brs.Order.OrderJournal;
import brs.Trade.Event;
import brs.util.CollectionWithIndex;
import brs.util.Listener;

public interface AssetExchange {

  CollectionWithIndex<Asset> getAllAssets(int from, int to);

  CollectionWithIndex<Asset> getAssetsByName(String name, int from, int to);

  Asset getAsset(long assetId);

  long getTradeVolume(long assetId, int heightStart, int heightEnd);

  long getHighPrice(long assetId, int heightStart, int heightEnd);

  long getLowPrice(long assetId, int heightStart, int heightEnd);

  long getOpenPrice(long assetId, int heightStart, int heightEnd);

  long getClosePrice(long assetId, int heightStart, int heightEnd);

  int getTradeCount(long assetId);

  int getTransferCount(long id);

  int getAssetAccountsCount(Asset asset, long minimumQuantity, boolean ignoreTreasury, boolean unconfirmed);

  long getAssetCirculatingSupply(Asset asset, boolean ignoreTreasury, boolean unconfirmed);

  void addTradeListener(Listener<Trade> listener, Event trade);

  Ask getAskOrder(long orderId);

  void addAsset(long assetId, long accountId, ColoredCoinsAssetIssuance attachment);

  void addAssetTransfer(Transaction transaction, long assetId, long quantityQNT);

  void addAskOrder(Transaction transaction, ColoredCoinsAskOrderPlacement attachment);

  void addBidOrder(Transaction transaction, ColoredCoinsBidOrderPlacement attachment);

  void removeAskOrder(long orderId);

  Order.Bid getBidOrder(long orderId);

  void removeBidOrder(long orderId);

  CollectionWithIndex<Trade> getAllTrades(int from, int to);

  CollectionWithIndex<Trade> getTrades(long assetId, int from, int to);

  CollectionWithIndex<Trade> getAccountTrades(long accountId, int from, int to);

  CollectionWithIndex<Trade> getAccountAssetTrades(long accountId, long assetId, int from, int to);
  
  CollectionWithIndex<OrderJournal> getOrderJournal(long assetId, long accountId, int from, int to);

  CollectionWithIndex<AccountAsset> getAssetAccounts(Asset asset, boolean ignoreTreasury, long minimumQuantity, boolean unconfirmed, int from, int to);

  CollectionWithIndex<Asset> getAssetsIssuedBy(long accountId, int from, int to);

  CollectionWithIndex<Asset> getAssetsOwnedBy(long accountId, int from, int to);

  CollectionWithIndex<AssetTransfer> getAssetTransfers(long assetId, int from, int to);

  CollectionWithIndex<AssetTransfer> getAccountAssetTransfers(long id, long id1, int from, int to);

  int getAssetsCount();

  int getAskCount();

  int getBidCount();

  int getTradesCount();

  int getAssetTransferCount();

  CollectionWithIndex<Ask> getAskOrdersByAccount(long accountId, int from, int to);

  CollectionWithIndex<Ask> getAskOrdersByAccountAsset(long accountId, long assetId, int from, int to);

  CollectionWithIndex<Bid> getBidOrdersByAccount(long accountId, int from, int to);

  CollectionWithIndex<Bid> getBidOrdersByAccountAsset(long accountId, long assetId, int from, int to);

  CollectionWithIndex<Ask> getAllAskOrders(int from, int to);

  CollectionWithIndex<Bid> getAllBidOrders(int from, int to);

  CollectionWithIndex<Ask> getSortedAskOrders(long assetId, int from, int to);

  CollectionWithIndex<Bid> getSortedBidOrders(long assetId, int from, int to);

}
