package chain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.codec.binary.Hex;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import signumj.entity.SignumAddress;
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

        byte []utx = nodeService.generateTransaction(tb).blockingGet();
        assertTrue(tb.verify(utx));

        byte[] stx = crypto.signTransaction(PASS1, utx);
        TransactionBroadcast tx = nodeService.broadcastTransaction(stx).blockingGet();
        forgeBlock(nodeService, PASS1, tx);

        Asset asset = nodeService.getAsset(tx.getTransactionId()).blockingGet();
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

        byte []utx = nodeService.generateTransaction(tb).blockingGet();
        assertTrue(tb.verify(utx));

        byte[] stx = crypto.signTransaction(PASS1, utx);
        TransactionBroadcast tx = nodeService.broadcastTransaction(stx).blockingGet();
        forgeBlock(nodeService, PASS1, tx);

        Asset asset = nodeService.getAsset(tx.getTransactionId()).blockingGet();
        
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

        byte []utx = nodeService.generateTransaction(tb).blockingGet();
        assertTrue(tb.verify(utx));

        byte[] stx = crypto.signTransaction(PASS1, utx);
        TransactionBroadcast tx = nodeService.broadcastTransaction(stx).blockingGet();
        forgeBlock(nodeService, PASS1, tx);

        Asset asset = nodeService.getAsset(tx.getTransactionId()).blockingGet();
        
        // Mint some asset and check the results
        tb = new TransactionBuilder(TransactionBuilder.MINT_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .asset(asset.getAssetId())
                .quantity(quantity);
        utx = nodeService.generateTransaction(tb).blockingGet();
        assertTrue(tb.verify(utx));
        
        stx = crypto.signTransaction(PASS1, utx);
        tx = nodeService.broadcastTransaction(stx).blockingGet();
        forgeBlock(nodeService, PASS1, tx);
        
        Account account = nodeService.getAccount(ACCOUNT1).blockingGet();
        for(AssetBalance a : account.getAssetBalances()) {
            if(a.getAssetId().equals(asset.getAssetId())) {
                assertEquals(quantity.multiply(2), a.getBalance());
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

        byte []utx = nodeService.generateTransaction(tb).blockingGet();
        assertTrue(tb.verify(utx));

        byte[] stx = crypto.signTransaction(PASS1, utx);
        TransactionBroadcast tx = nodeService.broadcastTransaction(stx).blockingGet();
        forgeBlock(nodeService, PASS1, tx);

        Asset asset = nodeService.getAsset(tx.getTransactionId()).blockingGet();
        
        // Mint some asset and check the results
        tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET_OWNERSHIP,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(150), 1440)
                .recipient(ACCOUNT2)
                .referencedTransactionFullHash(Hex.encodeHexString(tx.getFullHash()));
        utx = nodeService.generateTransaction(tb).blockingGet();
        assertTrue(tb.verify(utx));
        
        stx = crypto.signTransaction(PASS1, utx);
        tx = nodeService.broadcastTransaction(stx).blockingGet();
        forgeBlock(nodeService, PASS1, tx);
        forgeBlock(nodeService, PASS1);
        
        asset = nodeService.getAsset(asset.getAssetId()).blockingGet();
        
        assertEquals(ACCOUNT1, asset.getIssuer());
        assertEquals(ACCOUNT2, asset.getAccount());
    }
    
    @Test
    public void testTransferAsset() {
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
        forgeBlock(nodeService, PASS1, tx);

        Asset asset = nodeService.getAsset(tx.getTransactionId()).blockingGet();
        
        // Transfer some asset and check the results
        tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.1), 1440)
                .asset(asset.getAssetId())
                .recipient(ACCOUNT2)
                .quantity(quantity);
        utx = nodeService.generateTransaction(tb).blockingGet();
        assertTrue(tb.verify(utx));
        
        stx = crypto.signTransaction(PASS1, utx);
        tx = nodeService.broadcastTransaction(stx).blockingGet();
        forgeBlock(nodeService, PASS1, tx);
        forgeBlock(nodeService, PASS1);
        
        asset = nodeService.getAsset(asset.getAssetId()).blockingGet();
        
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
    public void testBurnAsset() {
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
        forgeBlock(nodeService, PASS1, tx);

        Asset asset = nodeService.getAsset(tx.getTransactionId()).blockingGet();
        
        // Burn it all and check the results
        tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.1), 1440)
                .asset(asset.getAssetId())
                .recipient(SignumAddress.fromId(0L))
                .quantity(quantity);
        utx = nodeService.generateTransaction(tb).blockingGet();
        assertTrue(tb.verify(utx));
        
        stx = crypto.signTransaction(PASS1, utx);
        tx = nodeService.broadcastTransaction(stx).blockingGet();
        forgeBlock(nodeService, PASS1, tx);
        forgeBlock(nodeService, PASS1);
        
        asset = nodeService.getAsset(asset.getAssetId()).blockingGet();
        
        assertEquals(0, asset.getNumberOfAccounts());
        assertEquals(quantity, asset.getQuantityBurnt());
    }
    
    @Test
    public void testDistributeToHolders() {
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
        forgeBlock(nodeService, PASS1, tx);

        Asset asset = nodeService.getAsset(tx.getTransactionId()).blockingGet();
        
        SignumValue holderQuantity = quantity.divide(100);

        // transfer to holders, ids = 100, 101, etc.
        for (int i = 0; i < 10; i++) {
            tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET,
                    ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.01), 1440)
                    .asset(asset.getAssetId())
                    .recipient(SignumAddress.fromId(100 + i))
                    .quantity(holderQuantity);
            utx = nodeService.generateTransaction(tb).blockingGet();
            assertTrue(tb.verify(utx));
            
            stx = crypto.signTransaction(PASS1, utx);
            tx = nodeService.broadcastTransaction(stx).blockingGet();            
        }
        forgeBlock(nodeService, PASS1, tx);
        
        // another 10 with half the quantity each, ids = 200, 201, etc.
        for (int i = 0; i < 10; i++) {
            tb = new TransactionBuilder(TransactionBuilder.TRANSFER_ASSET,
                    ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.01), 1440)
                    .asset(asset.getAssetId())
                    .recipient(SignumAddress.fromId(200 + i))
                    .quantity(holderQuantity.divide(2));
            utx = nodeService.generateTransaction(tb).blockingGet();
            assertTrue(tb.verify(utx));
            
            stx = crypto.signTransaction(PASS1, utx);
            tx = nodeService.broadcastTransaction(stx).blockingGet();            
        }
        forgeBlock(nodeService, PASS1, tx);
        
        asset = nodeService.getAsset(asset.getAssetId()).blockingGet();
        
        assertEquals(21, asset.getNumberOfAccounts());
        
        // Make the distribution
        SignumValue amount = SignumValue.fromSigna(10);
        tb = new TransactionBuilder(TransactionBuilder.DISTRIBUTE_TO_ASSET_HOLDERS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(0.1), 1440)
                .asset(asset.getAssetId())
                .quantityMinimum(holderQuantity)
                .amount(amount);
        utx = nodeService.generateTransaction(tb).blockingGet();
        assertTrue(tb.verify(utx));
        stx = crypto.signTransaction(PASS1, utx);
        tx = nodeService.broadcastTransaction(stx).blockingGet();
        forgeBlock(nodeService, PASS1, tx);
        
        Account holder = nodeService.getAccount(SignumAddress.fromId(100L)).blockingGet();
        assertEquals(amount.divide(10), holder.getBalance());
        
        Account belowMinimum = nodeService.getAccount(SignumAddress.fromId(200L)).blockingGet();
        assertEquals(0L, belowMinimum.getBalance().longValue());
    }
    

}
