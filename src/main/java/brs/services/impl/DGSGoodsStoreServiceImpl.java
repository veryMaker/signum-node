package brs.services.impl;

import brs.*;
import brs.DigitalGoodsStore.Event;
import brs.DigitalGoodsStore.Goods;
import brs.DigitalGoodsStore.Purchase;
import brs.crypto.EncryptedData;
import brs.db.BurstKey;
import brs.db.BurstKey.LongKeyFactory;
import brs.db.VersionedEntityTable;
import brs.db.VersionedValuesTable;
import brs.db.store.DigitalGoodsStoreStore;
import brs.services.AccountService;
import brs.services.DGSGoodsStoreService;
import brs.util.Convert;
import brs.util.Listeners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class DGSGoodsStoreServiceImpl implements DGSGoodsStoreService {

  private final DependencyProvider dp;
  private final VersionedValuesTable<Purchase, EncryptedData> feedbackTable;
  private final VersionedValuesTable<Purchase, String> publicFeedbackTable;

  private final VersionedEntityTable<Goods> goodsTable;
  private final VersionedEntityTable<Purchase> purchaseTable;
  private final LongKeyFactory<Goods> goodsDbKeyFactory;
  private final LongKeyFactory<Purchase> purchaseDbKeyFactory;

  private final Listeners<Goods,Event> goodsListeners = new Listeners<>();

  private final Listeners<Purchase,Event> purchaseListeners = new Listeners<>();

  public DGSGoodsStoreServiceImpl(DependencyProvider dp) {
    this.dp = dp;
    this.goodsTable = dp.digitalGoodsStoreStore.getGoodsTable();
    this.purchaseTable = dp.digitalGoodsStoreStore.getPurchaseTable();
    this.goodsDbKeyFactory = dp.digitalGoodsStoreStore.getGoodsDbKeyFactory();
    this.purchaseDbKeyFactory = dp.digitalGoodsStoreStore.getPurchaseDbKeyFactory();
    this.feedbackTable = dp.digitalGoodsStoreStore.getFeedbackTable();
    this.publicFeedbackTable = dp.digitalGoodsStoreStore.getPublicFeedbackTable();
  }

  @Override
  public boolean addGoodsListener(Consumer<Goods> listener, Event eventType) {
    return goodsListeners.addListener(listener, eventType);
  }

  @Override
  public boolean removeGoodsListener(Consumer<Goods> listener, Event eventType) {
    return goodsListeners.removeListener(listener, eventType);
  }

  @Override
  public boolean addPurchaseListener(Consumer<Purchase> listener, Event eventType) {
    return purchaseListeners.addListener(listener, eventType);
  }

  @Override
  public boolean removePurchaseListener(Consumer<Purchase> listener, Event eventType) {
    return purchaseListeners.removeListener(listener, eventType);
  }

  @Override
  public Goods getGoods(long goodsId) {
    return goodsTable.get(goodsDbKeyFactory.newKey(goodsId));
  }

  @Override
  public Collection<Goods> getAllGoods(int from, int to) {
    return goodsTable.getAll(from, to);
  }

  @Override
  public Collection<Goods> getGoodsInStock(int from, int to) {
    return dp.digitalGoodsStoreStore.getGoodsInStock(from, to);
  }

  @Override
  public Collection<Goods> getSellerGoods(final long sellerId, final boolean inStockOnly, int from, int to) {
    return dp.digitalGoodsStoreStore.getSellerGoods(sellerId, inStockOnly, from, to);
  }

  @Override
  public Collection<Purchase> getAllPurchases(int from, int to) {
    return purchaseTable.getAll(from, to);
  }

  @Override
  public Collection<Purchase> getSellerPurchases(long sellerId, int from, int to) {
    return dp.digitalGoodsStoreStore.getSellerPurchases(sellerId, from, to);
  }

  @Override
  public Collection<Purchase> getBuyerPurchases(long buyerId, int from, int to) {
    return dp.digitalGoodsStoreStore.getBuyerPurchases(buyerId, from, to);
  }

  @Override
  public Collection<Purchase> getSellerBuyerPurchases(final long sellerId, final long buyerId, int from, int to) {
    return dp.digitalGoodsStoreStore.getSellerBuyerPurchases(sellerId, buyerId, from, to);
  }

  @Override
  public Collection<Purchase> getPendingSellerPurchases(final long sellerId, int from, int to) {
    return dp.digitalGoodsStoreStore.getPendingSellerPurchases(sellerId, from, to);
  }

  @Override
  public Purchase getPurchase(long purchaseId) {
    return purchaseTable.get(purchaseDbKeyFactory.newKey(purchaseId));
  }

  @Override
  public void changeQuantity(long goodsId, int deltaQuantity, boolean allowDelisted) {
    Goods goods = goodsTable.get(goodsDbKeyFactory.newKey(goodsId));
    if (allowDelisted || ! goods.isDelisted()) {
      goods.changeQuantity(deltaQuantity);
      goodsTable.insert(goods);
      goodsListeners.accept(goods, Event.GOODS_QUANTITY_CHANGE);
    } else {
      throw new IllegalStateException("Can't change quantity of delisted goods");
    }
  }

  @Override
  public void purchase(Transaction transaction, Attachment.DigitalGoodsPurchase attachment) {
    Goods goods = goodsTable.get(goodsDbKeyFactory.newKey(attachment.getGoodsId()));
    if (! goods.isDelisted() && attachment.getQuantity() <= goods.getQuantity() && attachment.getPriceNQT() == goods.getPriceNQT()
        && attachment.getDeliveryDeadlineTimestamp() > dp.blockchain.getLastBlock().getTimestamp()) {
      changeQuantity(goods.getId(), -attachment.getQuantity(), false);
      addPurchase(transaction, attachment, goods.getSellerId());
    } else {
      Account buyer = dp.accountService.getAccount(transaction.getSenderId());
      dp.accountService.addToUnconfirmedBalanceNQT(buyer, Convert.INSTANCE.safeMultiply(attachment.getQuantity(), attachment.getPriceNQT()));
      // restoring the unconfirmed balance if purchase not successful, however buyer still lost the transaction fees
    }
  }

  @Override
  public void addPurchase(Transaction transaction, Attachment.DigitalGoodsPurchase attachment, long sellerId) {
    Purchase purchase = new Purchase(dp, transaction, attachment, sellerId);
    purchaseTable.insert(purchase);
    purchaseListeners.accept(purchase, Event.PURCHASE);
  }

  @Override
  public void listGoods(Transaction transaction, Attachment.DigitalGoodsListing attachment) {
    BurstKey dbKey = goodsDbKeyFactory.newKey(transaction.getId());
    Goods goods = new Goods(dbKey, transaction, attachment);
    goodsTable.insert(goods);
    goodsListeners.accept(goods, Event.GOODS_LISTED);
  }

  @Override
  public void delistGoods(long goodsId) {
    Goods goods = goodsTable.get(goodsDbKeyFactory.newKey(goodsId));
    if (! goods.isDelisted()) {
      goods.setDelisted(true);
      goodsTable.insert(goods);
      goodsListeners.accept(goods, Event.GOODS_DELISTED);
    } else {
      throw new IllegalStateException("Goods already delisted");
    }
  }

  @Override
  public void feedback(long purchaseId, Appendix.EncryptedMessage encryptedMessage, Appendix.Message message) {
    Purchase purchase = purchaseTable.get(purchaseDbKeyFactory.newKey(purchaseId));
    if (encryptedMessage != null) {
      purchase.addFeedbackNote(encryptedMessage.getEncryptedData());
      purchaseTable.insert(purchase);
      feedbackTable.insert(purchase, purchase.getFeedbackNotes());
    }
    if (message != null) {
      addPublicFeedback(purchase, Convert.INSTANCE.toString(message.getMessageBytes()));
    }
    purchaseListeners.accept(purchase, Event.FEEDBACK);
  }

  private void addPublicFeedback(Purchase purchase, String publicFeedback) {
    List<String> publicFeedbacks = purchase.getPublicFeedbacks();
    if (publicFeedbacks == null) {
      publicFeedbacks = new ArrayList<>();
    }
    publicFeedbacks.add(publicFeedback);
    purchase.setHasPublicFeedbacks(true);
    purchaseTable.insert(purchase);
    publicFeedbackTable.insert(purchase, publicFeedbacks);
  }

  @Override
  public void refund(long sellerId, long purchaseId, long refundNQT, Appendix.EncryptedMessage encryptedMessage) {
    Purchase purchase = purchaseTable.get(purchaseDbKeyFactory.newKey(purchaseId));
    Account seller = dp.accountService.getAccount(sellerId);
    dp.accountService.addToBalanceNQT(seller, -refundNQT);
    Account buyer = dp.accountService.getAccount(purchase.getBuyerId());
    dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(buyer, refundNQT);
    if (encryptedMessage != null) {
      purchase.setRefundNote(encryptedMessage.getEncryptedData());
      purchaseTable.insert(purchase);
    }
    purchase.setRefundNQT(refundNQT);
    purchaseTable.insert(purchase);
    purchaseListeners.accept(purchase, Event.REFUND);
  }

  @Override
  public Collection<Purchase> getExpiredPendingPurchases(final int timestamp) {
    return dp.digitalGoodsStoreStore.getExpiredPendingPurchases(timestamp);
  }

  @Override
  public void changePrice(long goodsId, long priceNQT) {
    Goods goods = goodsTable.get(goodsDbKeyFactory.newKey(goodsId));
    if (! goods.isDelisted()) {
      goods.changePrice(priceNQT);
      goodsTable.insert(goods);
      goodsListeners.accept(goods, Event.GOODS_PRICE_CHANGE);
    } else {
      throw new IllegalStateException("Can't change price of delisted goods");
    }
  }

  @Override
  public void deliver(Transaction transaction, Attachment.DigitalGoodsDelivery attachment) {
    Purchase purchase = getPendingPurchase(attachment.getPurchaseId());
    if ( purchase == null ) {
      throw new RuntimeException("cant find purchase with id " + attachment.getPurchaseId());
    }
    setPending(purchase, false);
    long totalWithoutDiscount = Convert.INSTANCE.safeMultiply(purchase.getQuantity(), purchase.getPriceNQT());
    Account buyer = dp.accountService.getAccount(purchase.getBuyerId());
    dp.accountService.addToBalanceNQT(buyer, Convert.INSTANCE.safeSubtract(attachment.getDiscountNQT(), totalWithoutDiscount));
    dp.accountService.addToUnconfirmedBalanceNQT(buyer, attachment.getDiscountNQT());
    Account seller = dp.accountService.getAccount(transaction.getSenderId());
    dp.accountService.addToBalanceAndUnconfirmedBalanceNQT(seller, Convert.INSTANCE.safeSubtract(totalWithoutDiscount, attachment.getDiscountNQT()));
    purchase.setEncryptedGoods(attachment.getGoods(), attachment.goodsIsText());
    purchaseTable.insert(purchase);
    purchase.setDiscountNQT(attachment.getDiscountNQT());
    purchaseTable.insert(purchase);
    purchaseListeners.accept(purchase, Event.DELIVERY);
  }

  @Override
  public Purchase getPendingPurchase(long purchaseId) {
    Purchase purchase = getPurchase(purchaseId);
    return purchase == null || ! purchase.isPending() ? null : purchase;
  }

  @Override
  public void setPending(Purchase purchase, boolean pendingValue) {
    purchase.setPending(pendingValue);
    purchaseTable.insert(purchase);
  }

  public static class ExpiredPurchaseListener implements Consumer<Block> {
    private final DependencyProvider dp;

    public ExpiredPurchaseListener(DependencyProvider dp) {
      this.dp = dp;
    }

    @Override
    public void accept(Block block) {
      for (Purchase purchase : dp.digitalGoodsStoreService.getExpiredPendingPurchases(block.getTimestamp())) {
        Account buyer = dp.accountService.getAccount(purchase.getBuyerId());
        dp.accountService.addToUnconfirmedBalanceNQT(buyer, Convert.INSTANCE.safeMultiply(purchase.getQuantity(), purchase.getPriceNQT()));
        dp.digitalGoodsStoreService.changeQuantity(purchase.getGoodsId(), purchase.getQuantity(), true);
        dp.digitalGoodsStoreService.setPending(purchase, false);
      }
    }
  }
}
