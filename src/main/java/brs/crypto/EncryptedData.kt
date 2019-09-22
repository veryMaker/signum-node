package brs.crypto

import brs.BurstException
import burst.kit.entity.BurstEncryptedMessage

import java.nio.ByteBuffer

// TODO replace this class with the one from burstkit4j
class EncryptedData(val data: ByteArray, val nonce: ByteArray) {

    val size: Int
        get() = data.size + nonce.size

    fun decrypt(myPrivateKey: ByteArray, theirPublicKey: ByteArray): ByteArray {
        return if (data.isEmpty()) {
            data
        } else burstCrypto.decryptMessage(BurstEncryptedMessage(data, nonce, false), myPrivateKey, theirPublicKey)
    }

    companion object {
        private val EMPTY_DATA = EncryptedData(ByteArray(0), ByteArray(0))

        fun encrypt(plaintext: ByteArray, myPrivateKey: ByteArray, theirPublicKey: ByteArray): EncryptedData {
            if (plaintext.isEmpty()) {
                return EMPTY_DATA
            }
            val message = burstCrypto.encryptBytesMessage(plaintext, myPrivateKey, theirPublicKey)
            return EncryptedData(message.data, message.nonce)
        }

        fun readEncryptedData(buffer: ByteBuffer, length: Int, maxLength: Int): EncryptedData {
            if (length == 0) {
                return EMPTY_DATA
            }
            if (length > maxLength) {
                throw BurstException.NotValidException("Max encrypted data length exceeded: $length")
            }
            val noteBytes = ByteArray(length)
            buffer.get(noteBytes)
            val noteNonceBytes = ByteArray(32)
            buffer.get(noteNonceBytes)
            return EncryptedData(noteBytes, noteNonceBytes)
        }
    }
}
