package brs.util.byteArray

import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

fun ByteArray.zero() {
    for (i in this.indices) {
        this[i] = 0
    }
}

fun ByteArray.fillFromEnd(byte: Byte, length: Int) {
    fill(byte, this.size - length, this.size)
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

/**
 * Checks whether a part of this array equals the other array
 * @param other The other array
 * @param offset The offset within this array to start checking from
 * @param length The length to check
 * @throws IllegalArgumentException if `length <= other.size || offset + length <= this.size`
 */
fun ByteArray.partEquals(other: ByteArray, offset: Int, length: Int): Boolean {
    require(offset + length <= this.size)
    require(length <= other.size)
    for (i in 0 until length) {
        if (this[offset + i] != other[i]) return false
    }
    return true
}

fun Boolean.toLong(): Long = if (this) 1 else 0
