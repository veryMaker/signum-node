package chain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import signumj.entity.SignumAddress;
import signumj.entity.SignumID;
import signumj.entity.SignumValue;
import signumj.entity.response.Account;
import signumj.entity.response.Asset;
import signumj.entity.response.AssetBalance;
import signumj.entity.response.TransactionBroadcast;
import signumj.service.TransactionBuilder;

import static chain.ChainUtils.*;

@RunWith(JUnit4.class)
public class AssetTest {
    
    static String name = "TST";
    static String description = "TST token description";
    static SignumValue quantity = SignumValue.fromNQT(1000);
    static int decimals = 3;

    @BeforeClass
    public static void setUpTest() {
        assertTrue("Mock node did not responded in time", setupNode());
    }

    @Test
    public void testIssueAsset() {
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.ISSUE_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .name(name).description(description)
                .quantity(quantity)
                .decimals(decimals);

        TransactionBroadcast tx = confirm(PASS1, tb);

        Asset asset = nodeService.getAsset(tx.getTransactionId(), null, null, null).blockingGet();
        assertEquals(name, asset.getName());
        assertEquals(description, asset.getDescription());
        assertEquals(decimals, asset.getDecimals());
        assertEquals(ACCOUNT1, asset.getIssuer());
        assertEquals(ACCOUNT1, asset.getAccount());
        assertFalse(asset.getMintable());
        assertEquals(quantity, asset.getQuantity());
        
        Account account = nodeService.getAccount(ACCOUNT1).blockingGet();
        boolean found = false;
        for(AssetBalance a : account.getAssetBalances()) {
            if(a.getAssetId().equals(asset.getAssetId())) {
                assertEquals(quantity, a.getBalance());
                found = true;
            }
        }
        assertTrue("Asset not found on the issuer account", found);
    }

    @Test
    public void testMintNonMintable() {
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.ISSUE_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .name(name).description(description)
                .quantity(quantity)
                .decimals(decimals);

        TransactionBroadcast tx = confirm(PASS1, tb);

        Asset asset = nodeService.getAsset(tx.getTransactionId(), null, null, null).blockingGet();
        
        RuntimeException ex = null;
        // Mint some asset, should not be allowed
        tb = new TransactionBuilder(TransactionBuilder.MINT_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .asset(asset.getAssetId())
                .quantity(quantity);
        try {
            nodeService.generateTransaction(tb).blockingGet();
        }
        catch (RuntimeException e) {
            ex = e;
        }
        assertNotNull(ex);
    }

    @Test
    public void testIssueAssetMintable() {
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.ISSUE_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .name(name).description(description)
                .mintable(true)
                .quantity(quantity)
                .decimals(decimals);

        TransactionBroadcast tx = confirm(PASS1, tb);

        Asset asset = nodeService.getAsset(tx.getTransactionId(), null, null, null).blockingGet();
        
        // Mint some asset and check the results
        tb = new TransactionBuilder(TransactionBuilder.MINT_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .asset(asset.getAssetId())
                .quantity(quantity);
        tx = confirm(PASS1, tb);
        
        Account account = nodeService.getAccount(ACCOUNT1).blockingGet();
        for(AssetBalance a : account.getAssetBalances()) {
            if(a.getAssetId().equals(asset.getAssetId())) {
                assertEquals(quantity.multiply(2), a.getBalance());
            }
        }
    }

    @Test
    public void testIssueAssetMintableZeroQuantity() {
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.ISSUE_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .name(name).description(description)
                .mintable(true)
                .quantity(SignumValue.fromNQT(0))
                .decimals(decimals);

        TransactionBroadcast tx = confirm(PASS1, tb);

        Asset asset = nodeService.getAsset(tx.getTransactionId(), null, null, null).blockingGet();
        
        // Mint some asset and check the results
        tb = new TransactionBuilder(TransactionBuilder.MINT_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.01), 1440)
                .asset(asset.getAssetId())
                .quantity(quantity);
        tx = confirm(PASS1, tb);
        
        Account account = nodeService.getAccount(ACCOUNT1).blockingGet();
        for(AssetBalance a : account.getAssetBalances()) {
            if(a.getAssetId().equals(asset.getAssetId())) {
                assertEquals(quantity, a.getBalance());
            }
        }
    }

    @Test
    public void testTransferAssetOwnership() {
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.ISSUE_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .name(name).description(description)
                .mintable(true)
                .quantity(quantity)
                .decimals(decimals);
        TransactionBroadcast tx = confirm(PASS1, tb);

        Asset asset = nodeService.getAsset(tx.getTransactionId(), null, null, null).blockingGet();
        
        // Mint some asset and check the results
        tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET_OWNERSHIP,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .recipient(ACCOUNT2)
                .referencedTransactionFullHash(Hex.encodeHexString(tx.getFullHash()));
        confirm(PASS1, tb);
        forgeBlock(PASS1);
        
        asset = nodeService.getAsset(asset.getAssetId(), null, null, null).blockingGet();
        assertEquals(ACCOUNT1, asset.getIssuer());
        assertEquals(ACCOUNT2, asset.getAccount());
        
        // try to transfer again and it should not be possible
        Exception ex = null;
        try {
            tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET_OWNERSHIP,
                    ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                    .recipient(ACCOUNT2)
                    .referencedTransactionFullHash(Hex.encodeHexString(tx.getFullHash()));
            confirm(PASS1, tb);
        }
        catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        
        // transfer again it to another account
        tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET_OWNERSHIP,
                ACCOUNT2.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .recipient(ACCOUNT3)
                .referencedTransactionFullHash(Hex.encodeHexString(tx.getFullHash()));
        confirm(PASS2, tb);
        forgeBlock(PASS1);
        
        asset = nodeService.getAsset(asset.getAssetId(), null, null, null).blockingGet();
        assertEquals(ACCOUNT1, asset.getIssuer());
        assertEquals(ACCOUNT3, asset.getAccount());
    }
    
    @Test
    public void testTransferAsset() {
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.ISSUE_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .name(name).description(description)
                .mintable(true)
                .quantity(quantity)
                .decimals(decimals);
        TransactionBroadcast tx = confirm(PASS1, tb);

        Asset asset = nodeService.getAsset(tx.getTransactionId(), null, null, null).blockingGet();
        
        // Transfer some asset and check the results
        tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.1), 1440)
                .asset(asset.getAssetId())
                .recipient(ACCOUNT2)
                .quantity(quantity);
        tx = confirm(PASS1, tb);
        forgeBlock(PASS1);
        
        asset = nodeService.getAsset(asset.getAssetId(), null, null, null).blockingGet();
        
        assertEquals(1, asset.getNumberOfAccounts());
        
        Account account = nodeService.getAccount(ACCOUNT2).blockingGet();
        boolean found = false;
        for(AssetBalance a : account.getAssetBalances()) {
            if(a.getAssetId().equals(asset.getAssetId())) {
                assertEquals(quantity, a.getBalance());
                found = true;
            }
        }
        assertTrue("Asset not found on the receiver account", found);
    }
    
    @Test
    public void testSetTreasuryAccountsAndTokenOwnership() {
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.ISSUE_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .name(name).description(description)
                .mintable(true)
                .quantity(quantity)
                .decimals(decimals);
        TransactionBroadcast tx = confirm(PASS1, tb);

        Asset asset = nodeService.getAsset(tx.getTransactionId(), null, null, null).blockingGet();
        assertEquals(quantity, asset.getQuantityCirculating());
        
        // Transfer some asset and check the results
        tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.1), 1440)
                .asset(asset.getAssetId())
                .recipient(ACCOUNT2)
                .quantity(quantity);
        confirm(PASS1, tb);
        
        asset = nodeService.getAsset(asset.getAssetId(), null, null, null).blockingGet();
        
        assertEquals(1, asset.getNumberOfAccounts());
        
        Account account = nodeService.getAccount(ACCOUNT2).blockingGet();
        boolean found = false;
        for(AssetBalance a : account.getAssetBalances()) {
            if(a.getAssetId().equals(asset.getAssetId())) {
                assertEquals(quantity, a.getBalance());
                found = true;
            }
        }
        assertTrue("Asset not found on the receiver account", found);
        
        // set account 2 as a treasury
        tb = new TransactionBuilder(TransactionBuilder.ADD_ASSET_TREASURY,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.1), 1440)
                .referencedTransactionFullHash(tx.getFullHash())
                .recipient(ACCOUNT2);
        confirm(PASS1, tb);
        
        asset = nodeService.getAsset(asset.getAssetId(), null, null, null).blockingGet();
        assertEquals(0, asset.getQuantityCirculating().longValue());
        
        // Transfer the asset ownership and account2 should still be a treasury account
        tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET_OWNERSHIP,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .referencedTransactionFullHash(tx.getFullHash())
                .recipient(ACCOUNT2);
        confirm(PASS1, tb);
        forgeBlock();
        
        asset = nodeService.getAsset(asset.getAssetId(), null, null, null).blockingGet();
        assertEquals(ACCOUNT2, asset.getAccount());
        assertEquals(0, asset.getQuantityCirculating().longValue());
        
        // mint some new tokens with the new owner, which is a treasury, so circulating should be still 0
        tb = new TransactionBuilder(TransactionBuilder.MINT_ASSET,
                ACCOUNT2.getPublicKey(), SignumValue.fromSigna(0.1), 1440)
                .asset(tx.getTransactionId())
                .quantity(quantity);
        confirm(PASS2, tb);
        forgeBlock();
        
        asset = nodeService.getAsset(asset.getAssetId(), null, null, null).blockingGet();
        assertEquals(0, asset.getQuantityCirculating().longValue());
        
        // transfer the newly minted tokens and so there should be the quantity circulating
        tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET,
                ACCOUNT2.getPublicKey(), SignumValue.fromSigna(0.1), 1440)
                .recipient(ACCOUNT3)
                .asset(tx.getTransactionId())
                .quantity(quantity);
        confirm(PASS2, tb);
        
        asset = nodeService.getAsset(asset.getAssetId(), null, null, null).blockingGet();
        assertEquals(quantity, asset.getQuantityCirculating());
    }

    @Test
    public void testTransferAssetMulti() {
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.ISSUE_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .name(name).description(description)
                .mintable(true)
                .quantity(quantity)
                .decimals(decimals);
        TransactionBroadcast tx = confirm(PASS1, tb);
        Asset asset1 = nodeService.getAsset(tx.getTransactionId(), null, null, null).blockingGet();

        tb = new TransactionBuilder(TransactionBuilder.ISSUE_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .name(name).description(description)
                .mintable(true)
                .quantity(quantity)
                .decimals(decimals);
        tx = confirm(PASS1, tb);
        Asset asset2 = nodeService.getAsset(tx.getTransactionId(), null, null, null).blockingGet();

        // Transfer some asset and check the results
        HashMap<SignumID, SignumValue> idsAndQuantities = new HashMap<>();
        idsAndQuantities.put(asset1.getAssetId(), quantity);
        idsAndQuantities.put(asset2.getAssetId(), quantity);
        tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET_MULTI,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.1), 1440)
                .recipient(ACCOUNT2)
                .assetIdsAndQuantities(idsAndQuantities);
        tx = confirm(PASS1, tb);
        forgeBlock(PASS1);
        
        asset1 = nodeService.getAsset(asset1.getAssetId(), null, null, null).blockingGet();
        asset2 = nodeService.getAsset(asset2.getAssetId(), null, null, null).blockingGet();
        
        assertEquals(1, asset1.getNumberOfAccounts());
        assertEquals(1, asset2.getNumberOfAccounts());
        
        Account account = nodeService.getAccount(ACCOUNT2).blockingGet();
        boolean found1 = false;
        boolean found2 = false;
        for(AssetBalance a : account.getAssetBalances()) {
            if(a.getAssetId().equals(asset1.getAssetId())) {
                assertEquals(quantity, a.getBalance());
                found1 = true;
            }
            if(a.getAssetId().equals(asset2.getAssetId())) {
                assertEquals(quantity, a.getBalance());
                found2 = true;
            }
        }
        assertTrue("Asset 1 not found on the receiver account", found1);
        assertTrue("Asset 2 not found on the receiver account", found2);
    }

    @Test
    public void testBurnAsset() {
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.ISSUE_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .name(name).description(description)
                .mintable(true)
                .quantity(quantity)
                .decimals(decimals);
        TransactionBroadcast tx = confirm(PASS1, tb);

        Asset asset = nodeService.getAsset(tx.getTransactionId(), null, null, null).blockingGet();
        
        // Burn it all and check the results
        tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.1), 1440)
                .asset(asset.getAssetId())
                .recipient(SignumAddress.fromId(0L))
                .quantity(quantity);
        tx = confirm(PASS1, tb);
        forgeBlock(PASS1);
        
        asset = nodeService.getAsset(asset.getAssetId(), null, null, null).blockingGet();
        
        assertEquals(0, asset.getNumberOfAccounts());
        assertEquals(quantity, asset.getQuantityBurnt());
    }
    
    @Test
    public void testDistributeToHolders() {
        Random r = new Random();
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.ISSUE_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .name(name).description(description)
                .mintable(true)
                .quantity(quantity)
                .decimals(decimals);

        byte []utx = nodeService.generateTransaction(tb).blockingGet();
        assertTrue(tb.verify(utx));

        byte[] stx = crypto.signTransaction(PASS1, utx);
        TransactionBroadcast tx = nodeService.broadcastTransaction(stx).blockingGet();
        forgeBlock(PASS1, tx);

        Asset asset = nodeService.getAsset(tx.getTransactionId(), null, null, null).blockingGet();
        
        SignumValue holderQuantity = quantity.divide(100);

        // first group
        SignumAddress firstGroup = null;
        for (int i = 0; i < 10; i++) {
            firstGroup = crypto.getAddressFromPassphrase(Long.toString(r.nextLong()));
            tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET,
                    ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.01), 1440)
                    .asset(asset.getAssetId())
                    .recipient(firstGroup)
                    .quantity(holderQuantity);
            utx = nodeService.generateTransaction(tb).blockingGet();
            assertTrue(tb.verify(utx));
            
            stx = crypto.signTransaction(PASS1, utx);
            tx = nodeService.broadcastTransaction(stx).blockingGet();            
        }
        forgeBlock(PASS1, tx);
        
        // another 10 with half the quantity each
        SignumAddress secondGroup = null;
        for (int i = 0; i < 10; i++) {
            secondGroup = crypto.getAddressFromPassphrase(Long.toString(r.nextLong()));
            tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET,
                    ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.01), 1440)
                    .asset(asset.getAssetId())
                    .recipient(secondGroup)
                    .quantity(holderQuantity.divide(2));
            utx = nodeService.generateTransaction(tb).blockingGet();
            assertTrue(tb.verify(utx));
            
            stx = crypto.signTransaction(PASS1, utx);
            tx = nodeService.broadcastTransaction(stx).blockingGet();            
        }
        forgeBlock(PASS1, tx);
        
        asset = nodeService.getAsset(asset.getAssetId(), null, null, null).blockingGet();
        
        assertEquals(21, asset.getNumberOfAccounts());
        
        // Make the distribution
        SignumValue amount = SignumValue.fromSigna(10);
        tb = new TransactionBuilder(TransactionBuilder.DISTRIBUTE_TO_ASSET_HOLDERS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.1), 1440)
                .asset(asset.getAssetId())
                .quantityMinimum(holderQuantity)
                .amount(amount);
        tx = confirm(PASS1, tb);
        
        Account holder = nodeService.getAccount(firstGroup).blockingGet();
        assertEquals(amount.divide(10), holder.getBalance());
        
        Account belowMinimum = nodeService.getAccount(secondGroup).blockingGet();
        assertEquals(0L, belowMinimum.getBalance().longValue());
    }
    

}
