package brs.util

import brs.BurstException
import brs.crypto.burstCrypto
import brs.crypto.rsEncode
import burst.kit.crypto.BurstCrypto
import burst.kit.entity.BurstAddress
import org.bouncycastle.util.encoders.DecoderException
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import kotlin.math.abs

object Convert {
    private val burstCrypto = BurstCrypto.getInstance()

    val two64: BigInteger = BigInteger.valueOf(2).pow(64)

    fun parseAccountId(account: String): Long {
        // TODO don't construct BurstAddress
        val address = BurstAddress.fromEither(account)
        return address?.burstID?.signedLongId ?: 0
    }

    fun rsAccount(accountId: Long): String {
        return "BURST-${accountId.rsEncode()}"
    }

    fun fullHashToId(hash: ByteArray?): Long {
        return burstCrypto.hashToId(hash).signedLongId
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

    fun readString(buffer: ByteBuffer, numBytes: Int, maxLength: Int): String {
        if (numBytes > 3 * maxLength) {
            throw BurstException.NotValidException("Max parameter length exceeded")
        }
        val bytes = ByteArray(numBytes)
        buffer.get(bytes)
        return toString(bytes)
    }

    fun truncate(s: String?, replaceNull: String, limit: Int, dots: Boolean): String {
        return if (s == null) replaceNull else if (s.length > limit) s.substring(0, if (dots) limit - 3 else limit) + if (dots) "..." else "" else s
    }

    // overflow checking based on https://www.securecoding.cert.org/confluence/display/java/NUM00-J.+Detect+or+prevent+integer+overflow
    fun safeAdd(left: Long, right: Long): Long {
        if (if (right > 0)
                    left > java.lang.Long.MAX_VALUE - right
                else
                    left < java.lang.Long.MIN_VALUE - right) {
            throw ArithmeticException("Integer overflow")
        }
        return left + right
    }

    fun safeSubtract(left: Long, right: Long): Long {
        if (if (right > 0)
                    left < java.lang.Long.MIN_VALUE + right
                else
                    left > java.lang.Long.MAX_VALUE + right) {
            throw ArithmeticException("Integer overflow")
        }
        return left - right
    }

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

    fun safeDivide(left: Long, right: Long): Long {
        if (left == java.lang.Long.MIN_VALUE && right == -1L) {
            throw ArithmeticException("Integer overflow")
        }
        return left / right
    }

    fun safeNegate(a: Long): Long {
        if (a == java.lang.Long.MIN_VALUE) {
            throw ArithmeticException("Integer overflow")
        }
        return -a
    }

    fun safeAbs(a: Long): Long {
        if (a == java.lang.Long.MIN_VALUE) {
            throw ArithmeticException("Integer overflow")
        }
        return abs(a)
    }
}

fun Long.toUnsignedString(): String {
    return java.lang.Long.toUnsignedString(this)
}

fun String?.parseUnsignedLong(): Long {
    // TODO do we need nullable receiver?
    return if (this.isNullOrEmpty()) 0 else java.lang.Long.parseUnsignedLong(this)
}

fun ByteArray?.toHexString(): String {
    return if (this == null) "" else burstCrypto.toHexString(this)
}

fun String.parseHexString(): ByteArray {
    var hex = this
    try {
        if (hex.length % 2 != 0) {
            hex = hex.substring(0, hex.length - 1)
        }
        return Hex.decode(hex)
    } catch (e: DecoderException) {
        throw RuntimeException("Could not parse hex string $hex", e)
    }
}
