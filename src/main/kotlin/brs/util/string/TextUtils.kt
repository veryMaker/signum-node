package brs.util.string

import brs.objects.Constants
import java.util.*

fun String?.isInAlphabet(): Boolean {
    if (this == null) return true
    for (c in this.toLowerCase(Locale.ENGLISH).toCharArray()) {
        if (!Constants.ALPHABET.contains(c.toString())) return false
    }
    return true
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
