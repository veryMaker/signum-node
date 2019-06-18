package brs.util

import brs.Constants
import java.util.*

object TextUtils {
    @JvmStatic
    fun isInAlphabet(input: String?): Boolean {
        if (input == null) return true
        for (c in input.toLowerCase(Locale.ENGLISH).toCharArray()) {
            if (!Constants.ALPHABET.contains(c.toString())) return false
        }
        return true
    }
}
