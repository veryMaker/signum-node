package brs;

import brs.db.SignumKey;

public class Alias {

  private long accountId;
  private final long id;
  public final SignumKey dbKey;
  private final String aliasName;
  private final Long tld;
  private String aliasURI;
  private int timestamp;

  private Alias(SignumKey dbKey, long id, long accountId, String aliasName, Long tld, String aliasURI, int timestamp) {
    this.id = id;
    this.dbKey = dbKey;
    this.accountId = accountId;
    this.aliasName = aliasName;
    this.tld = tld;
    this.aliasURI = aliasURI;
    this.timestamp = timestamp;
  }

  protected Alias(long id, long accountId, String aliasName, Long tld, String aliasURI, int timestamp, SignumKey dbKey) {
    this.id = id;
    this.dbKey = dbKey;
    this.accountId = accountId;
    this.aliasName = aliasName;
    this.tld = tld;
    this.aliasURI = aliasURI;
    this.timestamp = timestamp;
  }

  public Alias(long aliasId, SignumKey dbKey, Transaction transaction, Attachment.MessagingAliasAssignment attachment) {
    this(dbKey, aliasId, transaction.getSenderId(), attachment.getAliasName(), attachment.getTLD(), attachment.getAliasURI(),
        transaction.getBlockTimestamp());
  }
  
  public Alias(long aliasId, SignumKey dbKey, Transaction transaction, Attachment.MessagingTLDAssignment attachment) {
    this(dbKey, aliasId, transaction == null ? 0L : transaction.getSenderId(), attachment.getTLDName(), null, "",
        transaction == null ? 0 : transaction.getBlockTimestamp());
  }

  public long getId() {
    return id;
  }
  
  public Long getTLD() {
    return tld;
  }

  public String getAliasName() {
    return aliasName;
  }

  public String getAliasURI() {
    return aliasURI;
  }

  public int getTimestamp() {
    return timestamp;
  }

  public long getAccountId() {
    return accountId;
  }

  public void setAccountId(long accountId) {
    this.accountId = accountId;
  }

  public void setAliasURI(String aliasURI) {
    this.aliasURI = aliasURI;
  }

  public void setTimestamp(int timestamp) {
    this.timestamp = timestamp;
  }

  public static class Offer {

    private long priceNQT;
    private long buyerId;
    private final long aliasId;
    public final SignumKey dbKey;

    public Offer(SignumKey dbKey, long aliasId, long priceNQT, long buyerId) {
      this.dbKey = dbKey;
      this.priceNQT = priceNQT;
      this.buyerId = buyerId;
      this.aliasId = aliasId;
    }

    protected Offer(long aliasId, long priceNQT, long buyerId, SignumKey nxtKey) {
      this.priceNQT = priceNQT;
      this.buyerId = buyerId;
      this.aliasId = aliasId;
      this.dbKey = nxtKey;
    }

    public long getId() {
      return aliasId;
    }

    public long getPriceNQT() {
      return priceNQT;
    }

    public long getBuyerId() {
      return buyerId;
    }

    public void setPriceNQT(long priceNQT) {
      this.priceNQT = priceNQT;
    }

    public void setBuyerId(long buyerId) {
      this.buyerId = buyerId;
    }
  }

}
