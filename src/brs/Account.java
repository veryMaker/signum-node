package brs;

import static brs.Constants.AT_PUBLIC_KEY_BYTES;

import brs.crypto.Crypto;
import brs.crypto.EncryptedData;
import brs.db.SignumKey;
import brs.db.VersionedBatchEntityTable;
import brs.util.Convert;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO: Create JavaDocs and remove this
@SuppressWarnings({ "checkstyle:MissingJavadocTypeCheck", "checkstyle:MissingJavadocMethodCheck" })

public class Account {

    private static final Logger logger = Logger.getLogger(Account.class.getSimpleName());

    public final long id;
    public final SignumKey nxtKey;
    private final int creationHeight;
    private byte[] publicKey;
    private int keyHeight;
    private boolean isAutomatedTransaction;

    protected String name;
    protected String description;

    public static class Balance {
        public final long id;
        public final SignumKey nxtKey;

        protected long balanceNqt;
        protected long unconfirmedBalanceNqt;
        protected long forgedBalanceNqt;

        public Balance(long id) {
            this.id = id;
            this.nxtKey = accountSignumKeyFactory().newKey(this.id);
        }

        public void setForgedBalanceNqt(long forgedBalanceNqt) {
            this.forgedBalanceNqt = forgedBalanceNqt;
        }

        public void setUnconfirmedBalanceNqt(long unconfirmedBalanceNqt) {
            this.unconfirmedBalanceNqt = unconfirmedBalanceNqt;
        }

        public void setBalanceNqt(long balanceNqt) {
            this.balanceNqt = balanceNqt;
        }

        public long getId() {
            return id;
        }

        public long getBalanceNqt() {
            return balanceNqt;
        }

        public long getUnconfirmedBalanceNqt() {
            return unconfirmedBalanceNqt;
        }

        public long getForgedBalanceNqt() {
            return forgedBalanceNqt;
        }

        public void checkBalance() {
            Account.checkBalance(this.id, this.balanceNqt, this.unconfirmedBalanceNqt);
        }

    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public void setKeyHeight(int keyHeight) {
        this.keyHeight = keyHeight;
    }

    public void setIsAt(boolean isAutomatedTransaction) {
        this.isAutomatedTransaction = isAutomatedTransaction;
    }

    public boolean isAutomatedTransaction() {
        return this.isAutomatedTransaction;
    }

    public enum Event {
        BALANCE, UNCONFIRMED_BALANCE, ASSET_BALANCE, UNCONFIRMED_ASSET_BALANCE,
        LEASE_SCHEDULED, LEASE_STARTED, LEASE_ENDED

    }

    public static class AccountAsset {
        public final long accountId;
        public final long assetId;
        public final SignumKey signumKey;
        private long quantityQnt;
        private long unconfirmedQuantityQnt;
        private boolean isTreasury;

        protected AccountAsset(
                long accountId,
                long assetId,
                long quantityQnt,
                long unconfirmedQuantityQnt,
                SignumKey signumKey) {
            this.accountId = accountId;
            this.assetId = assetId;
            this.quantityQnt = quantityQnt;
            this.unconfirmedQuantityQnt = unconfirmedQuantityQnt;
            this.signumKey = signumKey;
            this.isTreasury = false;
        }

        public AccountAsset(
                SignumKey signumKey,
                long accountId,
                long assetId,
                long quantityQnt,
                long unconfirmedQuantityQnt) {
            this.accountId = accountId;
            this.assetId = assetId;
            this.signumKey = signumKey;
            this.quantityQnt = quantityQnt;
            this.unconfirmedQuantityQnt = unconfirmedQuantityQnt;
            this.isTreasury = false;
        }

        public long getAccountId() {
            return accountId;
        }

        public long getAssetId() {
            return assetId;
        }

        public long getQuantityQnt() {
            return quantityQnt;
        }

        public long getUnconfirmedQuantityQnt() {
            return unconfirmedQuantityQnt;
        }

        public void checkBalance() {
            Account.checkBalance(this.accountId, this.quantityQnt, this.unconfirmedQuantityQnt);
        }

        @Override
        public String toString() {
            return "AccountAsset account_id: "
                    + Convert.toUnsignedLong(accountId)
                    + " asset_id: "
                    + Convert.toUnsignedLong(assetId)
                    + " quantity: "
                    + quantityQnt
                    + " unconfirmedQuantity: "
                    + unconfirmedQuantityQnt;
        }

        public void setQuantityQnt(long quantityQnt) {
            this.quantityQnt = quantityQnt;
        }

        public void setUnconfirmedQuantityQnt(long unconfirmedQuantityQnt) {
            this.unconfirmedQuantityQnt = unconfirmedQuantityQnt;
        }

        public void setTreasury(boolean isTreasury) {
            this.isTreasury = isTreasury;
        }

        public boolean isTreasury() {
            return isTreasury;
        }

    }

    public static class RewardRecipientAssignment {
        public final Long accountId;
        private Long prevRecipientId;
        private Long recipientId;
        private int fromHeight;
        public final SignumKey signumKey;

        public RewardRecipientAssignment(
                Long accountId,
                Long prevRecipientId,
                Long recipientId,
                int fromHeight,
                SignumKey signumKey) {
            this.accountId = accountId;
            this.prevRecipientId = prevRecipientId;
            this.recipientId = recipientId;
            this.fromHeight = fromHeight;
            this.signumKey = signumKey;
        }

        public long getAccountId() {
            return accountId;
        }

        public long getPrevRecipientId() {
            return prevRecipientId;
        }

        public long getRecipientId() {
            return recipientId;
        }

        public int getFromHeight() {
            return fromHeight;
        }

        public void setRecipient(long newRecipientId, int fromHeight) {
            prevRecipientId = recipientId;
            recipientId = newRecipientId;
            this.fromHeight = fromHeight;
        }
    }

    static class DoubleSpendingException extends RuntimeException {

        DoubleSpendingException(String message) {
            super(message);
        }

    }

    private static SignumKey.LongKeyFactory<Account> accountSignumKeyFactory() {
        return Signum.getStores().getAccountStore().getAccountKeyFactory();
    }

    private static SignumKey.LongKeyFactory<Account.Balance> accountBalanceSignumKeyFactory() {
        return Signum.getStores().getAccountStore().getAccountBalanceKeyFactory();
    }

    private static VersionedBatchEntityTable<Account> accountTable() {
        return Signum.getStores().getAccountStore().getAccountTable();
    }

    private static VersionedBatchEntityTable<Account.Balance> accountBalanceTable() {
        return Signum.getStores().getAccountStore().getAccountBalanceTable();
    }

    public static Account getAccount(long id) {
        return id == 0 ? null : accountTable().get(accountSignumKeyFactory().newKey(id));
    }

    public static Account.Balance getAccountBalance(long id) {
        return id == 0 ? null
                : accountBalanceTable()
                        .get(accountBalanceSignumKeyFactory()
                                .newKey(id));
    }

    public static Account.AccountAsset getAccountAssetBalance(long id, long assetId) {
        return Signum.getStores().getAccountStore().getAccountAsset(id, assetId);
    }

    public long getId() {
        return id;
    }

    public static long getId(byte[] publicKey) {
        byte[] publicKeyHash = Crypto.sha256().digest(publicKey);
        return Convert.fullHashToId(publicKeyHash);
    }

    public static Account getOrAddAccount(long id) {
        Account account = getAccount(id);
        if (account == null) {
            account = new Account(id);
            accountTable().insert(account);
        }
        return account;
    }

    public Account(long id) {
        if (id != Crypto.rsDecode(Crypto.rsEncode(id))) {
            logger.log(Level.INFO, "CRITICAL ERROR: Reed-Solomon encoding fails for {0}", id);
        }
        this.id = id;
        this.nxtKey = accountSignumKeyFactory().newKey(this.id);
        this.creationHeight = Signum.getBlockchain().getHeight();
    }

    protected Account(long id, SignumKey signumKey, int creationHeight) {
        if (id != Crypto.rsDecode(Crypto.rsEncode(id))) {
            logger.log(Level.INFO, "CRITICAL ERROR: Reed-Solomon encoding fails for {0}", id);
        }
        this.id = id;
        this.nxtKey = signumKey;
        this.creationHeight = creationHeight;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public byte[] getPublicKey() {
        if (this.keyHeight == -1) {
            return null;
        }
        return publicKey;
    }

    public int getCreationHeight() {
        return creationHeight;
    }

    public int getKeyHeight() {
        return keyHeight;
    }

    public long getUnconfirmedBalanceNqt() {
        Balance balance = Account.getAccountBalance(id);
        return balance == null ? 0L : balance.getUnconfirmedBalanceNqt();
    }

    public long getBalanceNqt() {
        Balance balance = Account.getAccountBalance(id);
        return balance == null ? 0L : balance.getBalanceNqt();
    }

    public long getForgedBalanceNqt() {
        Balance balance = Account.getAccountBalance(id);
        return balance == null ? 0L : balance.getForgedBalanceNqt();
    }

    public EncryptedData encryptTo(byte[] data, String senderSecretPhrase) {
        if (getPublicKey() == null) {
            throw new IllegalArgumentException("Recipient account doesn't have a public key set");
        }
        return EncryptedData.encrypt(data, Crypto.getPrivateKey(senderSecretPhrase), publicKey);
    }

    public static EncryptedData encryptTo(
            byte[] data,
            String senderSecretPhrase,
            byte[] publicKey) {
        if (publicKey == null) {
            throw new IllegalArgumentException("public key required");
        }
        return EncryptedData.encrypt(data, Crypto.getPrivateKey(senderSecretPhrase), publicKey);
    }

    public byte[] decryptFrom(EncryptedData encryptedData, String recipientSecretPhrase) {
        if (getPublicKey() == null) {
            throw new IllegalArgumentException("Sender account doesn't have a public key set");
        }
        return encryptedData.decrypt(Crypto.getPrivateKey(recipientSecretPhrase), publicKey);
    }

    // returns true iff:
    // this.publicKey is set to null (in which case this.publicKey also gets set to
    // key)
    // or
    // this.publicKey is already set to an array equal to key
    public boolean setOrVerify(byte[] key, int height) {
        return Signum.getStores().getAccountStore().setOrVerify(this, key, height);
    }

    public void apply(byte[] key, int height) {
        if (!setOrVerify(key, this.creationHeight)) {
            throw new IllegalStateException("Public key mismatch");
        }
        if (this.publicKey == null) {
            throw new IllegalStateException(
                    "Public key has not been set for account " + Convert.toUnsignedLong(id)
                            + " at height " + height + ", key height is " + keyHeight);
        }
        if (this.keyHeight == -1 || this.keyHeight > height) {
            this.keyHeight = height;
            accountTable().insert(this);
        }
    }

    public static boolean checkIsAutomatedTransaction(Account account) {
        return Arrays.equals(account.getPublicKey(), AT_PUBLIC_KEY_BYTES);
    }

    private static void checkBalance(long accountId, long confirmed, long unconfirmed) {
        if (confirmed < 0) {
            throw new DoubleSpendingException("Negative balance or quantity ("
                    + confirmed
                    + ") for account "
                    + Convert.toUnsignedLong(accountId));
        }
        if (unconfirmed < 0) {
            throw new DoubleSpendingException("Negative unconfirmed balance or quantity ("
                    + unconfirmed
                    + ") for account "
                    + Convert.toUnsignedLong(accountId));
        }
        if (unconfirmed > confirmed) {
            throw new DoubleSpendingException("Unconfirmed ("
                    + unconfirmed
                    + ") exceeds confirmed ("
                    + confirmed
                    + ") balance or quantity for account "
                    + Convert.toUnsignedLong(accountId));
        }
    }

}
