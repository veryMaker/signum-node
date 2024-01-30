package brs;

import brs.db.SignumKey;

//TODO: Create JavaDocs and remove this
@SuppressWarnings({ "checkstyle:MissingJavadocTypeCheck", "checkstyle:MissingJavadocMethodCheck" })

public class Alias {

    private long accountId;
    private final long id;
    public final SignumKey dbKey;
    private final String aliasName;
    private final Long tld;
    private String aliasUri;
    private int timestamp;

    private Alias(
            SignumKey dbKey,
            long id,
            long accountId,
            String aliasName,
            Long tld,
            String aliasUri,
            int timestamp) {
        this.id = id;
        this.dbKey = dbKey;
        this.accountId = accountId;
        this.aliasName = aliasName;
        this.tld = tld;
        this.aliasUri = aliasUri;
        this.timestamp = timestamp;
    }

    protected Alias(
            long id,
            long accountId,
            String aliasName,
            Long tld,
            String aliasUri,
            int timestamp,
            SignumKey dbKey) {
        this.id = id;
        this.dbKey = dbKey;
        this.accountId = accountId;
        this.aliasName = aliasName;
        this.tld = tld;
        this.aliasUri = aliasUri;
        this.timestamp = timestamp;
    }

    public Alias(long aliasId,
            SignumKey dbKey,
            Transaction transaction,
            Attachment.MessagingAliasAssignment attachment) {
        this(
                dbKey,
                aliasId,
                transaction.getSenderId(),
                attachment.getAliasName(),
                attachment.getTLD(),
                attachment.getAliasURI(),
                transaction.getBlockTimestamp());
    }

    public Alias(
            long aliasId,
            SignumKey dbKey,
            Transaction transaction,
            Attachment.MessagingTLDAssignment attachment) {
        this(dbKey,
                aliasId,
                transaction == null ? 0L : transaction.getSenderId(),
                attachment.getTLDName(),
                null, "",
                transaction == null ? 0 : transaction.getBlockTimestamp());
    }

    public long getId() {
        return id;
    }

    public Long getTld() {
        return tld;
    }

    public String getAliasName() {
        return aliasName;
    }

    public String getAliasUri() {
        return aliasUri;
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

    public void setAliasUri(String aliasUri) {
        this.aliasUri = aliasUri;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public static class Offer {

        private long priceNqt;
        private long buyerId;
        private final long aliasId;
        public final SignumKey dbKey;

        public Offer(SignumKey dbKey, long aliasId, long priceNqt, long buyerId) {
            this.dbKey = dbKey;
            this.priceNqt = priceNqt;
            this.buyerId = buyerId;
            this.aliasId = aliasId;
        }

        protected Offer(long aliasId, long priceNqt, long buyerId, SignumKey nxtKey) {
            this.priceNqt = priceNqt;
            this.buyerId = buyerId;
            this.aliasId = aliasId;
            this.dbKey = nxtKey;
        }

        public long getId() {
            return aliasId;
        }

        public long getPriceNqt() {
            return priceNqt;
        }

        public long getBuyerId() {
            return buyerId;
        }

        public void setPriceNqt(long priceNqt) {
            this.priceNqt = priceNqt;
        }

        public void setBuyerId(long buyerId) {
            this.buyerId = buyerId;
        }
    }

}
