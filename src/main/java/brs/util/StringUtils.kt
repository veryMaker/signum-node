package brs.util

object StringUtils {
    fun countMatches(str: String, sub: String): Int {
        return if (str.isNotEmpty() && sub.isNotEmpty()) {
            var count = 0
            var idx = 0
            while (idx != -1) {
                count++
                idx += sub.length
                idx = str.indexOf(sub, idx)
            }
            count
        } else {
            0
        }
    }
}
