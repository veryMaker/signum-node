package chain;

import static chain.ChainUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import signumj.entity.SignumAddress;
import signumj.entity.SignumID;
import signumj.entity.SignumValue;
import signumj.entity.response.Alias;
import signumj.entity.response.Subscription;
import signumj.entity.response.TLD;
import signumj.entity.response.TransactionBroadcast;
import signumj.service.TransactionBuilder;

@RunWith(JUnit4.class)
public class AliasTest {
    Random r = new Random();
    
    @BeforeClass
    public static void setUpTest() {
        assertTrue("Mock node did not responded in time", setupNode());
    }

    @Test
    public void testSetAliasNoTLD() {
        String aliasName = "alias" + Integer.toString(r.nextInt(10000));
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.SET_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.2), 1440)
                .alias(aliasName, null);
        confirm(PASS1, tb);

        Alias[] aliases = nodeService.getAliases(ACCOUNT1, aliasName, null, null, null, null).blockingGet();
        
        assertEquals(1, aliases.length);
        assertEquals(ACCOUNT1, aliases[0].getAccount());
        
        Subscription[] subs = nodeService.getAccountSubscriptions(ACCOUNT1).blockingGet();
        Subscription thisSub = null;
        for(Subscription sub : subs) {
            if(sub.getAlias().equals(aliases[0].getAlias())) {
                thisSub = sub;
                break;
            }
        }
        assertNotNull(thisSub);
        assertEquals(aliasName, thisSub.getAliasName());
    }
    
    @Test
    public void testSellAlias() {
        String aliasName = "alias" + Integer.toString(r.nextInt(10000));
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.SET_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.2), 1440)
                .alias(aliasName, null);
        TransactionBroadcast tx = confirm(PASS1, tb);

        SignumValue price = SignumValue.fromSigna(10);
        tb = new TransactionBuilder(TransactionBuilder.SELL_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.01), 1440)
                .alias(aliasName, tx.getTransactionId())
                .price(price)
                .recipient(ACCOUNT2);
        tx = confirm(PASS1, tb);
        
        Alias[] aliases = nodeService.getAliases(ACCOUNT1, aliasName, null, null, null, null).blockingGet();
        assertEquals(1, aliases.length);
        assertEquals(ACCOUNT1, aliases[0].getAccount());
        assertEquals(0, 0);
        
        Alias alias = aliases[0];
        assertEquals(price, alias.getPrice());
        assertEquals(ACCOUNT2, alias.getBuyer());
        
        // try to buy with a wrong account, should not be accepted
        Exception ex = null;
        try {
            tb = new TransactionBuilder(TransactionBuilder.BUY_ALIAS,
                    ACCOUNT3.getPublicKey(), SignumValue.fromSigna(.01), 1440)
                    .amount(price)
                    .alias(aliasName, null);
            tx = confirm(PASS3, tb);
        }
        catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        
        // try to buy for a smaller price, should fail
        ex = null;
        try {
            tb = new TransactionBuilder(TransactionBuilder.BUY_ALIAS,
                    ACCOUNT2.getPublicKey(), SignumValue.fromSigna(.01), 1440)
                    .amount(price.divide(2))
                    .alias(aliasName, null);
            tx = confirm(PASS2, tb);
        }
        catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        
        // finally buy it
        tb = new TransactionBuilder(TransactionBuilder.BUY_ALIAS,
                ACCOUNT2.getPublicKey(), SignumValue.fromSigna(.01), 1440)
                .amount(price)
                .alias(aliasName, alias.getAlias());
        tx = confirm(PASS2, tb);            

        // should not be on account 1
        aliases = nodeService.getAliases(ACCOUNT1, aliasName, null, null, null, null).blockingGet();
        assertEquals(0, aliases.length);
        
        // should be on account 2
        aliases = nodeService.getAliases(ACCOUNT2, aliasName, null, null, null, null).blockingGet();
        assertEquals(1, aliases.length);
        
        // including the subscription
        Subscription[] subs = nodeService.getAccountSubscriptions(ACCOUNT2).blockingGet();
        Subscription subFound = null;
        for(Subscription s : subs) {
            if(s.getAlias().equals(alias.getAlias())) {
                subFound = s;
                break;
            }
        }
        assertNotNull(subFound);
    }
    
    @Test
    public void testSellAliasPublic() {
        String aliasName = "alias" + Integer.toString(r.nextInt(10000));
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.SET_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.2), 1440)
                .alias(aliasName, null);
        TransactionBroadcast tx = confirm(PASS1, tb);

        SignumValue price = SignumValue.fromSigna(10);
        tb = new TransactionBuilder(TransactionBuilder.SELL_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.01), 1440)
                .alias(aliasName, tx.getTransactionId())
                .price(price);
        tx = confirm(PASS1, tb);
        
        Alias[] aliases = nodeService.getAliases(ACCOUNT1, aliasName, null, null, null, null).blockingGet();
        assertEquals(1, aliases.length);
        assertEquals(ACCOUNT1, aliases[0].getAccount());
        assertEquals(0, 0);
        
        Alias alias = aliases[0];
        assertEquals(price, alias.getPrice());
        assertNull(alias.getBuyer());
    }
    
    @Test
    public void testTransferAliasCancelSubscription() {
        String aliasName = "alias" + Integer.toString(r.nextInt(10000));
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.SET_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.2), 1440)
                .alias(aliasName, null);
        TransactionBroadcast tx = confirm(PASS1, tb);

        SignumValue price = SignumValue.fromSigna(0);
        tb = new TransactionBuilder(TransactionBuilder.SELL_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.01), 1440)
                .alias(aliasName, tx.getTransactionId())
                .price(price)
                .recipient(ACCOUNT2);
        tx = confirm(PASS1, tb);
        
        // the alias should not be on account 1 anymore
        Alias[] aliases = nodeService.getAliases(ACCOUNT1, aliasName, null, null, null, null).blockingGet();
        assertEquals(0, aliases.length);
        
        aliases = nodeService.getAliases(ACCOUNT2, aliasName, null, null, null, null).blockingGet();
        assertEquals(ACCOUNT2, aliases[0].getAccount());
        assertEquals(0, 0);
        
        Alias alias = aliases[0];
        assertEquals(price, alias.getPrice());
        assertNull(alias.getBuyer());
        
        // subscription should be on account 1 still
        Subscription[] subs = nodeService.getAccountSubscriptions(ACCOUNT1).blockingGet();
        Subscription subFound = null;
        for(Subscription s : subs) {
            if(s.getAlias().equals(alias.getAlias())) {
                subFound = s;
                break;
            }
        }
        assertNotNull(subFound);
        
        // if the subscription is cancelled, the alias should go away
        tb = new TransactionBuilder(TransactionBuilder.SUBSCRIPTION_CANCEL,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.01), 1440)
                .subscription(subFound.getId());
        tx = confirm(PASS1, tb);
        
        aliases = nodeService.getAliases(ACCOUNT2, aliasName, null, null, null, null).blockingGet();
        assertEquals(0, aliases.length);
    }

    @Test
    public void testTransferAlias() {
        String aliasName = "alias" + Integer.toString(r.nextInt(10000));
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.SET_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.2), 1440)
                .alias(aliasName, null);
        TransactionBroadcast tx = confirm(PASS1, tb);

        SignumValue price = SignumValue.fromSigna(0);
        tb = new TransactionBuilder(TransactionBuilder.SELL_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.01), 1440)
                .alias(aliasName, tx.getTransactionId())
                .price(price)
                .recipient(ACCOUNT2);
        tx = confirm(PASS1, tb);
        
        // the alias should not be on account 1 anymore
        Alias[] aliases = nodeService.getAliases(ACCOUNT1, aliasName, null, null, null, null).blockingGet();
        assertEquals(0, aliases.length);
        
        aliases = nodeService.getAliases(ACCOUNT2, aliasName, null, null, null, null).blockingGet();
        assertEquals(ACCOUNT2, aliases[0].getAccount());
        assertEquals(0, 0);
        
        Alias alias = aliases[0];
        assertEquals(price, alias.getPrice());
        assertNull(alias.getBuyer());
        
        // subscription should be on account 1 still
        Subscription[] subs = nodeService.getAccountSubscriptions(ACCOUNT1).blockingGet();
        Subscription subFound = null;
        for(Subscription s : subs) {
            if(s.getAlias().equals(alias.getAlias())) {
                subFound = s;
                break;
            }
        }
        assertNotNull(subFound);
        
        // if the alias is modified by the receiver, the subscription should be transferred
        String newURI = "a new URI";
        tb = new TransactionBuilder(TransactionBuilder.SET_ALIAS,
                ACCOUNT2.getPublicKey(), SignumValue.fromSigna(.2), 1440)
                .alias(aliasName, null)
                .aliasURI(newURI);
        tx = confirm(PASS2, tb);
        
        aliases = nodeService.getAliases(ACCOUNT2, aliasName, null, null, null, null).blockingGet();
        assertEquals(1, aliases.length);
        assertEquals(newURI, aliases[0].getAliasURI());
        
        // subscription should not be on account 1 anymore
        subs = nodeService.getAccountSubscriptions(ACCOUNT1).blockingGet();
        subFound = null;
        for(Subscription s : subs) {
            if(s.getAlias().equals(alias.getAlias())) {
                subFound = s;
                break;
            }
        }
        assertNull(subFound);
        
        // subscription should be on account 2 now
        subs = nodeService.getAccountSubscriptions(ACCOUNT2).blockingGet();
        subFound = null;
        for(Subscription s : subs) {
            if(s.getAlias().equals(alias.getAlias())) {
                subFound = s;
                break;
            }
        }
        assertNotNull(subFound);
    }
    
    @Test
    public void testTransferAliasToNull() {
        String aliasName = "alias" + Integer.toString(r.nextInt(10000));
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.SET_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.2), 1440)
                .alias(aliasName, null);
        TransactionBroadcast tx = confirm(PASS1, tb);

        SignumValue price = SignumValue.fromSigna(0);
        tb = new TransactionBuilder(TransactionBuilder.SELL_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.01), 1440)
                .alias(aliasName, tx.getTransactionId())
                .price(price)
                .recipient(SignumAddress.fromId(0L));
        
        Exception ex = null;
        try {
            // this should not be allowed in this case
            tx = confirm(PASS1, tb);
        }
        catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
    }

    
    @Test
    public void testSetAliasWithTLD() {
        String aliasName = "alias" + Integer.toString(r.nextInt(10000));
        String tld = "coin";
        SignumID tldId = null;
        
        TLD[] tlds = nodeService.getTLDs(null, null, null).blockingGet();
        for(TLD t : tlds) {
            if(t.getAliasName().equals(tld)) {
                tldId = t.getAlias();
            }
        }
        
        TransactionBuilder tb = new TransactionBuilder(TransactionBuilder.SET_ALIAS,
                ACCOUNT1.getPublicKey(), SignumValue.fromSigna(.2), 1440)
                .alias(aliasName, null)
                .tld(tld, tldId);
        confirm(PASS1, tb);

        Alias[] aliases = nodeService.getAliases(ACCOUNT1, aliasName, tld, null, null, null).blockingGet();
        
        assertEquals(1, aliases.length);
        assertEquals(ACCOUNT1, aliases[0].getAccount());
    }
}
