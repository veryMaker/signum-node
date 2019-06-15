package brs.at;

import brs.Account;
import brs.Burst;
import brs.common.TestConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Burst.class, Account.class})
public class ATTest {
    @Before
    public void setUp() {
        AtTestHelper.setupMocks();
    }

    @Test
    public void testAddAt() {
        AtomicBoolean helloWorldReceived = new AtomicBoolean(false);
        AtTestHelper.setOnAtAdded(at -> {
            assertEquals("HelloWorld", at.getName());
            helloWorldReceived.set(true);
        });
        AT.addAT(0L, TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, "HelloWorld", "Hello World AT", AtTestHelper.HELLO_WORLD_CREATION_BYTES, Integer.MAX_VALUE);
        assertTrue(helloWorldReceived.get());

        AtomicBoolean echoReceived = new AtomicBoolean(false);
        AtTestHelper.setOnAtAdded(at -> {
            assertEquals("Echo", at.getName());
            echoReceived.set(true);
        });
        AT.addAT(0L, TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, "Echo", "Message Echo AT", AtTestHelper.ECHO_CREATION_BYTES, Integer.MAX_VALUE);
        assertTrue(echoReceived.get());

        AtomicBoolean tipThanksReceived = new AtomicBoolean(false);
        AtTestHelper.setOnAtAdded(at -> {
            assertEquals("TipThanks", at.getName());
            tipThanksReceived.set(true);
        });
        AT.addAT(0L, TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, "TipThanks", "Tip Thanks AT", AtTestHelper.TIP_THANKS_CREATION_BYTES, Integer.MAX_VALUE);
        assertTrue(tipThanksReceived.get());
    }
}
