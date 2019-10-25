package brs.util

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

fun ByteArray.zero() {
    for (i in this.indices) {
        this[i] = 0
    }
}

fun ByteArray.isZero(): Boolean {
    for (b in this) {
        if (b != 0.toByte()) return false
    }
    return true
}

fun ByteArray.xorWith(other: ByteArray) {
    for (i in this.indices) {
        this[i] = this[i] xor other[i]
    }
}

fun ByteArray.orWith(other: ByteArray) {
    for (i in this.indices) {
        this[i] = this[i] or other[i]
    }
}

fun ByteArray.andWith(other: ByteArray) {
    for (i in this.indices) {
        this[i] = this[i] and other[i]
    }
}

fun ByteArray.partEquals(other: ByteArray, offset: Int, length: Int): Boolean {
    require (offset + length <= this.size)
    require (length <= other.size)
    for (i in 0 until length) {
        if (this[offset + i] != other[i]) return false
    }
    return true
}

fun Boolean.toLong(): Long = if (this) 1 else 0
