package brs.crypto

import burst.kit.crypto.BurstCrypto
import burst.kit.entity.BurstID
import org.bouncycastle.jcajce.provider.digest.MD5

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Crypto {
    internal val burstCrypto = BurstCrypto.getInstance()

    fun sha256(): MessageDigest {
        return burstCrypto.sha256
    }

    fun shabal256(): MessageDigest {
        return burstCrypto.shabal256
    }

    fun ripemd160(): MessageDigest {
        return burstCrypto.ripeMD160
    }

    fun md5(): MessageDigest {// TODO unit test
        try {
            return MessageDigest.getInstance("MD5") // TODO burstkit4j integration
        } catch (e: NoSuchAlgorithmException) {
            return MD5.Digest()
        }

    }

    fun getPublicKey(secretPhrase: String): ByteArray {
        return burstCrypto.getPublicKey(secretPhrase)
    }

    fun getPrivateKey(secretPhrase: String): ByteArray {
        return burstCrypto.getPrivateKey(secretPhrase)
    }

    fun sign(message: ByteArray, secretPhrase: String): ByteArray {
        return burstCrypto.sign(message, secretPhrase)
    }

    fun verify(signature: ByteArray, message: ByteArray, publicKey: ByteArray, enforceCanonical: Boolean): Boolean {
        return burstCrypto.verify(signature, message, publicKey, enforceCanonical)
    }

    fun aesEncrypt(plaintext: ByteArray, myPrivateKey: ByteArray, theirPublicKey: ByteArray): ByteArray {
        return burstCrypto.aesSharedEncrypt(plaintext, myPrivateKey, theirPublicKey)
    }

    fun aesEncrypt(plaintext: ByteArray, myPrivateKey: ByteArray, theirPublicKey: ByteArray, nonce: ByteArray): ByteArray {
        return burstCrypto.aesSharedEncrypt(plaintext, myPrivateKey, theirPublicKey, nonce)
    }

    fun aesDecrypt(ivCiphertext: ByteArray, myPrivateKey: ByteArray, theirPublicKey: ByteArray): ByteArray {
        return burstCrypto.aesSharedDecrypt(ivCiphertext, myPrivateKey, theirPublicKey)
    }

    fun aesDecrypt(ivCiphertext: ByteArray, myPrivateKey: ByteArray, theirPublicKey: ByteArray, nonce: ByteArray): ByteArray {
        return burstCrypto.aesSharedDecrypt(ivCiphertext, myPrivateKey, theirPublicKey, nonce)
    }

    fun getSharedSecret(myPrivateKey: ByteArray, theirPublicKey: ByteArray): ByteArray {
        return burstCrypto.getSharedSecret(myPrivateKey, theirPublicKey)
    }

    fun rsEncode(id: Long): String {
        return burstCrypto.rsEncode(BurstID.fromLong(id))
    }

    fun rsDecode(rsString: String): Long {
        return burstCrypto.rsDecode(rsString).signedLongId
    }
}//never
