package brs.util

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.util.Locale

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

@RunWith(JUnit4::class)
class TextUtilsTest {
    @Test
    fun testIsInAlphabet() {
        assertFalse(TextUtils.isInAlphabet("This string should not be okay"))
        assertTrue(TextUtils.isInAlphabet("ThisStringShouldBeOkay"))
        assertFalse(TextUtils.isInAlphabet("ThisStringHasPunctuation!"))
        assertFalse(TextUtils.isInAlphabet(String(byteArrayOf(0x00, 0x01, 0x02))))

        Locale.setDefault(Locale.forLanguageTag("tr-TR"))
        assertTrue(TextUtils.isInAlphabet("ThisStringHasAnIInIt"))
    }
}
