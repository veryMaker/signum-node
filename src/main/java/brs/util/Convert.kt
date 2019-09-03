package brs.util

import brs.BurstException
import brs.Constants
import brs.crypto.Crypto
import burst.kit.crypto.BurstCrypto
import burst.kit.entity.BurstAddress
import org.bouncycastle.util.encoders.DecoderException
import org.bouncycastle.util.encoders.Hex

import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Date

object Convert {
    private val burstCrypto = BurstCrypto.getInstance()

    private val multipliers = longArrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000)

    val two64: BigInteger = BigInteger.valueOf(2).pow(64)

    fun parseHexString(hexString: String): ByteArray {
        var hex = hexString
        try {
            if (hex.length % 2 != 0) {
                hex = hex.substring(0, hex.length - 1)
            }
            return Hex.decode(hex)
        } catch (e: DecoderException) {
            throw RuntimeException("Could not parse hex string $hex", e)
        }

    }

    // TODO these should be extension functions
    fun toHexString(bytes: ByteArray): String {
        return Hex.toHexString(bytes)
    }

    fun toUnsignedLong(objectId: Long): String {
        return java.lang.Long.toUnsignedString(objectId)
    }

    fun parseUnsignedLong(number: String?): Long {
        return if (number == null) {
            0
        } else java.lang.Long.parseUnsignedLong(number)
    }

    fun parseAccountId(account: String): Long {
        val address = BurstAddress.fromEither(account)
        return address?.burstID?.signedLongId ?: 0
    }

    fun rsAccount(accountId: Long): String {
        return "BURST-" + Crypto.rsEncode(accountId)
    }

    fun fullHashToId(hash: ByteArray?): Long {
        return burstCrypto.hashToId(hash).signedLongId
    }

    fun fullHashToId(hash: String?): Long {
        return if (hash == null) {
            0
        } else fullHashToId(parseHexString(hash))
    }

    fun fromEpochTime(epochTime: Int): Date {
        return Date(epochTime * 1000L + Constants.EPOCH_BEGINNING - 500L)
    }

    fun emptyToNull(s: String?): String? {
        return if (s == null || s.isEmpty()) null else s
    }

    fun nullToEmpty(s: String?): String {
        return s ?: ""
    }

    fun nullToZero(l: Long?): Long {
        return l ?: 0
    }

    fun emptyToNull(bytes: ByteArray?): ByteArray? {
        if (bytes == null) {
            return null
        }
        for (b in bytes) {
            if (b.toInt() != 0) {
                return bytes
            }
        }
        return null
    }

    fun toBytes(s: String?): ByteArray {
        return s?.toByteArray(StandardCharsets.UTF_8) ?: ByteArray(0)
    }

    fun toString(bytes: ByteArray): String {
        return String(bytes, StandardCharsets.UTF_8)
    }

    @Throws(BurstException.NotValidException::class)
    fun readString(buffer: ByteBuffer, numBytes: Int, maxLength: Int): String {
        if (numBytes > 3 * maxLength) {
            throw BurstException.NotValidException("Max parameter length exceeded")
        }
        val bytes = ByteArray(numBytes)
        buffer.get(bytes)
        return Convert.toString(bytes)
    }

    fun truncate(s: String?, replaceNull: String, limit: Int, dots: Boolean): String {
        return if (s == null) replaceNull else if (s.length > limit) s.substring(0, if (dots) limit - 3 else limit) + if (dots) "..." else "" else s
    }

    fun parseNXT(nxt: String): Long {
        return parseStringFraction(nxt, 8, Constants.MAX_BALANCE_BURST)
    }

    private fun parseStringFraction(value: String, decimals: Int, maxValue: Long): Long {
        val s = value.trim { it <= ' ' }.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (s.isEmpty() || s.size > 2) {
            throw NumberFormatException("Invalid number: $value")
        }
        val wholePart = java.lang.Long.parseLong(s[0])
        if (wholePart > maxValue) {
            throw IllegalArgumentException("Whole part of value exceeds maximum possible")
        }
        if (s.size == 1) {
            return wholePart * multipliers[decimals]
        }
        var fractionalPart = java.lang.Long.parseLong(s[1])
        if (fractionalPart >= multipliers[decimals] || s[1].length > decimals) {
            throw IllegalArgumentException("Fractional part exceeds maximum allowed divisibility")
        }
        for (i in s[1].length until decimals) {
            fractionalPart *= 10
        }
        return wholePart * multipliers[decimals] + fractionalPart
    }

    // overflow checking based on https://www.securecoding.cert.org/confluence/display/java/NUM00-J.+Detect+or+prevent+integer+overflow
    @Throws(ArithmeticException::class)
    fun safeAdd(left: Long, right: Long): Long {
        if (if (right > 0)
                    left > java.lang.Long.MAX_VALUE - right
                else
                    left < java.lang.Long.MIN_VALUE - right) {
            throw ArithmeticException("Integer overflow")
        }
        return left + right
    }

    @Throws(ArithmeticException::class)
    fun safeSubtract(left: Long, right: Long): Long {
        if (if (right > 0)
                    left < java.lang.Long.MIN_VALUE + right
                else
                    left > java.lang.Long.MAX_VALUE + right) {
            throw ArithmeticException("Integer overflow")
        }
        return left - right
    }

    @Throws(ArithmeticException::class)
    fun safeMultiply(left: Long, right: Long): Long {
        if (when {
            right > 0 -> left > java.lang.Long.MAX_VALUE / right || left < java.lang.Long.MIN_VALUE / right
            right < -1L -> left > java.lang.Long.MIN_VALUE / right || left < java.lang.Long.MAX_VALUE / right
            else -> right == -1L && left == java.lang.Long.MIN_VALUE
        }) {
            throw ArithmeticException("Integer overflow")
        }
        return left * right
    }

    @Throws(ArithmeticException::class)
    fun safeDivide(left: Long, right: Long): Long {
        if (left == java.lang.Long.MIN_VALUE && right == -1L) {
            throw ArithmeticException("Integer overflow")
        }
        return left / right
    }

    @Throws(ArithmeticException::class)
    fun safeNegate(a: Long): Long {
        if (a == java.lang.Long.MIN_VALUE) {
            throw ArithmeticException("Integer overflow")
        }
        return -a
    }

    @Throws(ArithmeticException::class)
    fun safeAbs(a: Long): Long {
        if (a == java.lang.Long.MIN_VALUE) {
            throw ArithmeticException("Integer overflow")
        }
        return Math.abs(a)
    }

}
