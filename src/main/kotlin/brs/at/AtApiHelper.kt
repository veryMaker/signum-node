/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.

*/

package brs.at

import brs.at.AtApi.Companion.REGISTER_PART_SIZE
import brs.util.byteArray.fillFromEnd
import brs.util.byteArray.partEquals
import burst.kit.crypto.BurstCrypto
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.experimental.and

object AtApiHelper {

    private val burstCrypto = BurstCrypto.getInstance()

    fun longToHeight(x: Long): Int {
        return (x shr 32).toInt()
    }

    /**
     * Little Endian.
     */
    fun getLong(bytes: ByteArray): Long {
        return burstCrypto.bytesToLongLE(bytes)
    }

    /**
     * Little Endian.
     */
    fun getByteArray(long: Long, dest: ByteArray, offset: Int = 0) {
        return burstCrypto.longToBytesLE(long, dest, offset)
    }

    /**
     * Little Endian.
     */
    fun getByteArray(long: Long): ByteArray {
        return burstCrypto.longToBytesLE(long)
    }

    /**
     * Little Endian.
     */
    fun hashLong(messageDigest: MessageDigest, long: Long) {
        var l = long
        for (i in 7 downTo 0) {
            messageDigest.update((l and 0xFF).toByte())
            l = l shr 8
        }
    }

    fun longToNumOfTx(x: Long): Int {
        return x.toInt()
    }

    internal fun getLongTimestamp(height: Int, numOfTx: Int): Long {
        return height.toLong() shl 32 or numOfTx.toLong()
    }

    fun getA(state: AtMachineState): BigInteger {
        return getBigInteger(state.a1, state.a2, state.a3, state.a4) // TODO optimize
    }

    fun getB(state: AtMachineState): BigInteger {
        return getBigInteger(state.b1, state.b2, state.b3, state.b4) // TODO optimize
    }

    fun getBigInteger(b1: ByteArray, b2: ByteArray, b3: ByteArray, b4: ByteArray): BigInteger {
        return BigInteger(
            byteArrayOf(
                b4[7],
                b4[6],
                b4[5],
                b4[4],
                b4[3],
                b4[2],
                b4[1],
                b4[0],
                b3[7],
                b3[6],
                b3[5],
                b3[4],
                b3[3],
                b3[2],
                b3[1],
                b3[0],
                b2[7],
                b2[6],
                b2[5],
                b2[4],
                b2[3],
                b2[2],
                b2[1],
                b2[0],
                b1[7],
                b1[6],
                b1[5],
                b1[4],
                b1[3],
                b1[2],
                b1[1],
                b1[0]
            )
        )
    }

    /**
     * Little endian.
     * @param bigInt The integer to load into the arrays
     * @param first The first register eg. A1
     * @param second The second register eg. A2
     * @param second The third register eg. A3
     * @param second The fourth register eg. A4
     */
    fun getByteArray(
        bigInt: BigInteger,
        first: ByteArray,
        second: ByteArray,
        third: ByteArray,
        fourth: ByteArray
    ) {
        // Calculate the data from the BigInteger
        val array = bigInt.toByteArray()
        array.reverse()

        // Optimized algorithm for copying the data into the registers provided.
        if (array.size >= 32) {
            array.copyInto(first, 0, 0, 8)
            array.copyInto(second, 0, 8, 16)
            array.copyInto(third, 0, 16, 24)
            array.copyInto(fourth, 0, 24, 32)
            return
        }
        // The padding byte
        val padding = ((array[array.size - 1] and 0x80.toByte()).toInt() shr 7).toByte()
        // The number of non-padding bytes in the partially filled register (the register that is neither full of data nor empty of data)
        val partialRegisterLength = array.size % 8
        // The number of padding bytes in the partially filled register
        val partialRegisterPads = 8 - partialRegisterLength
        // Determine the range that the length lies in
        when (array.size / 8) {
            0 -> { // Length 0-7
                array.copyInto(first, 0, 0, partialRegisterLength)
                first.fillFromEnd(padding, partialRegisterPads)
                second.fill(padding)
                third.fill(padding)
                fourth.fill(padding)
            }
            1 -> { // Length 8-15
                array.copyInto(first, 0, 0, 8)
                array.copyInto(second, 0, 8, partialRegisterLength + 8)
                second.fillFromEnd(padding, partialRegisterPads)
                third.fill(padding)
                fourth.fill(padding)
            }
            2 -> { // Length 16-23
                array.copyInto(first, 0, 0, 8)
                array.copyInto(second, 0, 8, 16)
                array.copyInto(third, 0, 16, partialRegisterLength + 16)
                third.fillFromEnd(padding, partialRegisterPads)
                fourth.fill(padding)
            }
            3 -> { // Length 24-31
                array.copyInto(first, 0, 0, 8)
                array.copyInto(second, 0, 8, 16)
                array.copyInto(third, 0, 16, 24)
                array.copyInto(fourth, 0, 24, partialRegisterLength + 24)
                fourth.fillFromEnd(padding, partialRegisterPads)
            }
        }
    }
}

fun AtMachineState.putInA(data: ByteArray) {
    require(data.size == 32)
    data.copyInto(this.a1, 0, 0, 8)
    data.copyInto(this.a2, 0, 8, 16)
    data.copyInto(this.a3, 0, 16, 24)
    data.copyInto(this.a4, 0, 24, 32)
}

fun AtMachineState.putInB(data: ByteArray) {
    require(data.size == 32)
    data.copyInto(this.b1, 0, 0, 8)
    data.copyInto(this.b2, 0, 8, 16)
    data.copyInto(this.b3, 0, 16, 24)
    data.copyInto(this.b4, 0, 24, 32)
}

fun AtMachineState.bEquals(data: ByteArray): Boolean {
    require(data.size == 32)
    return data.partEquals(this.b1, 0, REGISTER_PART_SIZE)
            && data.partEquals(this.b2, 8, REGISTER_PART_SIZE)
            && data.partEquals(this.b3, 16, REGISTER_PART_SIZE)
            && data.partEquals(this.b4, 24, REGISTER_PART_SIZE)
}
