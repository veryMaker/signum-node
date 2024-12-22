package brs;

import static brs.web.api.http.common.Parameters.*;
import static brs.web.api.http.common.ResultFields.*;

import brs.TransactionType.Payment;
import brs.at.AtConstants;
import brs.crypto.EncryptedData;
import brs.fluxcapacitor.FluxValues;
import brs.util.Convert;
import brs.util.JSON;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;

//TODO: Create JavaDocs and remove this
@SuppressWarnings({ "checkstyle:MissingJavadocTypeCheck", "checkstyle:MissingJavadocMethodCheck" })

public interface Attachment extends Appendix {

    TransactionType getTransactionType();

    public abstract class AbstractAttachment extends AbstractAppendix implements Attachment {

        protected AbstractAttachment(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
        }

        protected AbstractAttachment(JsonObject attachmentData) {
            super(attachmentData);
        }

        protected AbstractAttachment(byte version) {
            super(version);
        }

        protected AbstractAttachment(int blockchainHeight) {
            super(blockchainHeight);
        }

        @Override
        public final void validate(Transaction transaction)
                throws SignumException.ValidationException {
            getTransactionType().validateAttachment(transaction);
        }

        @Override
        public final void apply(
                Transaction transaction,
                Account senderAccount,
                Account recipientAccount) {
            getTransactionType().apply(transaction, senderAccount, recipientAccount);
        }

    }

    abstract class EmptyAttachment extends AbstractAttachment {

        private EmptyAttachment() {
            super((byte) 0);
        }

        @Override
        protected final int getMySize() {
            return 0;
        }

        @Override
        protected final void putMyBytes(ByteBuffer buffer) {
        }

        @Override
        protected final void putMyJson(JsonObject json) {
        }

        @Override
        final boolean verifyVersion(byte transactionVersion) {
            return true;
        }

    }

    EmptyAttachment ORDINARY_PAYMENT = new EmptyAttachment() {

        @Override
        protected String getAppendixName() {
            return "OrdinaryPayment";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Payment.ORDINARY;
        }

    };

    EmptyAttachment ASSET_ADD_TREASURY_ACCOUNT_ATTACHMENT = new EmptyAttachment() {

        @Override
        protected String getAppendixName() {
            return "AssetAddTreasuryAccount";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.ColoredCoins.ASSET_ADD_TREASURY_ACCOUNT;
        }

    };

    class PaymentMultiOutCreation extends AbstractAttachment {

        private final ArrayList<ArrayList<Long>> recipients = new ArrayList<>();

        PaymentMultiOutCreation(
                ByteBuffer buffer,
                byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);

            int numberOfRecipients = Byte.toUnsignedInt(buffer.get());
            HashMap<Long, Boolean> recipientOf = new HashMap<>(numberOfRecipients);

            for (int i = 0; i < numberOfRecipients; ++i) {
                long recipientId = buffer.getLong();
                long amountNqt = buffer.getLong();

                if (recipientOf.containsKey(recipientId)) {
                    throw new SignumException.NotValidException(
                            "Duplicate recipient on multi out transaction");
                }

                if (amountNqt <= 0) {
                    throw new SignumException.NotValidException(
                            "Insufficient amountNQT on multi out transaction");
                }

                recipientOf.put(recipientId, true);
                this.recipients.add(new ArrayList<>(Arrays.asList(recipientId, amountNqt)));
            }
            if (recipients.size() > Constants.MAX_MULTI_OUT_RECIPIENTS || recipients.size() <= 1) {
                throw new SignumException.NotValidException(
                        "Invalid number of recipients listed on multi out transaction");
            }
        }

        PaymentMultiOutCreation(JsonObject attachmentData)
                throws SignumException.NotValidException {
            super(attachmentData);

            JsonArray receipientsJson = JSON.getAsJsonArray(
                    attachmentData.get(RECIPIENTS_PARAMETER));
            HashMap<Long, Boolean> recipientOf = new HashMap<>();

            for (JsonElement recipientObject : receipientsJson) {
                JsonArray recipient = JSON.getAsJsonArray(recipientObject);

                long recipientId = new BigInteger(JSON.getAsString(recipient.get(0))).longValue();
                long amountNqt = JSON.getAsLong(recipient.get(1));
                if (recipientOf.containsKey(recipientId)) {
                    throw new SignumException.NotValidException(
                            "Duplicate recipient on multi out transaction");
                }

                if (amountNqt <= 0) {
                    throw new SignumException.NotValidException(
                            "Insufficient amountNQT on multi out transaction");
                }

                recipientOf.put(recipientId, true);
                this.recipients.add(new ArrayList<>(Arrays.asList(recipientId, amountNqt)));
            }
            if (receipientsJson.size() > Constants.MAX_MULTI_OUT_RECIPIENTS
                    || receipientsJson.size() <= 1) {
                throw new SignumException.NotValidException(
                        "Invalid number of recipients listed on multi out transaction");
            }
        }

        public PaymentMultiOutCreation(
                Collection<Entry<String, Long>> recipients,
                int blockchainHeight)
                throws SignumException.NotValidException {
            super(blockchainHeight);

            HashMap<Long, Boolean> recipientOf = new HashMap<>();
            for (Entry<String, Long> recipient : recipients) {
                long recipientId = (new BigInteger(recipient.getKey())).longValue();
                long amountNqt = recipient.getValue();
                if (recipientOf.containsKey(recipientId)) {
                    throw new SignumException.NotValidException(
                            "Duplicate recipient on multi out transaction");
                }

                if (amountNqt <= 0) {
                    throw new SignumException.NotValidException(
                            "Insufficient amountNQT on multi out transaction");
                }

                recipientOf.put(recipientId, true);
                this.recipients.add(new ArrayList<>(Arrays.asList(recipientId, amountNqt)));
            }
            if (recipients.size() > Constants.MAX_MULTI_OUT_RECIPIENTS || recipients.size() <= 1) {
                throw new SignumException.NotValidException(
                        "Invalid number of recipients listed on multi out transaction");
            }
        }

        @Override
        protected String getAppendixName() {
            return "MultiOutCreation";
        }

        @Override
        protected int getMySize() {
            return 1 + recipients.size() * 16;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.put((byte) this.recipients.size());
            this.recipients.forEach((a) -> {
                buffer.putLong(a.get(0));
                buffer.putLong(a.get(1));
            });
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            final JsonArray recipientsJson = new JsonArray();

            this.recipients.stream()
                    .map(recipient -> {
                        final JsonArray recipientJson = new JsonArray();
                        recipientJson.add(Convert.toUnsignedLong(recipient.get(0)));
                        recipientJson.add(recipient.get(1).toString());
                        return recipientJson;
                    }).forEach(recipientsJson::add);

            attachment.add(RECIPIENTS_RESPONSE, recipientsJson);
        }

        @Override
        public TransactionType getTransactionType() {
            return Payment.MULTI_OUT;
        }

        public Long getAmountNqt() {
            long amountNqt = 0;
            for (ArrayList<Long> recipient : recipients) {
                amountNqt = Convert.safeAdd(amountNqt, recipient.get(1));
            }
            return amountNqt;
        }

        public Collection<List<Long>> getRecipients() {
            return Collections.unmodifiableCollection(recipients);
        }

    }

    class PaymentMultiSameOutCreation extends AbstractAttachment {

        private final ArrayList<Long> recipients = new ArrayList<>();

        PaymentMultiSameOutCreation(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);

            int numberOfRecipients = Byte.toUnsignedInt(buffer.get());
            HashMap<Long, Boolean> recipientOf = new HashMap<>(numberOfRecipients);

            for (int i = 0; i < numberOfRecipients; ++i) {
                long recipientId = buffer.getLong();

                if (recipientOf.containsKey(recipientId)) {
                    throw new SignumException.NotValidException(
                            "Duplicate recipient on multi same out transaction");
                }

                recipientOf.put(recipientId, true);
                this.recipients.add(recipientId);
            }
            if (recipients.size() > Constants.MAX_MULTI_SAME_OUT_RECIPIENTS
                    || recipients.size() <= 1) {
                throw new SignumException.NotValidException(
                        "Invalid number of recipients listed on multi same out transaction");
            }
        }

        PaymentMultiSameOutCreation(JsonObject attachmentData)
                throws SignumException.NotValidException {
            super(attachmentData);

            JsonArray recipientsJson = JSON.getAsJsonArray(
                    attachmentData.get(RECIPIENTS_PARAMETER));
            HashMap<Long, Boolean> recipientOf = new HashMap<>();

            for (JsonElement recipient : recipientsJson) {
                long recipientId = new BigInteger(JSON.getAsString(recipient)).longValue();
                if (recipientOf.containsKey(recipientId)) {
                    throw new SignumException.NotValidException(
                            "Duplicate recipient on multi same out transaction");
                }

                recipientOf.put(recipientId, true);
                this.recipients.add(recipientId);
            }
            if (recipientsJson.size() > Constants.MAX_MULTI_SAME_OUT_RECIPIENTS
                    || recipientsJson.size() <= 1) {
                throw new SignumException.NotValidException(
                        "Invalid number of recipients listed on multi same out transaction");
            }
        }

        public PaymentMultiSameOutCreation(Collection<Long> recipients, int blockchainHeight)
                throws SignumException.NotValidException {
            super(blockchainHeight);

            HashMap<Long, Boolean> recipientOf = new HashMap<>();
            for (Long recipientId : recipients) {
                if (recipientOf.containsKey(recipientId)) {
                    throw new SignumException.NotValidException(
                            "Duplicate recipient on multi same out transaction");
                }

                recipientOf.put(recipientId, true);
                this.recipients.add(recipientId);
            }
            if (recipients.size() > Constants.MAX_MULTI_SAME_OUT_RECIPIENTS
                    || recipients.size() <= 1) {
                throw new SignumException.NotValidException(
                        "Invalid number of recipients listed on multi same out transaction");
            }
        }

        @Override
        protected String getAppendixName() {
            return "MultiSameOutCreation";
        }

        @Override
        protected int getMySize() {
            return 1 + recipients.size() * 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.put((byte) this.recipients.size());
            this.recipients.forEach(buffer::putLong);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            JsonArray recipients = new JsonArray();
            this.recipients.forEach(a -> recipients.add(Convert.toUnsignedLong(a)));
            attachment.add(RECIPIENTS_RESPONSE, recipients);
        }

        @Override
        public TransactionType getTransactionType() {
            return Payment.MULTI_SAME_OUT;
        }

        public Collection<Long> getRecipients() {
            return Collections.unmodifiableCollection(recipients);
        }

    }

    // the message payload is in the Appendix
    EmptyAttachment ARBITRARY_MESSAGE = new EmptyAttachment() {

        @Override
        protected String getAppendixName() {
            return "ArbitraryMessage";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.ARBITRARY_MESSAGE;
        }

    };

    EmptyAttachment AT_PAYMENT = new EmptyAttachment() {

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AutomatedTransactions.AT_PAYMENT;
        }

        @Override
        protected String getAppendixName() {
            return "AT Payment";
        }

    };

    class MessagingAliasAssignment extends AbstractAttachment {

        private final String aliasName;
        private final String aliasUri;
        private long tld;

        MessagingAliasAssignment(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            aliasName = Convert.readString(buffer, buffer.get(), Constants.MAX_ALIAS_LENGTH).trim();
            aliasUri = Convert.readString(
                    buffer,
                    buffer.getShort(),
                    Constants.MAX_ALIAS_URI_LENGTH).trim();
            if (getVersion() > 1) {
                tld = buffer.getLong();
            }
        }

        MessagingAliasAssignment(JsonObject attachmentData) {
            super(attachmentData);
            aliasName = (Convert.nullToEmpty(JSON.getAsString(
                    attachmentData.get(ALIAS_PARAMETER)))).trim();
            aliasUri = (Convert.nullToEmpty(JSON.getAsString(
                    attachmentData.get(URI_PARAMETER)))).trim();
            if (getVersion() > 1) {
                tld = Convert.parseUnsignedLong(JSON.getAsString(
                        attachmentData.get(TLD_PARAMETER)));
            }
        }

        public MessagingAliasAssignment(
                String aliasName,
                String aliasUri,
                long tld,
                int blockchainHeight) {
            super((byte) ((Signum.getFluxCapacitor().getValue(
                    FluxValues.SMART_ALIASES, blockchainHeight) ? 2
                            : Signum.getFluxCapacitor().getValue(
                                    FluxValues.DIGITAL_GOODS_STORE, blockchainHeight) ? 1 : 0)));

            this.aliasName = aliasName.trim();
            this.aliasUri = aliasUri.trim();
            this.tld = tld;
        }

        @Override
        protected String getAppendixName() {
            return "AliasAssignment";
        }

        @Override
        protected int getMySize() {
            return 1 + Convert.toBytes(aliasName).length + 2 + Convert.toBytes(aliasUri).length
                    + (getVersion() > 1 ? 8 : 0);
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            byte[] alias = Convert.toBytes(this.aliasName);
            byte[] uri = Convert.toBytes(this.aliasUri);
            buffer.put((byte) alias.length);
            buffer.put(alias);
            buffer.putShort((short) uri.length);
            buffer.put(uri);
            if (getVersion() > 1) {
                buffer.putLong(tld);
            }
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(ALIAS_RESPONSE, aliasName);
            attachment.addProperty(URI_RESPONSE, aliasUri);
            if (getVersion() > 1) {
                attachment.addProperty(TLD_RESPONSE, Convert.toUnsignedLong(tld));
            }
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.ALIAS_ASSIGNMENT;
        }

        public String getAliasName() {
            return aliasName;
        }

        public String getAliasUri() {
            return aliasUri;
        }

        public long getTld() {
            return tld;
        }
    }

    class MessagingTldAssignment extends AbstractAttachment {

        private final String tldName;

        MessagingTldAssignment(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            tldName = Convert.readString(buffer, buffer.get(), Constants.MAX_TLD_LENGTH).trim();
        }

        MessagingTldAssignment(JsonObject attachmentData) {
            super(attachmentData);
            tldName = (Convert.nullToEmpty(
                    JSON.getAsString(attachmentData.get(TLD_PARAMETER)))).trim();
        }

        public MessagingTldAssignment(String tldName, int blockchainHeight) {
            super(blockchainHeight);
            this.tldName = tldName.trim();
        }

        @Override
        protected String getAppendixName() {
            return "TLDAssignment";
        }

        @Override
        protected int getMySize() {
            return 1 + Convert.toBytes(tldName).length;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            byte[] alias = Convert.toBytes(this.tldName);
            buffer.put((byte) alias.length);
            buffer.put(alias);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(TLD_RESPONSE, tldName);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.TLD_ASSIGNMENT;
        }

        public String getTldName() {
            return tldName;
        }
    }

    class MessagingAliasSell extends AbstractAttachment {

        private String aliasName;
        private long aliasId;
        private final long priceNqt;

        MessagingAliasSell(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            if (getVersion() > 1) {
                this.aliasId = buffer.getLong();
            } else {
                this.aliasName = Convert.readString(
                        buffer,
                        buffer.get(),
                        Constants.MAX_ALIAS_LENGTH);
            }
            this.priceNqt = buffer.getLong();
        }

        MessagingAliasSell(JsonObject attachmentData) {
            super(attachmentData);
            if (getVersion() > 1) {
                this.aliasId = Convert.parseUnsignedLong(
                        JSON.getAsString(attachmentData.get(ALIAS_PARAMETER)));
            } else {
                this.aliasName = Convert.nullToEmpty(
                        JSON.getAsString(attachmentData.get(ALIAS_PARAMETER)));
            }
            this.priceNqt = JSON.getAsLong(attachmentData.get(PRICE_NQT_PARAMETER));
        }

        public MessagingAliasSell(String aliasName, long priceNqt, int blockchainHeight) {
            super(blockchainHeight);
            this.aliasName = aliasName;
            this.priceNqt = priceNqt;
        }

        public MessagingAliasSell(
                long aliasId,
                String aliasName,
                long priceNqt,
                int blockchainHeight) {
            super((byte) ((Signum.getFluxCapacitor().getValue(
                    FluxValues.SMART_ALIASES, blockchainHeight)
                            ? 2
                            : Signum.getFluxCapacitor().getValue(
                                    FluxValues.DIGITAL_GOODS_STORE, blockchainHeight)
                                            ? 1
                                            : 0)));
            this.aliasId = aliasId;
            this.aliasName = aliasName;
            this.priceNqt = priceNqt;
        }

        @Override
        protected String getAppendixName() {
            return "AliasSell";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.ALIAS_SELL;
        }

        @Override
        protected int getMySize() {
            return (getVersion() > 1 ? 8 : 1 + Convert.toBytes(aliasName).length) + 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            if (getVersion() > 1) {
                buffer.putLong(aliasId);
            } else {
                byte[] aliasBytes = Convert.toBytes(aliasName);
                buffer.put((byte) aliasBytes.length);
                buffer.put(aliasBytes);
            }
            buffer.putLong(priceNqt);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(
                    ALIAS_RESPONSE,
                    getVersion() > 1
                            ? Convert.toUnsignedLong(aliasId)
                            : aliasName);
            attachment.addProperty(PRICE_NQT_RESPONSE, priceNqt);
        }

        public String getAliasName() {
            return aliasName;
        }

        public long getAliasId() {
            return aliasId;
        }

        public long getPriceNqt() {
            return priceNqt;
        }

    }

    final class MessagingAliasBuy extends AbstractAttachment {

        private String aliasName;
        private long aliasId;

        MessagingAliasBuy(
                ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            if (getVersion() > 1) {
                aliasId = buffer.getLong();
            } else {
                this.aliasName = Convert.readString(
                        buffer,
                        buffer.get(),
                        Constants.MAX_ALIAS_LENGTH);
            }
        }

        MessagingAliasBuy(JsonObject attachmentData) {
            super(attachmentData);
            if (getVersion() > 1) {
                aliasId = Convert.parseUnsignedLong(
                        JSON.getAsString(attachmentData.get(ALIAS_PARAMETER)));
            }
            this.aliasName = Convert.nullToEmpty(
                    JSON.getAsString(attachmentData.get(ALIAS_PARAMETER)));
        }

        public MessagingAliasBuy(
                long aliasId,
                String aliasName,
                int blockchainHeight) {
            super((byte) ((Signum.getFluxCapacitor().getValue(
                    FluxValues.SMART_ALIASES, blockchainHeight)
                            ? 2
                            : Signum.getFluxCapacitor().getValue(
                                    FluxValues.DIGITAL_GOODS_STORE, blockchainHeight)
                                            ? 1
                                            : 0)));

            this.aliasId = aliasId;
            this.aliasName = aliasName;
        }

        @Override
        protected String getAppendixName() {
            return "AliasBuy";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.ALIAS_BUY;
        }

        @Override
        protected int getMySize() {
            return (getVersion() > 1 ? 8 : 1 + Convert.toBytes(aliasName).length);
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            if (getVersion() > 1) {
                buffer.putLong(aliasId);
            } else {
                byte[] aliasBytes = Convert.toBytes(aliasName);
                buffer.put((byte) aliasBytes.length);
                buffer.put(aliasBytes);
            }
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(ALIAS_RESPONSE, getVersion() > 1
                    ? Convert.toUnsignedLong(aliasId)
                    : aliasName);
        }

        public String getAliasName() {
            return aliasName;
        }

        public long getAliasId() {
            return aliasId;
        }
    }

    final class MessagingAccountInfo extends AbstractAttachment {

        private final String name;
        private final String description;

        MessagingAccountInfo(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            this.name = Convert.readString(buffer, buffer.get(), Constants.MAX_ACCOUNT_NAME_LENGTH);
            this.description = Convert.readString(
                    buffer, buffer.getShort(),
                    Constants.MAX_ACCOUNT_DESCRIPTION_LENGTH);
        }

        MessagingAccountInfo(JsonObject attachmentData) {
            super(attachmentData);
            this.name = Convert.nullToEmpty(JSON.getAsString(attachmentData.get(NAME_PARAMETER)));
            this.description = Convert.nullToEmpty(
                    JSON.getAsString(attachmentData.get(DESCRIPTION_PARAMETER)));
        }

        public MessagingAccountInfo(String name, String description, int blockchainHeight) {
            super(blockchainHeight);
            this.name = name;
            this.description = description;
        }

        @Override
        protected String getAppendixName() {
            return "AccountInfo";
        }

        @Override
        protected int getMySize() {
            return 1 + Convert.toBytes(name).length + 2 + Convert.toBytes(description).length;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            byte[] putName = Convert.toBytes(this.name);
            byte[] putDescription = Convert.toBytes(this.description);
            buffer.put((byte) putName.length);
            buffer.put(putName);
            buffer.putShort((short) putDescription.length);
            buffer.put(putDescription);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(NAME_RESPONSE, name);
            attachment.addProperty(DESCRIPTION_RESPONSE, description);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.ACCOUNT_INFO;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

    }

    class ColoredCoinsAssetIssuance extends AbstractAttachment {

        private final String name;
        private final String description;
        private final long quantityQnt;
        private final byte decimals;
        private final boolean mintable;

        ColoredCoinsAssetIssuance(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            this.name = Convert.readString(buffer, buffer.get(), Constants.MAX_ASSET_NAME_LENGTH);
            this.description = Convert.readString(
                    buffer,
                    buffer.getShort(),
                    Constants.MAX_ASSET_DESCRIPTION_LENGTH);
            this.quantityQnt = buffer.getLong();
            this.decimals = buffer.get();

            boolean mintable = false;
            if (super.getVersion() > 1) {
                mintable = buffer.get() == 1;
            }
            this.mintable = mintable;
        }

        ColoredCoinsAssetIssuance(JsonObject attachmentData) {
            super(attachmentData);
            this.name = JSON.getAsString(attachmentData.get(NAME_PARAMETER));
            this.description = Convert.nullToEmpty(
                    JSON.getAsString(attachmentData.get(DESCRIPTION_PARAMETER)));
            this.quantityQnt = JSON.getAsLong(attachmentData.get(QUANTITY_QNT_PARAMETER));
            this.decimals = JSON.getAsByte(attachmentData.get(DECIMALS_PARAMETER));
            this.mintable = Boolean.TRUE.equals(
                    JSON.getAsBoolean(attachmentData.get(MINTABLE_PARAMETER)));
        }

        public ColoredCoinsAssetIssuance(
                String name,
                String description,
                long quantityQnt,
                byte decimals,
                int blockchainHeight,
                boolean mintable) {
            super((byte) (mintable ? 2
                    : Signum.getFluxCapacitor().getValue(
                            FluxValues.DIGITAL_GOODS_STORE, blockchainHeight)
                                    ? 1
                                    : 0));
            this.name = name;
            this.description = Convert.nullToEmpty(description);
            this.quantityQnt = quantityQnt;
            this.decimals = decimals;
            this.mintable = mintable;
        }

        @Override
        protected String getAppendixName() {
            return "AssetIssuance";
        }

        @Override
        protected int getMySize() {
            return 1
                    + Convert.toBytes(name).length
                    + 2
                    + Convert.toBytes(description).length
                    + 8
                    + 1
                    + (super.getVersion() > 1 ? 1 : 0);
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            byte[] name = Convert.toBytes(this.name);
            byte[] description = Convert.toBytes(this.description);
            buffer.put((byte) name.length);
            buffer.put(name);
            buffer.putShort((short) description.length);
            buffer.put(description);
            buffer.putLong(quantityQnt);
            buffer.put(decimals);
            if (super.getVersion() > 1) {
                buffer.put(mintable ? (byte) 1 : (byte) 0);
            }
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(NAME_RESPONSE, name);
            attachment.addProperty(DESCRIPTION_RESPONSE, description);
            attachment.addProperty(QUANTITY_QNT_RESPONSE, quantityQnt);
            attachment.addProperty(DECIMALS_RESPONSE, decimals);
            attachment.addProperty(MINTABLE_RESPONSE, mintable);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.ColoredCoins.ASSET_ISSUANCE;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public long getQuantityQnt() {
            return quantityQnt;
        }

        public byte getDecimals() {
            return decimals;
        }

        public boolean getMintable() {
            return mintable;
        }

    }

    final class ColoredCoinsAssetTransfer extends AbstractAttachment {

        private final long assetId;
        private final long quantityQnt;
        private final String comment;

        ColoredCoinsAssetTransfer(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            this.assetId = buffer.getLong();
            this.quantityQnt = buffer.getLong();
            this.comment = getVersion() == 0
                    ? Convert.readString(
                            buffer,
                            buffer.getShort(),
                            Constants.MAX_ASSET_TRANSFER_COMMENT_LENGTH)
                    : null;
        }

        ColoredCoinsAssetTransfer(JsonObject attachmentData) {
            super(attachmentData);
            this.assetId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(ASSET_PARAMETER)));
            this.quantityQnt = JSON.getAsLong(attachmentData.get(QUANTITY_QNT_PARAMETER));
            this.comment = getVersion() == 0
                    ? Convert.nullToEmpty(JSON.getAsString(attachmentData.get(COMMENT_PARAMETER)))
                    : null;
        }

        public ColoredCoinsAssetTransfer(long assetId, long quantityQnt, int blockchainHeight) {
            super(blockchainHeight);
            this.assetId = assetId;
            this.quantityQnt = quantityQnt;
            this.comment = null;
        }

        @Override
        protected String getAppendixName() {
            return "AssetTransfer";
        }

        @Override
        protected int getMySize() {
            return 8 + 8 + (getVersion() == 0 ? (2 + Convert.toBytes(comment).length) : 0);
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(assetId);
            buffer.putLong(quantityQnt);
            if (getVersion() == 0 && comment != null) {
                byte[] commentBytes = Convert.toBytes(this.comment);
                buffer.putShort((short) commentBytes.length);
                buffer.put(commentBytes);
            }
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(assetId));
            attachment.addProperty(QUANTITY_QNT_RESPONSE, quantityQnt);
            if (getVersion() == 0) {
                attachment.addProperty(COMMENT_RESPONSE, comment);
            }
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.ColoredCoins.ASSET_TRANSFER;
        }

        public long getAssetId() {
            return assetId;
        }

        public long getQuantityQnt() {
            return quantityQnt;
        }

        public String getComment() {
            return comment;
        }

    }

    final class ColoredCoinsAssetMultiTransfer extends AbstractAttachment {

        private final ArrayList<Long> assetIds;
        private final ArrayList<Long> quantitiesQnt;

        ColoredCoinsAssetMultiTransfer(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);

            int numberOfAssets = Byte.toUnsignedInt(buffer.get());
            if (numberOfAssets > Constants.MAX_MULTI_ASSET_IDS) {
                throw new SignumException.NotValidException("Invalid number of assets to transfer");
            }
            assetIds = new ArrayList<>(numberOfAssets);
            quantitiesQnt = new ArrayList<>(numberOfAssets);

            for (int i = 0; i < numberOfAssets; i++) {
                long assetId = buffer.getLong();
                long quantity = buffer.getLong();

                if (assetIds.contains(assetId)) {
                    throw new SignumException.NotValidException(
                            "No repeated assets in a multi transfer");
                }
                if (quantity <= 0) {
                    throw new SignumException.NotValidException(
                            "Insufficient quantityQNT on asset multi transfer");
                }
                assetIds.add(assetId);
                quantitiesQnt.add(quantity);
            }
        }

        ColoredCoinsAssetMultiTransfer(JsonObject attachmentData)
                throws SignumException.NotValidException {
            super(attachmentData);

            assetIds = new ArrayList<>();
            quantitiesQnt = new ArrayList<>();

            JsonArray assetIdsJsonArray = JSON.getAsJsonArray(
                    attachmentData.get(ASSET_IDS_RESPONSE));
            for (JsonElement assetIdJson : assetIdsJsonArray) {
                long assetId = Convert.parseUnsignedLong(assetIdJson.getAsString());
                if (assetIds.contains(assetId)) {
                    throw new SignumException.NotValidException(
                            "No repeated assets in a multi transfer");
                }
                assetIds.add(assetId);
            }
            JsonArray quantitiesJsonArray = JSON.getAsJsonArray(
                    attachmentData.get(QUANTITIES_QNT_RESPONSE));
            for (JsonElement quantityJson : quantitiesJsonArray) {
                long quantity = JSON.getAsLong(quantityJson);
                if (quantity <= 0) {
                    throw new SignumException.NotValidException(
                            "Insufficient quantityQNT on asset multi transfer");
                }
                quantitiesQnt.add(quantity);
            }

            if (assetIds.size() == 0 || assetIds.size() != quantitiesQnt.size()
                    || assetIds.size() > Constants.MAX_MULTI_ASSET_IDS) {
                throw new SignumException.NotValidException(
                        "Invalid asset/quantity for multi asset transfer");
            }
        }

        public ColoredCoinsAssetMultiTransfer(
                ArrayList<Long> assetIds,
                ArrayList<Long> quantitiesQnt,
                int blockchainHeight) {
            super(blockchainHeight);
            this.assetIds = assetIds;
            this.quantitiesQnt = quantitiesQnt;
        }

        @Override
        protected String getAppendixName() {
            return "AssetMultiTransfer";
        }

        @Override
        protected int getMySize() {
            return 1 + 8 * 2 * assetIds.size();
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.put((byte) assetIds.size());
            for (int i = 0; i < assetIds.size(); i++) {
                buffer.putLong(assetIds.get(i));
                buffer.putLong(quantitiesQnt.get(i));
            }
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            JsonArray assetIdsJson = new JsonArray();
            JsonArray quantitiesJson = new JsonArray();
            for (Long assetId : assetIds) {
                assetIdsJson.add(Convert.toUnsignedLong(assetId));
            }
            for (Long quantity : quantitiesQnt) {
                quantitiesJson.add(Convert.toUnsignedLong(quantity));
            }

            attachment.add(ASSET_IDS_RESPONSE, assetIdsJson);
            attachment.add(QUANTITIES_QNT_RESPONSE, quantitiesJson);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.ColoredCoins.ASSET_MULTI_TRANSFER;
        }

        public ArrayList<Long> getAssetIds() {
            return assetIds;
        }

        public ArrayList<Long> getQuantitiesQnt() {
            return quantitiesQnt;
        }
    }

    EmptyAttachment COLORED_COINS_ASSET_TRANSFER_OWNERSHIP = new EmptyAttachment() {

        @Override
        protected String getAppendixName() {
            return "AssetTransferOwnership";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.ColoredCoins.ASSET_TRANSFER_OWNERSHIP;
        }
    };

    final class ColoredCoinsAssetMint extends AbstractAttachment {

        private final long assetId;
        private final long quantityQnt;

        ColoredCoinsAssetMint(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            this.assetId = buffer.getLong();
            this.quantityQnt = buffer.getLong();
        }

        ColoredCoinsAssetMint(JsonObject attachmentData) {
            super(attachmentData);
            this.assetId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(ASSET_PARAMETER)));
            this.quantityQnt = JSON.getAsLong(attachmentData.get(QUANTITY_QNT_PARAMETER));
        }

        public ColoredCoinsAssetMint(long assetId, long quantityQnt, int blockchainHeight) {
            super(blockchainHeight);
            this.assetId = assetId;
            this.quantityQnt = quantityQnt;
        }

        @Override
        protected String getAppendixName() {
            return "AssetMint";
        }

        @Override
        protected int getMySize() {
            return 8 + 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(assetId);
            buffer.putLong(quantityQnt);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(assetId));
            attachment.addProperty(QUANTITY_QNT_RESPONSE, quantityQnt);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.ColoredCoins.ASSET_MINT;
        }

        public long getAssetId() {
            return assetId;
        }

        public long getQuantityQnt() {
            return quantityQnt;
        }

    }

    final class ColoredCoinsAssetDistributeToHolders extends AbstractAttachment {

        private final long assetId;
        private final long minimumAssetQuantityQnt;
        private final long assetIdToDistribute;
        private final long quantityQnt;

        ColoredCoinsAssetDistributeToHolders(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            this.assetId = buffer.getLong();
            this.minimumAssetQuantityQnt = buffer.getLong();
            this.assetIdToDistribute = buffer.getLong();
            this.quantityQnt = buffer.getLong();
        }

        ColoredCoinsAssetDistributeToHolders(JsonObject attachmentData) {
            super(attachmentData);
            this.assetId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(ASSET_PARAMETER)));
            this.minimumAssetQuantityQnt = JSON.getAsLong(
                    attachmentData.get(QUANTITY_MININUM_QNT_PARAMETER));
            this.assetIdToDistribute = Convert
                    .parseUnsignedLong(JSON.getAsString(
                            attachmentData.get(ASSET_TO_DISTRIBUTE_PARAMETER)));
            this.quantityQnt = JSON.getAsLong(attachmentData.get(QUANTITY_QNT_PARAMETER));
        }

        public ColoredCoinsAssetDistributeToHolders(
                long assetId,
                long minimumAssetQuantityQnt,
                long assetToDistribute,
                long quantityQnt,
                int blockchainHeight) {
            super(blockchainHeight);
            this.assetId = assetId;
            this.minimumAssetQuantityQnt = minimumAssetQuantityQnt;
            this.assetIdToDistribute = assetToDistribute;
            this.quantityQnt = quantityQnt;
        }

        @Override
        protected String getAppendixName() {
            return "AssetDistributeToHolders";
        }

        @Override
        protected int getMySize() {
            return 8 + 8 + 8 + 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(assetId);
            buffer.putLong(minimumAssetQuantityQnt);
            buffer.putLong(assetIdToDistribute);
            buffer.putLong(quantityQnt);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(assetId));
            attachment.addProperty(QUANTITY_MININUM_QNT_PARAMETER, minimumAssetQuantityQnt);
            attachment.addProperty(
                    ASSET_TO_DISTRIBUTE_PARAMETER,
                    Convert.toUnsignedLong(assetIdToDistribute));
            attachment.addProperty(QUANTITY_QNT_RESPONSE, quantityQnt);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.ColoredCoins.ASSET_DISTRIBUTE_TO_HOLDERS;
        }

        public long getAssetId() {
            return assetId;
        }

        public long getMinimumAssetQuantityQnt() {
            return minimumAssetQuantityQnt;
        }

        public long getAssetIdToDistribute() {
            return assetIdToDistribute;
        }

        public long getQuantityQnt() {
            return quantityQnt;
        }

    }

    abstract class ColoredCoinsOrderPlacement extends AbstractAttachment {

        private final long assetId;
        private final long quantityQnt;
        private final long priceNqt;

        private ColoredCoinsOrderPlacement(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.assetId = buffer.getLong();
            this.quantityQnt = buffer.getLong();
            this.priceNqt = buffer.getLong();
        }

        private ColoredCoinsOrderPlacement(JsonObject attachmentData) {
            super(attachmentData);
            this.assetId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(ASSET_PARAMETER)));
            this.quantityQnt = JSON.getAsLong(attachmentData.get(QUANTITY_QNT_PARAMETER));
            this.priceNqt = JSON.getAsLong(attachmentData.get(PRICE_NQT_PARAMETER));
        }

        private ColoredCoinsOrderPlacement(
                long assetId,
                long quantityQnt,
                long priceNqt,
                int blockchainHeight) {
            super(blockchainHeight);
            this.assetId = assetId;
            this.quantityQnt = quantityQnt;
            this.priceNqt = priceNqt;
        }

        @Override
        protected int getMySize() {
            return 8 + 8 + 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(assetId);
            buffer.putLong(quantityQnt);
            buffer.putLong(priceNqt);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(ASSET_RESPONSE, Convert.toUnsignedLong(assetId));
            attachment.addProperty(QUANTITY_QNT_RESPONSE, quantityQnt);
            attachment.addProperty(PRICE_NQT_RESPONSE, priceNqt);
        }

        public long getAssetId() {
            return assetId;
        }

        public long getQuantityQnt() {
            return quantityQnt;
        }

        public long getPriceNqt() {
            return priceNqt;
        }

    }

    final class ColoredCoinsAskOrderPlacement extends ColoredCoinsOrderPlacement {

        ColoredCoinsAskOrderPlacement(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
        }

        ColoredCoinsAskOrderPlacement(JsonObject attachmentData) {
            super(attachmentData);
        }

        public ColoredCoinsAskOrderPlacement(
                long assetId,
                long quantityQnt,
                long priceNqt,
                int blockchainHeight) {
            super(assetId, quantityQnt, priceNqt, blockchainHeight);
        }

        @Override
        protected String getAppendixName() {
            return "AskOrderPlacement";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.ColoredCoins.ASK_ORDER_PLACEMENT;
        }

    }

    final class ColoredCoinsBidOrderPlacement extends ColoredCoinsOrderPlacement {

        ColoredCoinsBidOrderPlacement(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
        }

        ColoredCoinsBidOrderPlacement(JsonObject attachmentData) {
            super(attachmentData);
        }

        public ColoredCoinsBidOrderPlacement(
                long assetId,
                long quantityQnt,
                long priceNqt,
                int blockchainHeight) {
            super(assetId, quantityQnt, priceNqt, blockchainHeight);
        }

        @Override
        protected String getAppendixName() {
            return "BidOrderPlacement";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.ColoredCoins.BID_ORDER_PLACEMENT;
        }

    }

    abstract class ColoredCoinsOrderCancellation extends AbstractAttachment {

        private final long orderId;

        private ColoredCoinsOrderCancellation(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.orderId = buffer.getLong();
        }

        private ColoredCoinsOrderCancellation(JsonObject attachmentData) {
            super(attachmentData);
            this.orderId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(ORDER_PARAMETER)));
        }

        private ColoredCoinsOrderCancellation(long orderId, int blockchainHeight) {
            super(blockchainHeight);
            this.orderId = orderId;
        }

        @Override
        protected int getMySize() {
            return 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(orderId);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(ORDER_RESPONSE, Convert.toUnsignedLong(orderId));
        }

        public long getOrderId() {
            return orderId;
        }

    }

    final class ColoredCoinsAskOrderCancellation extends ColoredCoinsOrderCancellation {

        ColoredCoinsAskOrderCancellation(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
        }

        ColoredCoinsAskOrderCancellation(JsonObject attachmentData) {
            super(attachmentData);
        }

        public ColoredCoinsAskOrderCancellation(long orderId, int blockchainHeight) {
            super(orderId, blockchainHeight);
        }

        @Override
        protected String getAppendixName() {
            return "AskOrderCancellation";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.ColoredCoins.ASK_ORDER_CANCELLATION;
        }

    }

    final class ColoredCoinsBidOrderCancellation extends ColoredCoinsOrderCancellation {

        ColoredCoinsBidOrderCancellation(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
        }

        ColoredCoinsBidOrderCancellation(JsonObject attachmentData) {
            super(attachmentData);
        }

        public ColoredCoinsBidOrderCancellation(long orderId, int blockchainHeight) {
            super(orderId, blockchainHeight);
        }

        @Override
        protected String getAppendixName() {
            return "BidOrderCancellation";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.ColoredCoins.BID_ORDER_CANCELLATION;
        }

    }

    final class DigitalGoodsListing extends AbstractAttachment {

        private final String name;
        private final String description;
        private final String tags;
        private final int quantity;
        private final long priceNqt;

        DigitalGoodsListing(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            this.name = Convert.readString(
                    buffer,
                    buffer.getShort(),
                    Constants.MAX_DGS_LISTING_NAME_LENGTH);
            this.description = Convert.readString(
                    buffer,
                    buffer.getShort(),
                    Constants.MAX_DGS_LISTING_DESCRIPTION_LENGTH);
            this.tags = Convert.readString(
                    buffer,
                    buffer.getShort(),
                    Constants.MAX_DGS_LISTING_TAGS_LENGTH);
            this.quantity = buffer.getInt();
            this.priceNqt = buffer.getLong();
        }

        DigitalGoodsListing(JsonObject attachmentData) {
            super(attachmentData);
            this.name = JSON.getAsString(attachmentData.get(NAME_RESPONSE));
            this.description = JSON.getAsString(attachmentData.get(DESCRIPTION_RESPONSE));
            this.tags = JSON.getAsString(attachmentData.get(TAGS_RESPONSE));
            this.quantity = JSON.getAsInt(attachmentData.get(QUANTITY_RESPONSE));
            this.priceNqt = JSON.getAsLong(attachmentData.get(PRICE_NQT_PARAMETER));
        }

        public DigitalGoodsListing(
                String name,
                String description,
                String tags,
                int quantity,
                long priceNqt,
                int blockchainHeight) {
            super(blockchainHeight);
            this.name = name;
            this.description = description;
            this.tags = tags;
            this.quantity = quantity;
            this.priceNqt = priceNqt;
        }

        @Override
        protected String getAppendixName() {
            return "DigitalGoodsListing";
        }

        @Override
        protected int getMySize() {
            return 2 + Convert.toBytes(name).length + 2 + Convert.toBytes(description).length + 2
                    + Convert.toBytes(tags).length + 4 + 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            byte[] nameBytes = Convert.toBytes(name);
            buffer.putShort((short) nameBytes.length);
            buffer.put(nameBytes);
            byte[] descriptionBytes = Convert.toBytes(description);
            buffer.putShort((short) descriptionBytes.length);
            buffer.put(descriptionBytes);
            byte[] tagsBytes = Convert.toBytes(tags);
            buffer.putShort((short) tagsBytes.length);
            buffer.put(tagsBytes);
            buffer.putInt(quantity);
            buffer.putLong(priceNqt);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(NAME_RESPONSE, name);
            attachment.addProperty(DESCRIPTION_RESPONSE, description);
            attachment.addProperty(TAGS_RESPONSE, tags);
            attachment.addProperty(QUANTITY_RESPONSE, quantity);
            attachment.addProperty(PRICE_NQT_RESPONSE, priceNqt);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.DigitalGoods.LISTING;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getTags() {
            return tags;
        }

        public int getQuantity() {
            return quantity;
        }

        public long getPriceNqt() {
            return priceNqt;
        }

    }

    final class DigitalGoodsDelisting extends AbstractAttachment {

        private final long goodsId;

        DigitalGoodsDelisting(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.goodsId = buffer.getLong();
        }

        DigitalGoodsDelisting(JsonObject attachmentData) {
            super(attachmentData);
            this.goodsId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(GOODS_PARAMETER)));
        }

        public DigitalGoodsDelisting(long goodsId, int blockchainHeight) {
            super(blockchainHeight);
            this.goodsId = goodsId;
        }

        @Override
        protected String getAppendixName() {
            return "DigitalGoodsDelisting";
        }

        @Override
        protected int getMySize() {
            return 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(goodsId);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(GOODS_RESPONSE, Convert.toUnsignedLong(goodsId));
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.DigitalGoods.DELISTING;
        }

        public long getGoodsId() {
            return goodsId;
        }

    }

    final class DigitalGoodsPriceChange extends AbstractAttachment {

        private final long goodsId;
        private final long priceNqt;

        DigitalGoodsPriceChange(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.goodsId = buffer.getLong();
            this.priceNqt = buffer.getLong();
        }

        DigitalGoodsPriceChange(JsonObject attachmentData) {
            super(attachmentData);
            this.goodsId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(GOODS_PARAMETER)));
            this.priceNqt = JSON.getAsLong(attachmentData.get(PRICE_NQT_PARAMETER));
        }

        public DigitalGoodsPriceChange(long goodsId, long priceNqt, int blockchainHeight) {
            super(blockchainHeight);
            this.goodsId = goodsId;
            this.priceNqt = priceNqt;
        }

        @Override
        protected String getAppendixName() {
            return "DigitalGoodsPriceChange";
        }

        @Override
        protected int getMySize() {
            return 8 + 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(goodsId);
            buffer.putLong(priceNqt);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(GOODS_RESPONSE, Convert.toUnsignedLong(goodsId));
            attachment.addProperty(PRICE_NQT_RESPONSE, priceNqt);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.DigitalGoods.PRICE_CHANGE;
        }

        public long getGoodsId() {
            return goodsId;
        }

        public long getPriceNqt() {
            return priceNqt;
        }

    }

    final class DigitalGoodsQuantityChange extends AbstractAttachment {

        private final long goodsId;
        private final int deltaQuantity;

        DigitalGoodsQuantityChange(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.goodsId = buffer.getLong();
            this.deltaQuantity = buffer.getInt();
        }

        DigitalGoodsQuantityChange(JsonObject attachmentData) {
            super(attachmentData);
            this.goodsId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(GOODS_PARAMETER)));
            this.deltaQuantity = JSON.getAsInt(attachmentData.get(DELTA_QUANTITY_PARAMETER));
        }

        public DigitalGoodsQuantityChange(long goodsId, int deltaQuantity, int blockchainHeight) {
            super(blockchainHeight);
            this.goodsId = goodsId;
            this.deltaQuantity = deltaQuantity;
        }

        @Override
        protected String getAppendixName() {
            return "DigitalGoodsQuantityChange";
        }

        @Override
        protected int getMySize() {
            return 8 + 4;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(goodsId);
            buffer.putInt(deltaQuantity);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(GOODS_RESPONSE, Convert.toUnsignedLong(goodsId));
            attachment.addProperty(DELTA_QUANTITY_RESPONSE, deltaQuantity);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.DigitalGoods.QUANTITY_CHANGE;
        }

        public long getGoodsId() {
            return goodsId;
        }

        public int getDeltaQuantity() {
            return deltaQuantity;
        }

    }

    final class DigitalGoodsPurchase extends AbstractAttachment {

        private final long goodsId;
        private final int quantity;
        private final long priceNqt;
        private final int deliveryDeadlineTimestamp;

        DigitalGoodsPurchase(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.goodsId = buffer.getLong();
            this.quantity = buffer.getInt();
            this.priceNqt = buffer.getLong();
            this.deliveryDeadlineTimestamp = buffer.getInt();
        }

        DigitalGoodsPurchase(JsonObject attachmentData) {
            super(attachmentData);
            this.goodsId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(GOODS_PARAMETER)));
            this.quantity = JSON.getAsInt(attachmentData.get(QUANTITY_PARAMETER));
            this.priceNqt = JSON.getAsLong(attachmentData.get(PRICE_NQT_PARAMETER));
            this.deliveryDeadlineTimestamp = JSON.getAsInt(
                    attachmentData.get(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER));
        }

        public DigitalGoodsPurchase(
                long goodsId,
                int quantity,
                long priceNqt,
                int deliveryDeadlineTimestamp,
                int blockchainHeight) {
            super(blockchainHeight);
            this.goodsId = goodsId;
            this.quantity = quantity;
            this.priceNqt = priceNqt;
            this.deliveryDeadlineTimestamp = deliveryDeadlineTimestamp;
        }

        @Override
        protected String getAppendixName() {
            return "DigitalGoodsPurchase";
        }

        @Override
        protected int getMySize() {
            return 8 + 4 + 8 + 4;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(goodsId);
            buffer.putInt(quantity);
            buffer.putLong(priceNqt);
            buffer.putInt(deliveryDeadlineTimestamp);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(GOODS_RESPONSE, Convert.toUnsignedLong(goodsId));
            attachment.addProperty(QUANTITY_RESPONSE, quantity);
            attachment.addProperty(PRICE_NQT_RESPONSE, priceNqt);
            attachment.addProperty(DELIVERY_DEADLINE_TIMESTAMP_RESPONSE, deliveryDeadlineTimestamp);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.DigitalGoods.PURCHASE;
        }

        public long getGoodsId() {
            return goodsId;
        }

        public int getQuantity() {
            return quantity;
        }

        public long getPriceNqt() {
            return priceNqt;
        }

        public int getDeliveryDeadlineTimestamp() {
            return deliveryDeadlineTimestamp;
        }

    }

    final class DigitalGoodsDelivery extends AbstractAttachment {

        private final long purchaseId;
        private final EncryptedData goods;
        private final long discountNqt;
        private final boolean goodsIsText;

        DigitalGoodsDelivery(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            this.purchaseId = buffer.getLong();
            int length = buffer.getInt();
            goodsIsText = length < 0;
            if (length < 0) {
                length &= Integer.MAX_VALUE;
            }
            this.goods = EncryptedData.readEncryptedData(
                    buffer,
                    length,
                    Constants.MAX_DGS_GOODS_LENGTH);
            this.discountNqt = buffer.getLong();
        }

        DigitalGoodsDelivery(JsonObject attachmentData) {
            super(attachmentData);
            this.purchaseId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(PURCHASE_PARAMETER)));
            this.goods = new EncryptedData(
                    Convert.parseHexString(
                            JSON.getAsString(attachmentData.get(GOODS_DATA_PARAMETER))),
                    Convert.parseHexString(
                            JSON.getAsString(attachmentData.get(GOODS_NONCE_PARAMETER))));
            this.discountNqt = JSON.getAsLong(attachmentData.get(DISCOUNT_NQT_PARAMETER));
            this.goodsIsText = Boolean.TRUE.equals(
                    JSON.getAsBoolean(attachmentData.get(GOODS_IS_TEXT_PARAMETER)));
        }

        public DigitalGoodsDelivery(
                long purchaseId,
                EncryptedData goods,
                boolean goodsIsText,
                long discountNqt,
                int blockchainHeight) {
            super(blockchainHeight);
            this.purchaseId = purchaseId;
            this.goods = goods;
            this.discountNqt = discountNqt;
            this.goodsIsText = goodsIsText;
        }

        @Override
        protected String getAppendixName() {
            return "DigitalGoodsDelivery";
        }

        @Override
        protected int getMySize() {
            return 8 + 4 + goods.getSize() + 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(purchaseId);
            buffer.putInt(goodsIsText
                    ? goods.getData().length | Integer.MIN_VALUE
                    : goods.getData().length);
            buffer.put(goods.getData());
            buffer.put(goods.getNonce());
            buffer.putLong(discountNqt);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(PURCHASE_RESPONSE, Convert.toUnsignedLong(purchaseId));
            attachment.addProperty(GOODS_DATA_RESPONSE, Convert.toHexString(goods.getData()));
            attachment.addProperty(GOODS_NONCE_RESPONSE, Convert.toHexString(goods.getNonce()));
            attachment.addProperty(DISCOUNT_NQT_RESPONSE, discountNqt);
            attachment.addProperty(GOODS_IS_TEXT_RESPONSE, goodsIsText);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.DigitalGoods.DELIVERY;
        }

        public long getPurchaseId() {
            return purchaseId;
        }

        public EncryptedData getGoods() {
            return goods;
        }

        public long getDiscountNqt() {
            return discountNqt;
        }

        public boolean goodsIsText() {
            return goodsIsText;
        }

    }

    final class DigitalGoodsFeedback extends AbstractAttachment {

        private final long purchaseId;

        DigitalGoodsFeedback(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.purchaseId = buffer.getLong();
        }

        DigitalGoodsFeedback(JsonObject attachmentData) {
            super(attachmentData);
            this.purchaseId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(PURCHASE_PARAMETER)));
        }

        public DigitalGoodsFeedback(long purchaseId, int blockchainHeight) {
            super(blockchainHeight);
            this.purchaseId = purchaseId;
        }

        @Override
        protected String getAppendixName() {
            return "DigitalGoodsFeedback";
        }

        @Override
        protected int getMySize() {
            return 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(purchaseId);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(PURCHASE_RESPONSE, Convert.toUnsignedLong(purchaseId));
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.DigitalGoods.FEEDBACK;
        }

        public long getPurchaseId() {
            return purchaseId;
        }

    }

    final class DigitalGoodsRefund extends AbstractAttachment {

        private final long purchaseId;
        private final long refundNqt;

        DigitalGoodsRefund(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.purchaseId = buffer.getLong();
            this.refundNqt = buffer.getLong();
        }

        DigitalGoodsRefund(JsonObject attachmentData) {
            super(attachmentData);
            this.purchaseId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(PURCHASE_PARAMETER)));
            this.refundNqt = JSON.getAsLong(attachmentData.get(REFUND_NQT_PARAMETER));
        }

        public DigitalGoodsRefund(long purchaseId, long refundNqt, int blockchainHeight) {
            super(blockchainHeight);
            this.purchaseId = purchaseId;
            this.refundNqt = refundNqt;
        }

        @Override
        protected String getAppendixName() {
            return "DigitalGoodsRefund";
        }

        @Override
        protected int getMySize() {
            return 8 + 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(purchaseId);
            buffer.putLong(refundNqt);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(PURCHASE_RESPONSE, Convert.toUnsignedLong(purchaseId));
            attachment.addProperty(REFUND_NQT_RESPONSE, refundNqt);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.DigitalGoods.REFUND;
        }

        public long getPurchaseId() {
            return purchaseId;
        }

        public long getRefundNqt() {
            return refundNqt;
        }

    }

    final class AccountControlEffectiveBalanceLeasing extends AbstractAttachment {

        private final short period;

        AccountControlEffectiveBalanceLeasing(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.period = buffer.getShort();
        }

        AccountControlEffectiveBalanceLeasing(JsonObject attachmentData) {
            super(attachmentData);
            this.period = JSON.getAsShort(attachmentData.get(PERIOD_PARAMETER));
        }

        public AccountControlEffectiveBalanceLeasing(short period, int blockchainHeight) {
            super(blockchainHeight);
            this.period = period;
        }

        @Override
        protected String getAppendixName() {
            return "EffectiveBalanceLeasing";
        }

        @Override
        protected int getMySize() {
            return 2;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putShort(period);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(PERIOD_RESPONSE, period);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AccountControl.EFFECTIVE_BALANCE_LEASING;
        }

        public short getPeriod() {
            return period;
        }

    }

    final class SignaMiningRewardRecipientAssignment extends AbstractAttachment {

        SignaMiningRewardRecipientAssignment(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
        }

        SignaMiningRewardRecipientAssignment(JsonObject attachmentData) {
            super(attachmentData);
        }

        public SignaMiningRewardRecipientAssignment(int blockchainHeight) {
            super(blockchainHeight);
        }

        @Override
        protected String getAppendixName() {
            return "RewardRecipientAssignment";
        }

        @Override
        protected int getMySize() {
            return 0;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            // Reward recipient does not have additional data.
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            // Reward recipient does not have additional data.
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.SignaMining.REWARD_RECIPIENT_ASSIGNMENT;
        }

    }

    abstract class CommitmentAttachment extends AbstractAttachment {

        private final long amountNqt;

        private CommitmentAttachment(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.amountNqt = buffer.getLong();
        }

        private CommitmentAttachment(JsonObject attachmentData) {
            super(attachmentData);
            this.amountNqt = JSON.getAsLong(attachmentData.get(AMOUNT_NQT_PARAMETER));
        }

        private CommitmentAttachment(long amountNqt, int blockchainHeight) {
            super(blockchainHeight);
            this.amountNqt = amountNqt;
        }

        public long getAmountNqt() {
            return amountNqt;
        }

        @Override
        protected int getMySize() {
            return 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(amountNqt);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(AMOUNT_NQT_RESPONSE, amountNqt);
        }

    }

    final class CommitmentAdd extends CommitmentAttachment {

        CommitmentAdd(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
        }

        CommitmentAdd(JsonObject attachmentData) {
            super(attachmentData);
        }

        public CommitmentAdd(long amountNqt, int blockchainHeight) {
            super(amountNqt, blockchainHeight);
        }

        @Override
        protected String getAppendixName() {
            return "CommitmentAdd";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.SignaMining.COMMITMENT_ADD;
        }

    }

    final class CommitmentRemove extends CommitmentAttachment {

        CommitmentRemove(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
        }

        CommitmentRemove(JsonObject attachmentData) {
            super(attachmentData);
        }

        public CommitmentRemove(long amountNqt, int blockchainHeight) {
            super(amountNqt, blockchainHeight);
        }

        @Override
        protected String getAppendixName() {
            return "CommitmentRemove";
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.SignaMining.COMMITMENT_REMOVE;
        }

    }

    final class AdvancedPaymentEscrowCreation extends AbstractAttachment {

        private final Long amountNqt;
        private final byte requiredSigners;
        private final SortedSet<Long> signers = new TreeSet<>();
        private final int deadline;
        private final Escrow.DecisionType deadlineAction;

        AdvancedPaymentEscrowCreation(ByteBuffer buffer, byte transactionVersion)
                throws SignumException.NotValidException {
            super(buffer, transactionVersion);
            this.amountNqt = buffer.getLong();
            this.deadline = buffer.getInt();
            this.deadlineAction = Escrow.byteToDecision(buffer.get());
            this.requiredSigners = buffer.get();
            byte totalSigners = buffer.get();
            if (totalSigners > 10 || totalSigners <= 0) {
                throw new SignumException.NotValidException(
                        "Invalid number of signers listed on create escrow transaction");
            }
            for (int i = 0; i < totalSigners; i++) {
                if (!this.signers.add(buffer.getLong())) {
                    throw new SignumException.NotValidException(
                            "Duplicate signer on escrow creation");
                }
            }
        }

        AdvancedPaymentEscrowCreation(JsonObject attachmentData)
                throws SignumException.NotValidException {
            super(attachmentData);
            this.amountNqt = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(AMOUNT_NQT_PARAMETER)));
            this.deadline = JSON.getAsInt(attachmentData.get(DEADLINE_PARAMETER));
            this.deadlineAction = Escrow
                    .stringToDecision(JSON.getAsString(
                            attachmentData.get(DEADLINE_ACTION_PARAMETER)));
            this.requiredSigners = JSON.getAsByte(attachmentData.get(REQUIRED_SIGNERS_PARAMETER));
            int totalSigners = (JSON.getAsJsonArray(attachmentData.get(SIGNERS_PARAMETER))).size();
            if (totalSigners > 10 || totalSigners <= 0) {
                throw new SignumException.NotValidException(
                        "Invalid number of signers listed on create escrow transaction");
            }
            JsonArray signersJson = JSON.getAsJsonArray(attachmentData.get(SIGNERS_PARAMETER));
            for (JsonElement eachSignerJson : signersJson) {
                this.signers.add(Convert.parseUnsignedLong(JSON.getAsString(eachSignerJson)));
            }
            if (this.signers.size() != (JSON.getAsJsonArray(
                    attachmentData.get(SIGNERS_PARAMETER))).size()) {
                throw new SignumException.NotValidException("Duplicate signer on escrow creation");
            }
        }

        public AdvancedPaymentEscrowCreation(
                Long amountNqt,
                int deadline,
                Escrow.DecisionType deadlineAction,
                int requiredSigners,
                Collection<Long> signers,
                int blockchainHeight)
                throws SignumException.NotValidException {
            super(blockchainHeight);
            this.amountNqt = amountNqt;
            this.deadline = deadline;
            this.deadlineAction = deadlineAction;
            this.requiredSigners = (byte) requiredSigners;
            if (signers.size() > 10 || signers.isEmpty()) {
                throw new SignumException.NotValidException(
                        "Invalid number of signers listed on create escrow transaction");
            }
            this.signers.addAll(signers);
            if (this.signers.size() != signers.size()) {
                throw new SignumException.NotValidException("Duplicate signer on escrow creation");
            }
        }

        @Override
        protected String getAppendixName() {
            return "EscrowCreation";
        }

        @Override
        protected int getMySize() {
            int size = 8 + 4 + 1 + 1 + 1;
            size += (signers.size() * 8);
            return size;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(this.amountNqt);
            buffer.putInt(this.deadline);
            buffer.put(Escrow.decisionToByte(this.deadlineAction));
            buffer.put(this.requiredSigners);
            byte totalSigners = (byte) this.signers.size();
            buffer.put(totalSigners);
            this.signers.forEach(buffer::putLong);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(AMOUNT_NQT_RESPONSE, Convert.toUnsignedLong(this.amountNqt));
            attachment.addProperty(DEADLINE_RESPONSE, this.deadline);
            attachment.addProperty(
                    DEADLINE_ACTION_RESPONSE,
                    Escrow.decisionToString(this.deadlineAction));
            attachment.addProperty(REQUIRED_SIGNERS_RESPONSE, (int) this.requiredSigners);
            JsonArray ids = new JsonArray();
            for (Long signer : this.signers) {
                ids.add(Convert.toUnsignedLong(signer));
            }
            attachment.add(SIGNERS_RESPONSE, ids);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AdvancedPayment.ESCROW_CREATION;
        }

        public Long getAmountNqt() {
            return amountNqt;
        }

        public int getDeadline() {
            return deadline;
        }

        public Escrow.DecisionType getDeadlineAction() {
            return deadlineAction;
        }

        public int getRequiredSigners() {
            return (int) requiredSigners;
        }

        public Collection<Long> getSigners() {
            return Collections.unmodifiableCollection(signers);
        }

        public int getTotalSigners() {
            return signers.size();
        }

    }

    final class AdvancedPaymentEscrowSign extends AbstractAttachment {

        private final Long escrowId;
        private final Escrow.DecisionType decision;

        AdvancedPaymentEscrowSign(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.escrowId = buffer.getLong();
            this.decision = Escrow.byteToDecision(buffer.get());
        }

        AdvancedPaymentEscrowSign(JsonObject attachmentData) {
            super(attachmentData);
            this.escrowId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(ESCROW_ID_PARAMETER)));
            this.decision = Escrow.stringToDecision(
                    JSON.getAsString(attachmentData.get(DECISION_PARAMETER)));
        }

        public AdvancedPaymentEscrowSign(
                Long escrowId,
                Escrow.DecisionType decision,
                int blockchainHeight) {
            super(blockchainHeight);
            this.escrowId = escrowId;
            this.decision = decision;
        }

        @Override
        protected String getAppendixName() {
            return "EscrowSign";
        }

        @Override
        protected int getMySize() {
            return 8 + 1;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(this.escrowId);
            buffer.put(Escrow.decisionToByte(this.decision));
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(ESCROW_ID_RESPONSE, Convert.toUnsignedLong(this.escrowId));
            attachment.addProperty(DECISION_RESPONSE, Escrow.decisionToString(this.decision));
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AdvancedPayment.ESCROW_SIGN;
        }

        public Long getEscrowId() {
            return this.escrowId;
        }

        public Escrow.DecisionType getDecision() {
            return this.decision;
        }

    }

    final class AdvancedPaymentEscrowResult extends AbstractAttachment {

        private final Long escrowId;
        private final Escrow.DecisionType decision;

        AdvancedPaymentEscrowResult(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.escrowId = buffer.getLong();
            this.decision = Escrow.byteToDecision(buffer.get());
        }

        AdvancedPaymentEscrowResult(JsonObject attachmentData) {
            super(attachmentData);
            this.escrowId = Convert.parseUnsignedLong(
                    JSON.getAsString(attachmentData.get(ESCROW_ID_PARAMETER)));
            this.decision = Escrow.stringToDecision(
                    JSON.getAsString(attachmentData.get(DECISION_PARAMETER)));
        }

        public AdvancedPaymentEscrowResult(
                Long escrowId,
                Escrow.DecisionType decision,
                int blockchainHeight) {
            super(blockchainHeight);
            this.escrowId = escrowId;
            this.decision = decision;
        }

        @Override
        protected String getAppendixName() {
            return "EscrowResult";
        }

        @Override
        protected int getMySize() {
            return 8 + 1;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(this.escrowId);
            buffer.put(Escrow.decisionToByte(this.decision));
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(ESCROW_ID_RESPONSE, Convert.toUnsignedLong(this.escrowId));
            attachment.addProperty(DECISION_RESPONSE, Escrow.decisionToString(this.decision));
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AdvancedPayment.ESCROW_RESULT;
        }

    }

    final class AdvancedPaymentSubscriptionSubscribe extends AbstractAttachment {

        private final Integer frequency;

        AdvancedPaymentSubscriptionSubscribe(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.frequency = buffer.getInt();
        }

        AdvancedPaymentSubscriptionSubscribe(JsonObject attachmentData) {
            super(attachmentData);
            this.frequency = JSON.getAsInt(attachmentData.get(FREQUENCY_PARAMETER));
        }

        public AdvancedPaymentSubscriptionSubscribe(int frequency, int blockchainHeight) {
            super(blockchainHeight);
            this.frequency = frequency;
        }

        @Override
        protected String getAppendixName() {
            return "SubscriptionSubscribe";
        }

        @Override
        protected int getMySize() {
            return 4;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putInt(this.frequency);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(FREQUENCY_RESPONSE, this.frequency);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AdvancedPayment.SUBSCRIPTION_SUBSCRIBE;
        }

        public Integer getFrequency() {
            return this.frequency;
        }

    }

    final class AdvancedPaymentSubscriptionCancel extends AbstractAttachment {

        private final Long subscriptionId;

        AdvancedPaymentSubscriptionCancel(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.subscriptionId = buffer.getLong();
        }

        AdvancedPaymentSubscriptionCancel(JsonObject attachmentData) {
            super(attachmentData);
            this.subscriptionId = Convert
                    .parseUnsignedLong(JSON.getAsString(
                            attachmentData.get(SUBSCRIPTION_ID_PARAMETER)));
        }

        public AdvancedPaymentSubscriptionCancel(Long subscriptionId, int blockchainHeight) {
            super(blockchainHeight);
            this.subscriptionId = subscriptionId;
        }

        @Override
        protected String getAppendixName() {
            return "SubscriptionCancel";
        }

        @Override
        protected int getMySize() {
            return 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(subscriptionId);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(
                    SUBSCRIPTION_ID_RESPONSE,
                    Convert.toUnsignedLong(this.subscriptionId));
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AdvancedPayment.SUBSCRIPTION_CANCEL;
        }

        public Long getSubscriptionId() {
            return this.subscriptionId;
        }

    }

    final class AdvancedPaymentSubscriptionPayment extends AbstractAttachment {

        private final Long subscriptionId;

        AdvancedPaymentSubscriptionPayment(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.subscriptionId = buffer.getLong();
        }

        AdvancedPaymentSubscriptionPayment(JsonObject attachmentData) {
            super(attachmentData);
            this.subscriptionId = Convert
                    .parseUnsignedLong(JSON.getAsString(
                            attachmentData.get(SUBSCRIPTION_ID_PARAMETER)));
        }

        public AdvancedPaymentSubscriptionPayment(Long subscriptionId, int blockchainHeight) {
            super(blockchainHeight);
            this.subscriptionId = subscriptionId;
        }

        @Override
        protected String getAppendixName() {
            return "SubscriptionPayment";
        }

        @Override
        protected int getMySize() {
            return 8;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(this.subscriptionId);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(
                    SUBSCRIPTION_ID_RESPONSE,
                    Convert.toUnsignedLong(this.subscriptionId));
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AdvancedPayment.SUBSCRIPTION_PAYMENT;
        }

        public long getSubscriptionId() {
            return subscriptionId;
        }

    }

    final class AutomatedTransactionsCreation extends AbstractAttachment {

        private final String name;
        private final String description;
        private final byte[] creationBytes;

        // TODO: See about removing this check suppression:
        // Option 1: Move variables closer to when they're needed
        // Option 2: Make variables final, if possible
        @SuppressWarnings("checkstyle:VariableDeclarationUsageDistanceCheck")

        AutomatedTransactionsCreation(ByteBuffer buffer,
                byte transactionVersion) throws SignumException.NotValidException {
            super(buffer, transactionVersion);

            this.name = Convert.readString(
                    buffer,
                    buffer.get(),
                    Constants.MAX_AUTOMATED_TRANSACTION_NAME_LENGTH);
            this.description = Convert.readString(buffer, buffer.getShort(),
                    Constants.MAX_AUTOMATED_TRANSACTION_DESCRIPTION_LENGTH);

            // rest of the parsing is at related; code comes from
            // public AtMachineState( byte[] atId, byte[] creator, byte[] creationBytes, int
            // height ) {
            int startPosition = buffer.position();
            short version = buffer.getShort();

            buffer.getShort(); // future: reserved for future needs

            int pageSize = (int) AtConstants.getInstance().pageSize(version);
            short codePages = buffer.getShort();
            short dataPages = buffer.getShort();
            buffer.getShort();
            buffer.getShort();

            buffer.getLong();

            int codeLen;
            if (codePages * pageSize < pageSize + 1) {
                codeLen = buffer.get();
                if (codeLen < 0) {
                    codeLen += (Byte.MAX_VALUE + 1) * 2;
                }
            } else if (codePages * pageSize < Short.MAX_VALUE + 1) {
                codeLen = buffer.getShort();
                if (codeLen < 0) {
                    codeLen += (Short.MAX_VALUE + 1) * 2;
                }
            } else {
                codeLen = buffer.getInt();
            }
            byte[] code = new byte[codeLen];
            buffer.get(code, 0, codeLen);

            int dataLen;
            if (dataPages * pageSize < 257) {
                dataLen = buffer.get();
                if (dataLen < 0) {
                    dataLen += (Byte.MAX_VALUE + 1) * 2;
                }
            } else if (dataPages * pageSize < Short.MAX_VALUE + 1) {
                dataLen = buffer.getShort();
                if (dataLen < 0) {
                    dataLen += (Short.MAX_VALUE + 1) * 2;
                }
            } else {
                dataLen = buffer.getInt();
            }

            if (codeLen == 0 && dataLen > 0 && dataLen < buffer.remaining()) {
                // fix problematic contracts with incorrect length,
                // so it at least the bytes are all loaded in the attachment.
                dataLen = buffer.remaining();
            }

            byte[] data = new byte[dataLen];
            buffer.get(data, 0, dataLen);

            int endPosition = buffer.position();
            buffer.position(startPosition);
            byte[] dst = new byte[endPosition - startPosition];
            buffer.get(dst, 0, endPosition - startPosition);
            this.creationBytes = dst;
        }

        AutomatedTransactionsCreation(JsonObject attachmentData) {
            super(attachmentData);

            this.name = JSON.getAsString(attachmentData.get(NAME_PARAMETER));
            this.description = JSON.getAsString(attachmentData.get(DESCRIPTION_PARAMETER));

            this.creationBytes = Convert.parseHexString(
                    JSON.getAsString(attachmentData.get(CREATION_BYTES_PARAMETER)));

        }

        public AutomatedTransactionsCreation(String name, String description, byte[] creationBytes,
                int blockchainHeight) {
            super(blockchainHeight);
            this.name = name;
            this.description = description;
            this.creationBytes = creationBytes;
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AutomatedTransactions.AUTOMATED_TRANSACTION_CREATION;
        }

        @Override
        protected String getAppendixName() {
            return "AutomatedTransactionsCreation";
        }

        @Override
        protected int getMySize() {
            return 1
                    + Convert.toBytes(name).length
                    + 2
                    + Convert.toBytes(description).length
                    + creationBytes.length;
        }

        @Override
        protected void putMyBytes(ByteBuffer buffer) {
            byte[] nameBytes = Convert.toBytes(name);
            buffer.put((byte) nameBytes.length);
            buffer.put(nameBytes);
            byte[] descriptionBytes = Convert.toBytes(description);
            buffer.putShort((short) descriptionBytes.length);
            buffer.put(descriptionBytes);

            buffer.put(creationBytes);
        }

        @Override
        protected void putMyJson(JsonObject attachment) {
            attachment.addProperty(NAME_RESPONSE, name);
            attachment.addProperty(DESCRIPTION_RESPONSE, description);
            attachment.addProperty(CREATION_BYTES_RESPONSE, Convert.toHexString(creationBytes));
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public byte[] getCreationBytes() {
            return creationBytes;
        }

    }

}
