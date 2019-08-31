package brs

import brs.crypto.Crypto
import brs.util.Convert

class Token private constructor(val publicKey: ByteArray, val timestamp: Int, val isValid: Boolean) {
    companion object {

        fun generateToken(secretPhrase: String, websiteString: String, timestamp: Int): String {

            val website = Convert.toBytes(websiteString)
            val data = ByteArray(website.size + 32 + 4)
            System.arraycopy(website, 0, data, 0, website.size)
            System.arraycopy(Crypto.getPublicKey(secretPhrase), 0, data, website.size, 32)

            data[website.size + 32] = timestamp.toByte()
            data[website.size + 32 + 1] = (timestamp shr 8).toByte()
            data[website.size + 32 + 2] = (timestamp shr 16).toByte()
            data[website.size + 32 + 3] = (timestamp shr 24).toByte()

            val token = ByteArray(100)
            System.arraycopy(data, website.size, token, 0, 32 + 4)
            System.arraycopy(Crypto.sign(data, secretPhrase), 0, token, 32 + 4, 64)

            val buf = StringBuilder()
            var ptr = 0
            while (ptr < 100) {

                val number = ((token[ptr] and 0xFF).toLong() or ((token[ptr + 1] and 0xFF).toLong() shl 8) or ((token[ptr + 2] and 0xFF).toLong() shl 16)
                        or ((token[ptr + 3] and 0xFF).toLong() shl 24) or ((token[ptr + 4] and 0xFF).toLong() shl 32))

                if (number < 32) {
                    buf.append("0000000")
                } else if (number < 1024) {
                    buf.append("000000")
                } else if (number < 32768) {
                    buf.append("00000")
                } else if (number < 1048576) {
                    buf.append("0000")
                } else if (number < 33554432) {
                    buf.append("000")
                } else if (number < 1073741824) {
                    buf.append("00")
                } else if (number < 34359738368L) {
                    buf.append("0")
                }
                buf.append(java.lang.Long.toString(number, 32))
                ptr += 5

            }

            return buf.toString()

        }

        fun parseToken(tokenString: String, website: String): Token {

            val websiteBytes = Convert.toBytes(website)
            val tokenBytes = ByteArray(100)
            var i = 0
            var j = 0

            while (i < tokenString.length) {

                val number = java.lang.Long.parseLong(tokenString.substring(i, i + 8), 32)
                tokenBytes[j] = number.toByte()
                tokenBytes[j + 1] = (number shr 8).toByte()
                tokenBytes[j + 2] = (number shr 16).toByte()
                tokenBytes[j + 3] = (number shr 24).toByte()
                tokenBytes[j + 4] = (number shr 32).toByte()
                i += 8
                j += 5

            }

            require(i == 160) { "Invalid token string: $tokenString" }
            val publicKey = ByteArray(32)
            System.arraycopy(tokenBytes, 0, publicKey, 0, 32)
            val timestamp = tokenBytes[32] and 0xFF or (tokenBytes[33] and 0xFF shl 8) or (tokenBytes[34] and 0xFF shl 16) or (tokenBytes[35] and 0xFF shl 24)
            val signature = ByteArray(64)
            System.arraycopy(tokenBytes, 36, signature, 0, 64)

            val data = ByteArray(websiteBytes.size + 36)
            System.arraycopy(websiteBytes, 0, data, 0, websiteBytes.size)
            System.arraycopy(tokenBytes, 0, data, websiteBytes.size, 36)
            val isValid = Crypto.verify(signature, data, publicKey, true)

            return Token(publicKey, timestamp, isValid)

        }
    }

}
