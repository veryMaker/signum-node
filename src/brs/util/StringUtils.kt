package brs.util

object StringUtils {
    @JvmStatic
    fun isEmpty(str: String?): Boolean {
        return str == null || str.isEmpty()
    }

    @JvmStatic
    fun countMatches(str: String, sub: String): Int {
        return if (!isEmpty(str) && !isEmpty(sub)) {
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
