package brs.util

import brs.objects.Constants
import java.util.*

object TextUtils { // TODO extension function
    fun isInAlphabet(input: String?): Boolean {
        if (input == null) return true
        for (c in input.toLowerCase(Locale.ENGLISH).toCharArray()) {
            if (!Constants.ALPHABET.contains(c.toString())) return false
        }
        return true
    }
}

fun String.countMatches(sub: String): Int {
    return if (this.isNotEmpty() && sub.isNotEmpty()) {
        var count = 0
        var idx = 0
        while (idx != -1) {
            count++
            idx += sub.length
            idx = this.indexOf(sub, idx)
        }
        count
    } else {
        0
    }
}
