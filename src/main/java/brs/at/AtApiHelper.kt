/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.

*/

package brs.at

import burst.kit.crypto.BurstCrypto
import org.bouncycastle.util.Arrays

import java.math.BigInteger
import java.nio.BufferOverflowException
import kotlin.experimental.and

object AtApiHelper {

    private val burstCrypto = BurstCrypto.getInstance()

    fun longToHeight(x: Long): Int {
        return (x shr 32).toInt()
    }

    fun getLong(bytes: ByteArray): Long {
        if (bytes.size > 8) {
            throw BufferOverflowException()
        }
        return burstCrypto.bytesToLong(bytes)
    }

    fun getByteArray(l: Long): ByteArray {
        return Arrays.reverse(burstCrypto.longToBytes(l))
    }

    fun longToNumOfTx(x: Long): Int {
        return x.toInt()
    }

    internal fun getLongTimestamp(height: Int, numOfTx: Int): Long {
        return height.toLong() shl 32 or numOfTx.toLong()
    }

    fun getBigInteger(b1: ByteArray, b2: ByteArray, b3: ByteArray, b4: ByteArray): BigInteger {
        return BigInteger(byteArrayOf(b4[7], b4[6], b4[5], b4[4], b4[3], b4[2], b4[1], b4[0], b3[7], b3[6], b3[5], b3[4], b3[3], b3[2], b3[1], b3[0], b2[7], b2[6], b2[5], b2[4], b2[3], b2[2], b2[1], b2[0], b1[7], b1[6], b1[5], b1[4], b1[3], b1[2], b1[1], b1[0]))
    }

    fun getByteArray(bigInt: BigInteger): ByteArray {
        val resultSize = 32
        val bigIntBytes = Arrays.reverse(bigInt.toByteArray())
        val result = ByteArray(resultSize)
        if (bigIntBytes.size < resultSize) {
            val padding = ((bigIntBytes[bigIntBytes.size - 1] and 0x80.toByte()).toInt() shr 7).toByte()
            var i = 0
            val length = resultSize - bigIntBytes.size
            while (i < length) {
                result[resultSize - 1 - i] = padding
                i++
            }
        }
        System.arraycopy(bigIntBytes, 0, result, 0, if (resultSize >= bigIntBytes.size) bigIntBytes.size else resultSize)
        return result
    }
}
