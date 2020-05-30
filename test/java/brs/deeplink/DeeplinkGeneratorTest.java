package brs.deeplink;

import com.google.zxing.WriterException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class DeeplinkGeneratorTest {
    private DeeplinkGenerator deeplinkGenerator;

    @Before
    public void setUpDeeplinkGeneratorTest() {
        deeplinkGenerator = new DeeplinkGenerator();
    }

    @Test
    public void testDeeplinkGenerator_Success() throws UnsupportedEncodingException {
        String result = deeplinkGenerator.generateDeepLink("generic", "testAction", "dGVzdERhdGE=");
        String expectedResult = "burst.generic://v1?action=testAction&payload=dGVzdERhdGE%3D";
        assertEquals(expectedResult, result);
    }

    @Test
    public void testDeeplinkGenerator_NoPayloadSuccess() throws UnsupportedEncodingException {
        String result = deeplinkGenerator.generateDeepLink("generic_test", "testAction", null);
        String expectedResult = "burst.generic_test://v1?action=testAction";
        assertEquals(expectedResult, result);
    }

    @Test
    public void testDeeplinkGenerator_InvalidDomain_Empty() throws UnsupportedEncodingException {
        try {
            deeplinkGenerator.generateDeepLink(" ", "testAction", null);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Domain. Must be alphanumeric with optional underscore");
        }
    }

    @Test
    public void testDeeplinkGenerator_InvalidDomain_WithWhitespace() throws UnsupportedEncodingException {
        try {
            deeplinkGenerator.generateDeepLink("test 123", "testAction", null);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Domain. Must be alphanumeric with optional underscore");
        }
    }

    @Test
    public void testDeeplinkGenerator_InvalidDomain_WithSpecialCahr() throws UnsupportedEncodingException {
        try {
            deeplinkGenerator.generateDeepLink("test-123", "testAction", null);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Domain. Must be alphanumeric with optional underscore");
        }
    }

    @Test
    public void testDeeplinkGenerator_PayloadLengthExceeded() throws UnsupportedEncodingException {

        StringBuilder s = new StringBuilder();
        for (int i = 0; i<=2048; ++i ){
            s.append("a");
        }

        try {
            deeplinkGenerator.generateDeepLink("generic", "testAction", s.toString());
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith( "Maximum Payload Length "));
        }
    }

    @Test
    public void testDeeplinkGenerator_QrCode() throws WriterException, UnsupportedEncodingException {
        BufferedImage bufferedImage = deeplinkGenerator.generateDeepLinkQrCode("generic", "testAction", "dGVzdERhdGE=");
        assertNotNull(bufferedImage);
    }
}
