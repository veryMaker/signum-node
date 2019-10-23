package brs.util.convert

import brs.BurstException
import brs.crypto.burstCrypto
import brs.crypto.rsEncode
import burst.kit.entity.BurstAddress
import org.bouncycastle.util.encoders.DecoderException
import org.bouncycastle.util.encoders.Hex
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

fun Long.rsAccount(): String {
    return "BURST-${this.rsEncode()}"
}

fun ByteArray.fullHashToId(): Long {
    return burstCrypto.hashToId(this).signedLongId
}

fun String?.emptyToNull(): String? {
    return if (this.isNullOrEmpty()) null else this
}

fun Long?.nullToZero(): Long {
    return this ?: 0
}

fun ByteArray?.emptyToNull(): ByteArray? { // TODO remove
    if (this == null) {
        return null
    }
    for (b in this) {
        if (b.toInt() != 0) {
            return this
        }
    }
    return null
}

fun String?.toBytes(): ByteArray {
    return this?.toByteArray(StandardCharsets.UTF_8) ?: ByteArray(0)
}

fun ByteArray.toUtf8String(): String {
    return String(this, StandardCharsets.UTF_8)
}

fun ByteBuffer.readString(numBytes: Int, maxLength: Int): String {
    if (numBytes > 3 * maxLength) {
        throw BurstException.NotValidException("Max parameter length exceeded")
    }
    val bytes = ByteArray(numBytes)
    this.get(bytes)
    return bytes.toUtf8String()
}

fun String?.truncate(replaceNull: String, limit: Int, dots: Boolean): String {
    return if (this == null) replaceNull else if (this.length > limit) this.substring(0, if (dots) limit - 3 else limit) + if (dots) "..." else "" else this
}

fun String.parseAccountId(): Long {
    // TODO don't construct BurstAddress
    val address = BurstAddress.fromEither(this)
    return address?.burstID?.signedLongId ?: 0
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
