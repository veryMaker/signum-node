package brs.util.crypto

import brs.util.BurstException
import burst.kit.crypto.BurstCrypto
import burst.kit.entity.BurstEncryptedMessage
import burst.kit.entity.BurstID
import java.nio.ByteBuffer
import java.security.MessageDigest

internal val burstCrypto = BurstCrypto.getInstance()

object Crypto {
    fun sha256(): MessageDigest {
        return burstCrypto.sha256
    }

    fun shabal256(): MessageDigest {
        return burstCrypto.shabal256
    }

    fun ripeMD160(): MessageDigest {
        return burstCrypto.ripeMD160
    }

    fun md5(): MessageDigest { // TODO unit test
        return burstCrypto.mD5
    }

    fun getPublicKey(secretPhrase: String): ByteArray {
        return burstCrypto.getPublicKey(secretPhrase)
    }

    fun getPrivateKey(secretPhrase: String): ByteArray {
        return burstCrypto.getPrivateKey(secretPhrase)
    }

    fun encryptData(
        plaintext: ByteArray,
        myPrivateKey: ByteArray,
        theirPublicKey: ByteArray,
        isText: Boolean
    ): BurstEncryptedMessage {
        if (plaintext.isEmpty()) {
            return BurstEncryptedMessage(ByteArray(0), ByteArray(0), isText)
        }
        val message = burstCrypto.encryptBytesMessage(plaintext, myPrivateKey, theirPublicKey)
        return BurstEncryptedMessage(message.data, message.nonce, isText)
    }

    fun readEncryptedData(buffer: ByteBuffer, length: Int, maxLength: Int, isText: Boolean): BurstEncryptedMessage {
        if (length == 0) {
            return BurstEncryptedMessage(ByteArray(0), ByteArray(0), isText)
        }
        if (length > maxLength) {
            throw BurstException.NotValidException("Max encrypted data length exceeded: $length")
        }
        val noteBytes = ByteArray(length)
        buffer.get(noteBytes)
        val noteNonceBytes = ByteArray(32)
        buffer.get(noteNonceBytes)
        return BurstEncryptedMessage(noteBytes, noteNonceBytes, isText)
    }
}

fun ByteArray.verifySignature(signature: ByteArray, publicKey: ByteArray, enforceCanonical: Boolean): Boolean {
    return burstCrypto.verify(signature, this, publicKey, enforceCanonical)
}

fun ByteArray.signUsing(secretPhrase: String): ByteArray {
    return burstCrypto.sign(this, secretPhrase)
}

fun Long.rsEncode(): String {
    // TODO don't construct BurstID
    return burstCrypto.rsEncode(BurstID.fromLong(this))
}

fun String.rsDecode(): Long {
    // TODO don't construct BurstID
    return burstCrypto.rsDecode(this).signedLongId
}

fun Long.rsVerify(): Boolean {
    // TODO is this method necessary? Is it possible that this will ever fail?
    return this == this.rsEncode().rsDecode()
}
