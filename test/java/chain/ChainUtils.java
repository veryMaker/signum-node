package chain;

import signumj.crypto.SignumCrypto;
import signumj.entity.SignumAddress;
import signumj.entity.response.Transaction;
import signumj.entity.response.TransactionBroadcast;
import signumj.service.NodeService;

public class ChainUtils {

    static NodeService nodeService;
    static SignumCrypto crypto;
    static final String PASS1 = "a test passphrase 1";
    static final String PASS2 = "a test passphrase 2";
    static final String PASS3 = "a test passphrase 3";
    static final String PASS4 = "a test passphrase 4";
    
    static final SignumAddress ACCOUNT1 = SignumCrypto.getInstance().getAddressFromPassphrase(PASS1);
    static final SignumAddress ACCOUNT2 = SignumCrypto.getInstance().getAddressFromPassphrase(PASS2);
    static final SignumAddress ACCOUNT3 = SignumCrypto.getInstance().getAddressFromPassphrase(PASS3);
    static final SignumAddress ACCOUNT4 = SignumCrypto.getInstance().getAddressFromPassphrase(PASS4);

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
