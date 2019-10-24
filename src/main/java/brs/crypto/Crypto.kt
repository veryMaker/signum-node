package brs.crypto

import burst.kit.crypto.BurstCrypto
import burst.kit.entity.BurstID
import org.bouncycastle.jcajce.provider.digest.MD5

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

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
        return try {
            MessageDigest.getInstance("MD5") // TODO burstkit4j integration
        } catch (e: NoSuchAlgorithmException) {
            MD5.Digest()
        }
    }

    fun getPublicKey(secretPhrase: String): ByteArray {
        return burstCrypto.getPublicKey(secretPhrase)
    }

    fun getPrivateKey(secretPhrase: String): ByteArray {
        return burstCrypto.getPrivateKey(secretPhrase)
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
