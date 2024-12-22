package brs.assetexchange;

import brs.Block;
import brs.Order;
import brs.Trade;
import brs.Trade.Event;
import brs.db.SignumKey;
import brs.db.SignumKey.LinkKeyFactory;
import brs.db.sql.EntitySqlTable;
import brs.db.store.TradeStore;
import brs.util.Listener;
import brs.util.Listeners;

import java.util.Collection;

class TradeServiceImpl {

  private final Listeners<Trade,Event> listeners = new Listeners<>();

  private final TradeStore tradeStore;
  private final EntitySqlTable<Trade> tradeTable;
  private final LinkKeyFactory<Trade> tradeDbKeyFactory;


  public TradeServiceImpl(TradeStore tradeStore) {
    this.tradeStore = tradeStore;
    this.tradeTable = tradeStore.getTradeTable();
    this.tradeDbKeyFactory = tradeStore.getTradeDbKeyFactory();
  }

  public Collection<Trade> getAssetTrades(long assetId, int from, int to) {
    return tradeStore.getAssetTrades(assetId, from, to);
  }

  public Collection<Trade> getAccountAssetTrades(long accountId, long assetId, int from, int to) {
    return tradeStore.getAccountAssetTrades(accountId, assetId, from, to);
  }

  public Collection<Trade> getAccountTrades(long id, int from, int to) {
    return tradeStore.getAccountTrades(id, from, to);
  }
  
  public Collection<Trade> getOrderTrades(long orderId) {
    return tradeStore.getOrderTrades(orderId);
  }

  public int getCount() {
    return tradeTable.getCount();
  }

  public int getTradeCount(long assetId) {
    return tradeStore.getTradeCount(assetId);
  }

  public long getTradeVolume(long assetId, int heightStart, int heightEnd) {
    return tradeStore.getTradeVolume(assetId, heightStart, heightEnd);
  }

  public long getHighPrice(long assetId, int heightStart, int heightEnd) {
    return tradeStore.getHighPrice(assetId, heightStart, heightEnd);
  }

  public long getLowPrice(long assetId, int heightStart, int heightEnd) {
    return tradeStore.getLowPrice(assetId, heightStart, heightEnd);
  }

  public long getOpenPrice(long assetId, int heightStart, int heightEnd) {
    return tradeStore.getOpenPrice(assetId, heightStart, heightEnd);
  }

  public long getClosePrice(long assetId, int heightStart, int heightEnd) {
    return tradeStore.getClosePrice(assetId, heightStart, heightEnd);
  }


  public Collection<Trade> getAllTrades(int from, int to) {
    return tradeTable.getAll(from, to);
  }

  public boolean addListener(Listener<Trade> listener, Event eventType) {
    return listeners.addListener(listener, eventType);
  }

  public boolean removeListener(Listener<Trade> listener, Event eventType) {
    return listeners.removeListener(listener, eventType);
  }

  public Trade addTrade(long assetId, Block block, Order.Ask askOrder, Order.Bid bidOrder) {
    SignumKey dbKey = tradeDbKeyFactory.newKey(askOrder.getId(), bidOrder.getId());
    Trade trade = new Trade(dbKey, assetId, block, askOrder, bidOrder);
    tradeTable.insert(trade);
    listeners.notify(trade, Event.TRADE);
    return trade;
  }
}
