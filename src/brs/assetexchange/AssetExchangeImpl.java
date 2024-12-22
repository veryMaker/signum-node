package brs.assetexchange;

import brs.Account.AccountAsset;
import brs.Asset;
import brs.AssetTransfer;
import brs.Attachment.ColoredCoinsAskOrderPlacement;
import brs.Attachment.ColoredCoinsAssetIssuance;
import brs.Attachment.ColoredCoinsBidOrderPlacement;
import brs.Order.Ask;
import brs.Order.Bid;
import brs.Order.OrderJournal;
import brs.Trade;
import brs.Trade.Event;
import brs.Transaction;
import brs.db.store.*;
import brs.services.AccountService;
import brs.util.CollectionWithIndex;
import brs.util.Listener;

public class AssetExchangeImpl implements AssetExchange {

  private final TradeServiceImpl tradeService;
  private final AssetAccountServiceImpl assetAccountService;
  private final AssetTransferServiceImpl assetTransferService;
  private final AssetServiceImpl assetService;
  private final OrderServiceImpl orderService;


  public AssetExchangeImpl(AccountService accountService, TradeStore tradeStore, AccountStore accountStore, AssetTransferStore assetTransferStore, AssetStore assetStore, OrderStore orderStore) {
    this.tradeService = new TradeServiceImpl(tradeStore);
    this.assetAccountService = new AssetAccountServiceImpl(accountStore);
    this.assetTransferService = new AssetTransferServiceImpl(assetTransferStore);
    this.assetService = new AssetServiceImpl(this.assetAccountService, tradeService, assetStore, assetTransferService);
    this.orderService = new OrderServiceImpl(orderStore, accountService, tradeService);
  }

  @Override
  public CollectionWithIndex<Asset> getAllAssets(int from, int to) {
    return new CollectionWithIndex<Asset>(assetService.getAllAssets(from, to), from, to);
  }
  
  @Override
  public CollectionWithIndex<Asset> getAssetsByName(String name, int from, int to) {
    return new CollectionWithIndex<Asset>(assetService.getAssetsByName(name, from, to), from, to);
  }

  @Override
  public Asset getAsset(long assetId) {
    return assetService.getAsset(assetId);
  }

  @Override
  public int getTradeCount(long assetId) {
    return tradeService.getTradeCount(assetId);
  }

  @Override
  public int getTransferCount(long assetId) {
    return assetTransferService.getTransferCount(assetId);
  }

  @Override
  public int getAssetAccountsCount(Asset asset, long minimumQuantity, boolean ignoreTreasury, boolean unconfirmed) {
    return assetAccountService.getAssetAccountsCount(asset, minimumQuantity, ignoreTreasury, unconfirmed);
  }

  @Override
  public long getAssetCirculatingSupply(Asset asset, boolean ignoreTreasury, boolean unconfirmed) {
    return assetAccountService.getAssetCirculatingSupply(asset, ignoreTreasury, unconfirmed);
  }

  @Override
  public void addTradeListener(Listener<Trade> listener, Event eventType) {
    tradeService.addListener(listener, eventType);
  }

  @Override
  public Ask getAskOrder(long orderId) {
    return orderService.getAskOrder(orderId);
  }

  @Override
  public void addAsset(long assetId, long accountId, ColoredCoinsAssetIssuance attachment) {
    assetService.addAsset(assetId, accountId, attachment);
  }

  @Override
  public void addAssetTransfer(Transaction transaction, long assetId, long quantityQNT) {
    assetTransferService.addAssetTransfer(transaction, assetId, quantityQNT);
  }

  @Override
  public void addAskOrder(Transaction transaction, ColoredCoinsAskOrderPlacement attachment) {
    orderService.addAskOrder(transaction, attachment);
  }

  @Override
  public void addBidOrder(Transaction transaction, ColoredCoinsBidOrderPlacement attachment) {
    orderService.addBidOrder(transaction, attachment);
  }

  @Override
  public void removeAskOrder(long orderId) {
    orderService.removeAskOrder(orderId);
  }

  @Override
  public Bid getBidOrder(long orderId) {
    return orderService.getBidOrder(orderId);
  }

  @Override
  public void removeBidOrder(long orderId) {
    orderService.removeBidOrder(orderId);
  }

  @Override
  public CollectionWithIndex<Trade> getAllTrades(int from, int to) {
    return new CollectionWithIndex<Trade>(tradeService.getAllTrades(from, to), from, to);
  }

  @Override
  public CollectionWithIndex<Trade> getTrades(long assetId, int from, int to) {
    return new CollectionWithIndex<Trade>(assetService.getTrades(assetId, from, to), from, to);
  }

  @Override
  public CollectionWithIndex<Trade> getAccountTrades(long id, int from, int to) {
    return new CollectionWithIndex<Trade>(tradeService.getAccountTrades(id, from, to), from, to);
  }

  @Override
  public CollectionWithIndex<Trade> getAccountAssetTrades(long accountId, long assetId, int from, int to) {
    return new CollectionWithIndex<Trade>(tradeService.getAccountAssetTrades(accountId, assetId, from, to), from, to);
  }

  @Override
  public CollectionWithIndex<OrderJournal> getOrderJournal(long accountId, long assetId, int from, int to) {
    return orderService.getTradeJournal(accountId, assetId, from, to);
  }

  @Override
  public int getAssetsCount() {
    return assetService.getAssetsCount();
  }

  @Override
  public CollectionWithIndex<AccountAsset> getAssetAccounts(Asset asset, boolean ignoreTreasury, long minimumQuantity, boolean unconfirmed, int from, int to) {
    return new CollectionWithIndex<AccountAsset>(assetAccountService.getAssetAccounts(asset, ignoreTreasury, minimumQuantity, unconfirmed, from, to), from, to);
  }

  @Override
  public CollectionWithIndex<Asset> getAssetsIssuedBy(long accountId, int from, int to) {
    return new CollectionWithIndex<Asset>(assetService.getAssetsIssuedBy(accountId, from, to), from, to);
  }

  @Override
  public CollectionWithIndex<Asset> getAssetsOwnedBy(long accountId, int from, int to) {
    return assetService.getAssetsOwnedBy(accountId, from, to);
  }

  @Override
  public CollectionWithIndex<AssetTransfer> getAssetTransfers(long assetId, int from, int to) {
    return new CollectionWithIndex<AssetTransfer>(assetTransferService.getAssetTransfers(assetId, from, to), from, to);
  }

  @Override
  public CollectionWithIndex<AssetTransfer> getAccountAssetTransfers(long accountId, long assetId, int from, int to) {
    return new CollectionWithIndex<AssetTransfer>(assetTransferService.getAccountAssetTransfers(accountId, assetId, from, to), from, to);
  }

  @Override
  public int getAskCount() {
    return orderService.getAskCount();
  }

  @Override
  public int getBidCount() {
    return orderService.getBidCount();
  }

  @Override
  public int getTradesCount() {
    return tradeService.getCount();
  }

  @Override
  public int getAssetTransferCount() {
    return assetTransferService.getAssetTransferCount();
  }

  @Override
  public CollectionWithIndex<Ask> getAskOrdersByAccount(long accountId, int from, int to) {
    return orderService.getAskOrdersByAccount(accountId, from, to);
  }

  @Override
  public CollectionWithIndex<Ask> getAskOrdersByAccountAsset(long accountId, long assetId, int from, int to) {
    return orderService.getAskOrdersByAccountAsset(accountId, assetId, from, to);
  }

  @Override
  public CollectionWithIndex<Bid> getBidOrdersByAccount(long accountId, int from, int to) {
    return orderService.getBidOrdersByAccount(accountId, from, to);
  }

  @Override
  public CollectionWithIndex<Bid> getBidOrdersByAccountAsset(long accountId, long assetId, int from, int to) {
    return orderService.getBidOrdersByAccountAsset(accountId, assetId, from, to);
  }

  @Override
  public CollectionWithIndex<Ask> getAllAskOrders(int from, int to) {
    return orderService.getAllAskOrders(from, to);
  }

  @Override
  public CollectionWithIndex<Bid> getAllBidOrders(int from, int to) {
    return orderService.getAllBidOrders(from, to);
  }

  @Override
  public CollectionWithIndex<Ask> getSortedAskOrders(long assetId, int from, int to) {
    return orderService.getSortedAskOrders(assetId, from, to);
  }

  @Override
  public CollectionWithIndex<Bid> getSortedBidOrders(long assetId, int from, int to) {
    return orderService.getSortedBidOrders(assetId, from, to);
  }

  @Override
  public long getTradeVolume(long assetId, int heightStart, int heightEnd) {
    return tradeService.getTradeVolume(assetId, heightStart, heightEnd);
  }

  @Override
  public long getHighPrice(long assetId, int heightStart, int heightEnd) {
    return tradeService.getHighPrice(assetId, heightStart, heightEnd);
  }

  @Override
  public long getLowPrice(long assetId, int heightStart, int heightEnd) {
    return tradeService.getLowPrice(assetId, heightStart, heightEnd);
  }

  @Override
  public long getOpenPrice(long assetId, int heightStart, int heightEnd) {
    return tradeService.getOpenPrice(assetId, heightStart, heightEnd);
  }

  @Override
  public long getClosePrice(long assetId, int heightStart, int heightEnd) {
    return tradeService.getClosePrice(assetId, heightStart, heightEnd);
  }

}
