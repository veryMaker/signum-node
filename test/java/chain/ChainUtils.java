package chain;

import brs.Burst;
import signumj.Constants;
import signumj.crypto.SignumCrypto;
import signumj.entity.SignumAddress;
import signumj.entity.response.Transaction;
import signumj.entity.response.TransactionBroadcast;
import signumj.service.NodeService;
import signumj.service.impl.HttpBurstNodeService;

public class ChainUtils {

    public static NodeService nodeService;
    public static SignumCrypto crypto;
    static final String PASS1 = "a test passphrase 1";
    static final String PASS2 = "a test passphrase 2";
    static final String PASS3 = "a test passphrase 3";
    static final String PASS4 = "a test passphrase 4";
    
    static final SignumAddress ACCOUNT1 = SignumCrypto.getInstance().getAddressFromPassphrase(PASS1);
    static final SignumAddress ACCOUNT2 = SignumCrypto.getInstance().getAddressFromPassphrase(PASS2);
    static final SignumAddress ACCOUNT3 = SignumCrypto.getInstance().getAddressFromPassphrase(PASS3);
    static final SignumAddress ACCOUNT4 = SignumCrypto.getInstance().getAddressFromPassphrase(PASS4);
    
    public static boolean setupNode() {
        if(nodeService != null) {
            return true;
        }
        
        // a mock node with memory DB
        String[] args = {"-l", "-c", "conf/junit"};
        Burst.main(args);

        crypto = SignumCrypto.getInstance();
        nodeService = new HttpBurstNodeService(Constants.HTTP_NODE_LOCAL_TESTNET, "mock-node-testing");
        long startupTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startupTime < 10000) {
            // we wait for the node to boot
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            try{
                nodeService.getBlockChainStatus().blockingGet();

                // make the accounts to have some balance
                forgeBlock(nodeService, PASS1);
                forgeBlock(nodeService, PASS2);
                forgeBlock(nodeService, PASS3);
                forgeBlock(nodeService, PASS4);

                return true;
            }
            catch (Exception e) {
            }
        }
        return false;
    }
    
    public static void shutdownNode() {
        nodeService = null;
        Burst.shutdown(false);
    }

    public static void forgeBlock(NodeService nodeService, String pass, TransactionBroadcast ... txs) {
        for (int i = 0; i < 4; i++) {
            // retries
            boolean allFound = true;
            for(TransactionBroadcast tx : txs){
                try {
                    Transaction txConfirmed = nodeService.getTransaction(tx.getTransactionId()).blockingGet();
                    if (txConfirmed.getBlockHeight() == Integer.MAX_VALUE) {
                        allFound = false;
                        break;
                    }
                }
                catch (Exception e){
                    allFound = false;
                }
            }
            if (allFound)
                break;

            forgeBlock(nodeService, pass);
        }
    }
    
    /**
     * Forge block by mock mining for the given passphrase with the given millis timeout.
     *
     * Just for testing purposes.
     */
    public static void forgeBlock(NodeService nodeService, String pass) {
        int timeout = 2000;
        try {
            Thread.sleep(200);
            long height = nodeService.getMiningInfoSingle().blockingGet().getHeight();
            long startTimer = System.currentTimeMillis();
            nodeService.submitNonce(pass, "0", null).blockingGet();
            while(true) {
                Thread.sleep(50);
                long newHeight = nodeService.getMiningInfoSingle().blockingGet().getHeight();
                long timeElapsed = System.currentTimeMillis() - startTimer;
                if(newHeight > height || timeElapsed > timeout) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }    
}
