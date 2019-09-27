package brs.util

fun ByteArray.isZero(): Boolean {
    for (b in this) {
        if (b != 0.toByte()) return false
    }
    return true
}