package brs.assetexchange;

import brs.*;
import brs.Order.Ask;
import brs.Order.Bid;
import brs.db.BurstKey;
import brs.db.BurstKey.LongKeyFactory;
import brs.db.VersionedEntityTable;
import brs.db.store.OrderStore;
import brs.services.AccountService;
import brs.util.Convert;

import java.util.Collection;

class OrderServiceImpl {
  private final DependencyProvider dp;
  private final TradeServiceImpl tradeService;
  private final VersionedEntityTable<Ask> askOrderTable;
  private final LongKeyFactory<Ask> askOrderDbKeyFactory;
  private final VersionedEntityTable<Bid> bidOrderTable;
  private final LongKeyFactory<Bid> bidOrderDbKeyFactory;

  public OrderServiceImpl(DependencyProvider dp, TradeServiceImpl tradeService) {
    this.dp = dp;
    this.tradeService = tradeService;
    this.askOrderTable = dp.orderStore.getAskOrderTable();
    this.askOrderDbKeyFactory = dp.orderStore.getAskOrderDbKeyFactory();
    this.bidOrderTable = dp.orderStore.getBidOrderTable();
    this.bidOrderDbKeyFactory = dp.orderStore.getBidOrderDbKeyFactory();
  }

  public Ask getAskOrder(long orderId) {
    return askOrderTable.get(askOrderDbKeyFactory.newKey(orderId));
  }

  public Bid getBidOrder(long orderId) {
    return bidOrderTable.get(bidOrderDbKeyFactory.newKey(orderId));
  }

  public Collection<Ask> getAllAskOrders(int from, int to) {
    return askOrderTable.getAll(from, to);
  }

  public Collection<Bid> getAllBidOrders(int from, int to) {
    return bidOrderTable.getAll(from, to);
  }

  public Collection<Bid> getSortedBidOrders(long assetId, int from, int to) {
    return dp.orderStore.getSortedBids(assetId, from, to);
  }

  public Collection<Ask> getAskOrdersByAccount(long accountId, int from, int to) {
    return dp.orderStore.getAskOrdersByAccount(accountId, from, to);
  }

  public Collection<Ask> getAskOrdersByAccountAsset(final long accountId, final long assetId, int from, int to) {
    return dp.orderStore.getAskOrdersByAccountAsset(accountId, assetId, from, to);
  }

  public Collection<Ask> getSortedAskOrders(long assetId, int from, int to) {
    return dp.orderStore.getSortedAsks(assetId, from, to);
  }

  public int getBidCount() {
    return bidOrderTable.getCount();
  }

  public int getAskCount() {
    return askOrderTable.getCount();
  }

  public Collection<Bid> getBidOrdersByAccount(long accountId, int from, int to) {
    return dp.orderStore.getBidOrdersByAccount(accountId, from, to);
  }

  public Collection<Bid> getBidOrdersByAccountAsset(final long accountId, final long assetId, int from, int to) {
    return dp.orderStore.getBidOrdersByAccountAsset(accountId, assetId, from, to);
  }

  public void removeBidOrder(long orderId) {
    bidOrderTable.delete(getBidOrder(orderId));
  }

  public void removeAskOrder(long orderId) {
    askOrderTable.delete(getAskOrder(orderId));
  }

  public void addAskOrder(Transaction transaction, Attachment.ColoredCoinsAskOrderPlacement attachment) {
    BurstKey dbKey = askOrderDbKeyFactory.newKey(transaction.getId());
    Ask order = new Ask(dbKey, transaction, attachment);
    askOrderTable.insert(order);
    matchOrders(attachment.getAssetId());
  }

  public void addBidOrder(Transaction transaction, Attachment.ColoredCoinsBidOrderPlacement attachment) {
    BurstKey dbKey = bidOrderDbKeyFactory.newKey(transaction.getId());
    Bid order = new Bid(dbKey, transaction, attachment);
    bidOrderTable.insert(order);
    matchOrders(attachment.getAssetId());
  }

  private Ask getNextAskOrder(long assetId) {
    return dp.orderStore.getNextOrder(assetId);
  }

  private Bid getNextBidOrder(long assetId) {
    return dp.orderStore.getNextBid(assetId);
  }

  private void matchOrders(long assetId) {

    Order.Ask askOrder;
    Order.Bid bidOrder;

    while ((askOrder = getNextAskOrder(assetId)) != null
        && (bidOrder = getNextBidOrder(assetId)) != null) {

      if (askOrder.getPriceNQT() > bidOrder.getPriceNQT()) {
        break;
      }


      Trade trade = tradeService.addTrade(assetId, dp.blockchain.getLastBlock(), askOrder, bidOrder);

      askOrderUpdateQuantityQNT(askOrder, Convert.INSTANCE.safeSubtract(askOrder.getQuantityQNT(), trade.getQuantityQNT()));
      Account askAccount = dp.accountService.getAccount(askOrder.getAccountId());
      dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(askAccount, Convert.INSTANCE.safeMultiply(trade.getQuantityQNT(), trade.getPriceNQT()));
      dp.accountService.addToAssetBalanceQNT(askAccount, assetId, -trade.getQuantityQNT());

      bidOrderUpdateQuantityQNT(bidOrder, Convert.INSTANCE.safeSubtract(bidOrder.getQuantityQNT(), trade.getQuantityQNT()));
      Account bidAccount = dp.accountService.getAccount(bidOrder.getAccountId());
      dp.accountService.addToAssetAndUnconfirmedAssetBalanceQNT(bidAccount, assetId, trade.getQuantityQNT());
      dp.accountService.addToBalanceNQT(bidAccount, -Convert.INSTANCE.safeMultiply(trade.getQuantityQNT(), trade.getPriceNQT()));
      dp.accountService.addToUnconfirmedBalanceNQT(bidAccount, Convert.INSTANCE.safeMultiply(trade.getQuantityQNT(), (bidOrder.getPriceNQT() - trade.getPriceNQT())));
    }
  }

  private void askOrderUpdateQuantityQNT(Ask askOrder, long quantityQNT) {
    askOrder.setQuantityQNT(quantityQNT);
    if (quantityQNT > 0) {
      askOrderTable.insert(askOrder);
    } else if (quantityQNT == 0) {
      askOrderTable.delete(askOrder);
    } else {
      throw new IllegalArgumentException("Negative quantity: " + quantityQNT
          + " for order: " + Convert.INSTANCE.toUnsignedLong(askOrder.getId()));
    }
  }

  private void bidOrderUpdateQuantityQNT(Bid bidOrder, long quantityQNT) {
    bidOrder.setQuantityQNT(quantityQNT);
    if (quantityQNT > 0) {
      bidOrderTable.insert(bidOrder);
    } else if (quantityQNT == 0) {
      bidOrderTable.delete(bidOrder);
    } else {
      throw new IllegalArgumentException("Negative quantity: " + quantityQNT
          + " for order: " + Convert.INSTANCE.toUnsignedLong(bidOrder.getId()));
    }
  }
}
