package brs;

import brs.db.SignumKey;

//TODO: Create JavaDocs and remove this
@SuppressWarnings({ "checkstyle:MissingJavadocTypeCheck", "checkstyle:MissingJavadocMethodCheck" })

public class AssetTransfer {

    public enum Event {
        ASSET_TRANSFER
    }

    private final long id;
    private final SignumKey dbKey;
    private final long assetId;
    private final int height;
    private final long senderId;
    private final long recipientId;
    private final long quantityQnt;
    private final int timestamp;

    public AssetTransfer(SignumKey dbKey, Transaction transaction, long assetId, long quantityQnt) {
        this.dbKey = dbKey;
        this.id = transaction.getId();
        this.height = transaction.getHeight();
        this.assetId = assetId;
        this.senderId = transaction.getSenderId();
        this.recipientId = transaction.getRecipientId();
        this.quantityQnt = quantityQnt;
        this.timestamp = transaction.getBlockTimestamp();
    }

    protected AssetTransfer(
            long id,
            SignumKey dbKey,
            long assetId,
            int height,
            long senderId,
            long recipientId,
            long quantityQnt,
            int timestamp) {
        this.id = id;
        this.dbKey = dbKey;
        this.assetId = assetId;
        this.height = height;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.quantityQnt = quantityQnt;
        this.timestamp = timestamp;
    }

    public SignumKey getDbKey() {
        return dbKey;
    }

    public long getId() {
        return id;
    }

    public long getAssetId() {
        return assetId;
    }

    public long getSenderId() {
        return senderId;
    }

    public long getRecipientId() {
        return recipientId;
    }

    public long getQuantityQnt() {
        return quantityQnt;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getHeight() {
        return height;
    }

}
