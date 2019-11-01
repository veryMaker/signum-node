package brs.util

import brs.util.string.isInAlphabet
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class TextUtilsTest {
    @Test
    fun testIsInAlphabet() {
        assertFalse("This string should not be okay".isInAlphabet())
        assertTrue("ThisStringShouldBeOkay".isInAlphabet())
        assertFalse("ThisStringHasPunctuation!".isInAlphabet())
        assertFalse(String(byteArrayOf(0x00, 0x01, 0x02)).isInAlphabet())

        Locale.setDefault(Locale.forLanguageTag("tr-TR"))
        assertTrue("ThisStringHasAnIInIt".isInAlphabet())
    }
}
