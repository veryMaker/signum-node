/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.
*/

package brs.at

import brs.DependencyProvider
import brs.crypto.Crypto
import brs.fluxcapacitor.FluxValues
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder

open class AtApiImpl(private val dp: DependencyProvider) : AtApi {
    override fun getA1(state: AtMachineState): Long {
        return AtApiHelper.getLong(state.a1)
    }

    override fun getA2(state: AtMachineState): Long {
        return AtApiHelper.getLong(state.a2)
    }

    override fun getA3(state: AtMachineState): Long {
        return AtApiHelper.getLong(state.a3)
    }

    override fun getA4(state: AtMachineState): Long {
        return AtApiHelper.getLong(state.a4)
    }

    override fun getB1(state: AtMachineState): Long {
        return AtApiHelper.getLong(state.b1)
    }

    override fun getB2(state: AtMachineState): Long {
        return AtApiHelper.getLong(state.b2)
    }

    override fun getB3(state: AtMachineState): Long {
        return AtApiHelper.getLong(state.b3)
    }

    override fun getB4(state: AtMachineState): Long {
        return AtApiHelper.getLong(state.b4)
    }

    override fun setA1(`val`: Long, state: AtMachineState) {
        state.a1 = AtApiHelper.getByteArray(`val`)
    }

    override fun setA2(`val`: Long, state: AtMachineState) {
        state.a2 = AtApiHelper.getByteArray(`val`)
    }

    override fun setA3(`val`: Long, state: AtMachineState) {
        state.a3 = AtApiHelper.getByteArray(`val`)
    }

    override fun setA4(`val`: Long, state: AtMachineState) {
        state.a4 = AtApiHelper.getByteArray(`val`)
    }

    override fun setA1A2(val1: Long, val2: Long, state: AtMachineState) {
        state.a1 = AtApiHelper.getByteArray(val1)
        state.a2 = AtApiHelper.getByteArray(val2)
    }

    override fun setA3A4(val1: Long, val2: Long, state: AtMachineState) {
        state.a3 = AtApiHelper.getByteArray(val1)
        state.a4 = AtApiHelper.getByteArray(val2)

    }

    override fun setB1(`val`: Long, state: AtMachineState) {
        state.b1 = AtApiHelper.getByteArray(`val`)
    }

    override fun setB2(`val`: Long, state: AtMachineState) {
        state.b2 = AtApiHelper.getByteArray(`val`)
    }

    override fun setB3(`val`: Long, state: AtMachineState) {
        state.b3 = AtApiHelper.getByteArray(`val`)
    }

    override fun setB4(`val`: Long, state: AtMachineState) {
        state.b4 = AtApiHelper.getByteArray(`val`)
    }

    override fun setB1B2(val1: Long, val2: Long, state: AtMachineState) {
        state.b1 = AtApiHelper.getByteArray(val1)
        state.b2 = AtApiHelper.getByteArray(val2)
    }

    override fun setB3B4(val3: Long, val4: Long, state: AtMachineState) {
        state.b3 = AtApiHelper.getByteArray(val3)
        state.b4 = AtApiHelper.getByteArray(val4)
    }

    override fun clearA(state: AtMachineState) {
        val b = ByteArray(8)
        state.a1 = b
        state.a2 = b
        state.a3 = b
        state.a4 = b
    }

    override fun clearB(state: AtMachineState) {
        val b = ByteArray(8)
        state.b1 = b
        state.b2 = b
        state.b3 = b
        state.b4 = b
    }

    override fun copyAFromB(state: AtMachineState) {
        state.a1 = state.b1
        state.a2 = state.b2
        state.a3 = state.b3
        state.a4 = state.b4
    }

    override fun copyBFromA(state: AtMachineState) {
        state.b1 = state.a1
        state.b2 = state.a2
        state.b3 = state.a3
        state.b4 = state.a4
    }

    private fun isZero(bytes: ByteArray?): Boolean {
        if (bytes == null) return false
        var i = 0
        val bytesLength = bytes.size
        while (i < bytesLength) {
            val b = bytes[i]
            if (b.toInt() != 0) return false
            i++
        }
        return true
    }

    override fun checkAIsZero(state: AtMachineState): Long {
        return (if (isZero(state.a1)
                && isZero(state.a2)
                && isZero(state.a3)
                && isZero(state.a4))
            0
        else
            1).toLong() // TODO why return long?!
    }

    override fun checkBIsZero(state: AtMachineState): Long {
        return (if (isZero(state.b1)
                && isZero(state.b2)
                && isZero(state.b3)
                && isZero(state.b4))
            0
        else
            1).toLong() // TODO why return long?!
    }

    override fun checkAEqualsB(state: AtMachineState): Long {
        return (if (state.a1.contentEquals(state.b1) &&
            state.a2.contentEquals(state.b2) &&
            state.a3.contentEquals(state.b3) &&
            state.a4.contentEquals(state.b4)
        )
            1
        else
            0).toLong()
    }

    override fun swapAAndB(state: AtMachineState) {
        var b: ByteArray = state.a1.clone()

        state.a1 = state.b1
        state.b1 = b

        b = state.a2.clone()
        state.a2 = state.b2
        state.b2 = b

        b = state.a3.clone()
        state.a3 = state.b3
        state.b3 = b

        b = state.a4.clone()
        state.a4 = state.b4
        state.b4 = b
    }

    override fun addAToB(state: AtMachineState) {
        val a = AtApiHelper.getBigInteger(state.a1, state.a2, state.a3, state.a4)
        val b = AtApiHelper.getBigInteger(state.b1, state.b2, state.b3, state.b4)
        val result = a.add(b)
        val resultBuffer = ByteBuffer.wrap(AtApiHelper.getByteArray(result))
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val temp = ByteArray(8)
        resultBuffer.get(temp, 0, 8)
        state.b1 = temp
        resultBuffer.get(temp, 0, 8)
        state.b2 = temp
        resultBuffer.get(temp, 0, 8)
        state.b3 = temp
        resultBuffer.get(temp, 0, 8)
        state.b4 = temp
    }

    override fun addBToA(state: AtMachineState) {
        val a = AtApiHelper.getBigInteger(state.a1, state.a2, state.a3, state.a4)
        val b = AtApiHelper.getBigInteger(state.b1, state.b2, state.b3, state.b4)
        val result = a.add(b)
        val resultBuffer = ByteBuffer.wrap(AtApiHelper.getByteArray(result))
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val temp = ByteArray(8)
        resultBuffer.get(temp, 0, 8)
        state.a1 = temp
        resultBuffer.get(temp, 0, 8)
        state.a2 = temp
        resultBuffer.get(temp, 0, 8)
        state.a3 = temp
        resultBuffer.get(temp, 0, 8)
        state.a4 = temp
    }

    override fun subAFromB(state: AtMachineState) {
        val a = AtApiHelper.getBigInteger(state.a1, state.a2, state.a3, state.a4)
        val b = AtApiHelper.getBigInteger(state.b1, state.b2, state.b3, state.b4)
        val result = b.subtract(a)
        val resultBuffer = ByteBuffer.wrap(AtApiHelper.getByteArray(result))
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val temp = ByteArray(8)
        resultBuffer.get(temp, 0, 8)
        state.b1 = temp
        resultBuffer.get(temp, 0, 8)
        state.b2 = temp
        resultBuffer.get(temp, 0, 8)
        state.b3 = temp
        resultBuffer.get(temp, 0, 8)
        state.b4 = temp
    }

    override fun subBFromA(state: AtMachineState) {
        val a = AtApiHelper.getBigInteger(state.a1, state.a2, state.a3, state.a4)
        val b = AtApiHelper.getBigInteger(state.b1, state.b2, state.b3, state.b4)
        val result = a.subtract(b)
        val resultBuffer = ByteBuffer.wrap(AtApiHelper.getByteArray(result))
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val temp = ByteArray(8)
        resultBuffer.get(temp, 0, 8)
        state.a1 = temp
        resultBuffer.get(temp, 0, 8)
        state.a2 = temp
        resultBuffer.get(temp, 0, 8)
        state.a3 = temp
        resultBuffer.get(temp, 0, 8)
        state.a4 = temp
    }

    override fun mulAByB(state: AtMachineState) {
        val a = AtApiHelper.getBigInteger(state.a1, state.a2, state.a3, state.a4)
        val b = AtApiHelper.getBigInteger(state.b1, state.b2, state.b3, state.b4)
        val result = a.multiply(b)
        val resultBuffer = ByteBuffer.wrap(AtApiHelper.getByteArray(result))
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val temp = ByteArray(8)
        resultBuffer.get(temp, 0, 8)
        state.b1 = temp
        resultBuffer.get(temp, 0, 8)
        state.b2 = temp
        resultBuffer.get(temp, 0, 8)
        state.b3 = temp
        resultBuffer.get(temp, 0, 8)
        state.b4 = temp
    }

    override fun mulBByA(state: AtMachineState) {
        val a = AtApiHelper.getBigInteger(state.a1, state.a2, state.a3, state.a4)
        val b = AtApiHelper.getBigInteger(state.b1, state.b2, state.b3, state.b4)
        val result = a.multiply(b)
        val resultBuffer = ByteBuffer.wrap(AtApiHelper.getByteArray(result))
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val temp = ByteArray(8)
        resultBuffer.get(temp, 0, 8)
        state.a1 = temp
        resultBuffer.get(temp, 0, 8)
        state.a2 = temp
        resultBuffer.get(temp, 0, 8)
        state.a3 = temp
        resultBuffer.get(temp, 0, 8)
        state.a4 = temp
    }

    override fun divAByB(state: AtMachineState) {
        val a = AtApiHelper.getBigInteger(state.a1, state.a2, state.a3, state.a4)
        val b = AtApiHelper.getBigInteger(state.b1, state.b2, state.b3, state.b4)
        if (b.compareTo(BigInteger.ZERO) == 0)
            return
        val result = a.divide(b)
        val resultBuffer = ByteBuffer.wrap(AtApiHelper.getByteArray(result))
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val temp = ByteArray(8)
        resultBuffer.get(temp, 0, 8)
        state.b1 = temp
        resultBuffer.get(temp, 0, 8)
        state.b2 = temp
        resultBuffer.get(temp, 0, 8)
        state.b3 = temp
        resultBuffer.get(temp, 0, 8)
        state.b4 = temp
    }

    override fun divBByA(state: AtMachineState) {
        val a = AtApiHelper.getBigInteger(state.a1, state.a2, state.a3, state.a4)
        val b = AtApiHelper.getBigInteger(state.b1, state.b2, state.b3, state.b4)
        if (a.compareTo(BigInteger.ZERO) == 0)
            return
        val result = b.divide(a)
        val resultBuffer = ByteBuffer.wrap(AtApiHelper.getByteArray(result))
        resultBuffer.order(ByteOrder.LITTLE_ENDIAN)

        val temp = ByteArray(8)
        resultBuffer.get(temp, 0, 8)
        state.a1 = temp
        resultBuffer.get(temp, 0, 8)
        state.a2 = temp
        resultBuffer.get(temp, 0, 8)
        state.a3 = temp
        resultBuffer.get(temp, 0, 8)
        state.a4 = temp
    }

    override fun orAWithB(state: AtMachineState) {
        val a = ByteBuffer.allocate(32)
        a.order(ByteOrder.LITTLE_ENDIAN)
        a.put(state.a1)
        a.put(state.a2)
        a.put(state.a3)
        a.put(state.a4)
        a.clear()

        val b = ByteBuffer.allocate(32)
        b.order(ByteOrder.LITTLE_ENDIAN)
        b.put(state.b1)
        b.put(state.b2)
        b.put(state.b3)
        b.put(state.b4)
        b.clear()

        state.a1 = AtApiHelper.getByteArray(a.getLong(0) or b.getLong(0))
        state.a2 = AtApiHelper.getByteArray(a.getLong(8) or b.getLong(8))
        state.a3 = AtApiHelper.getByteArray(a.getLong(16) or b.getLong(16))
        state.a4 = AtApiHelper.getByteArray(a.getLong(24) or b.getLong(24))
    }

    override fun orBWithA(state: AtMachineState) {
        val a = ByteBuffer.allocate(32)
        a.order(ByteOrder.LITTLE_ENDIAN)
        a.put(state.a1)
        a.put(state.a2)
        a.put(state.a3)
        a.put(state.a4)
        a.clear()

        val b = ByteBuffer.allocate(32)
        b.order(ByteOrder.LITTLE_ENDIAN)
        b.put(state.b1)
        b.put(state.b2)
        b.put(state.b3)
        b.put(state.b4)
        b.clear()

        state.b1 = AtApiHelper.getByteArray(a.getLong(0) or b.getLong(0))
        state.b2 = AtApiHelper.getByteArray(a.getLong(8) or b.getLong(8))
        state.b3 = AtApiHelper.getByteArray(a.getLong(16) or b.getLong(16))
        state.b4 = AtApiHelper.getByteArray(a.getLong(24) or b.getLong(24))
    }

    override fun andAWithB(state: AtMachineState) {
        val a = ByteBuffer.allocate(32)
        a.order(ByteOrder.LITTLE_ENDIAN)
        a.put(state.a1)
        a.put(state.a2)
        a.put(state.a3)
        a.put(state.a4)
        a.clear()

        val b = ByteBuffer.allocate(32)
        b.order(ByteOrder.LITTLE_ENDIAN)
        b.put(state.b1)
        b.put(state.b2)
        b.put(state.b3)
        b.put(state.b4)
        b.clear()

        state.a1 = AtApiHelper.getByteArray(a.getLong(0) and b.getLong(0))
        state.a2 = AtApiHelper.getByteArray(a.getLong(8) and b.getLong(8))
        state.a3 = AtApiHelper.getByteArray(a.getLong(16) and b.getLong(16))
        state.a4 = AtApiHelper.getByteArray(a.getLong(24) and b.getLong(24))
    }

    override fun andBWithA(state: AtMachineState) {
        val a = ByteBuffer.allocate(32)
        a.order(ByteOrder.LITTLE_ENDIAN)
        a.put(state.a1)
        a.put(state.a2)
        a.put(state.a3)
        a.put(state.a4)
        a.clear()

        val b = ByteBuffer.allocate(32)
        b.order(ByteOrder.LITTLE_ENDIAN)
        b.put(state.b1)
        b.put(state.b2)
        b.put(state.b3)
        b.put(state.b4)
        b.clear()

        state.b1 = AtApiHelper.getByteArray(a.getLong(0) and b.getLong(0))
        state.b2 = AtApiHelper.getByteArray(a.getLong(8) and b.getLong(8))
        state.b3 = AtApiHelper.getByteArray(a.getLong(16) and b.getLong(16))
        state.b4 = AtApiHelper.getByteArray(a.getLong(24) and b.getLong(24))
    }

    override fun xorAWithB(state: AtMachineState) {
        val a = ByteBuffer.allocate(32)
        a.order(ByteOrder.LITTLE_ENDIAN)
        a.put(state.a1)
        a.put(state.a2)
        a.put(state.a3)
        a.put(state.a4)
        a.clear()

        val b = ByteBuffer.allocate(32)
        b.order(ByteOrder.LITTLE_ENDIAN)
        b.put(state.b1)
        b.put(state.b2)
        b.put(state.b3)
        b.put(state.b4)
        b.clear()

        state.a1 = AtApiHelper.getByteArray(a.getLong(0) xor b.getLong(0))
        state.a2 = AtApiHelper.getByteArray(a.getLong(8) xor b.getLong(8))
        state.a3 = AtApiHelper.getByteArray(a.getLong(16) xor b.getLong(16))
        state.a4 = AtApiHelper.getByteArray(a.getLong(24) xor b.getLong(24))
    }

    override fun xorBWithA(state: AtMachineState) {
        val a = ByteBuffer.allocate(32)
        a.order(ByteOrder.LITTLE_ENDIAN)
        a.put(state.a1)
        a.put(state.a2)
        a.put(state.a3)
        a.put(state.a4)
        a.clear()

        val b = ByteBuffer.allocate(32)
        b.order(ByteOrder.LITTLE_ENDIAN)
        b.put(state.b1)
        b.put(state.b2)
        b.put(state.b3)
        b.put(state.b4)
        b.clear()

        state.b1 = AtApiHelper.getByteArray(a.getLong(0) xor b.getLong(0))
        state.b2 = AtApiHelper.getByteArray(a.getLong(8) xor b.getLong(8))
        state.b3 = AtApiHelper.getByteArray(a.getLong(16) xor b.getLong(16))
        state.b4 = AtApiHelper.getByteArray(a.getLong(24) xor b.getLong(24))
    }

    override fun md5Atob(state: AtMachineState) {
        val b = ByteBuffer.allocate(16)
        b.order(ByteOrder.LITTLE_ENDIAN)

        b.put(state.a1)
        b.put(state.a2)

        val md5 = Crypto.md5()
        val mdb = ByteBuffer.wrap(md5.digest(b.array()))
        mdb.order(ByteOrder.LITTLE_ENDIAN)

        state.b1 = AtApiHelper.getByteArray(mdb.getLong(0))
        state.b1 = AtApiHelper.getByteArray(mdb.getLong(8))

    }


    override fun checkMd5AWithB(state: AtMachineState): Long {
        if (dp.fluxCapacitor.getValue(FluxValues.AT_FIX_BLOCK_3)) {
            val b = ByteBuffer.allocate(16)
            b.order(ByteOrder.LITTLE_ENDIAN)

            b.put(state.a1)
            b.put(state.a2)

            val md5 = Crypto.md5()
            val mdb = ByteBuffer.wrap(md5.digest(b.array()))
            mdb.order(ByteOrder.LITTLE_ENDIAN)

            return (if (mdb.getLong(0) == AtApiHelper.getLong(state.b1) && mdb.getLong(8) == AtApiHelper.getLong(state.b2))
                1
            else
                0).toLong()
        } else {
            return (if (state.a1.contentEquals(state.b1) && state.a2.contentEquals(state.b2))
                1
            else
                0).toLong()
        }
    }

    override fun hash160AToB(state: AtMachineState) {
        val b = ByteBuffer.allocate(32)
        b.order(ByteOrder.LITTLE_ENDIAN)

        b.put(state.a1)
        b.put(state.a2)
        b.put(state.a3)
        b.put(state.a4)

        val ripemdb = ByteBuffer.wrap(Crypto.ripemd160().digest(b.array()))
        ripemdb.order(ByteOrder.LITTLE_ENDIAN)

        state.b1 = AtApiHelper.getByteArray(ripemdb.getLong(0))
        state.b2 = AtApiHelper.getByteArray(ripemdb.getLong(8))
        state.b3 = AtApiHelper.getByteArray(ripemdb.getInt(16).toLong())
    }

    override fun checkHash160AWithB(state: AtMachineState): Long {
        if (dp.fluxCapacitor.getValue(FluxValues.AT_FIX_BLOCK_3)) {
            val b = ByteBuffer.allocate(32)
            b.order(ByteOrder.LITTLE_ENDIAN)

            b.put(state.a1)
            b.put(state.a2)
            b.put(state.a3)
            b.put(state.a4)

            val ripemdb = ByteBuffer.wrap(Crypto.ripemd160().digest(b.array()))
            ripemdb.order(ByteOrder.LITTLE_ENDIAN)

            return (if (ripemdb.getLong(0) == AtApiHelper.getLong(state.b1) &&
                    ripemdb.getLong(8) == AtApiHelper.getLong(state.b2) &&
                    ripemdb.getInt(16) == (AtApiHelper.getLong(state.b3) and 0x00000000FFFFFFFFL).toInt())
                1
            else
                0).toLong()
        } else {
            return (if (state.a1.contentEquals(state.b1) &&
                state.a2.contentEquals(state.b2) &&
                    AtApiHelper.getLong(state.a3) and 0x00000000FFFFFFFFL == AtApiHelper.getLong(state.b3) and 0x00000000FFFFFFFFL)
                1
            else
                0).toLong()
        }
    }

    override fun sha256AToB(state: AtMachineState) {
        val b = ByteBuffer.allocate(32)
        b.order(ByteOrder.LITTLE_ENDIAN)

        b.put(state.a1)
        b.put(state.a2)
        b.put(state.a3)
        b.put(state.a4)

        val sha256 = Crypto.sha256()
        val shab = ByteBuffer.wrap(sha256.digest(b.array()))
        shab.order(ByteOrder.LITTLE_ENDIAN)

        state.b1 = AtApiHelper.getByteArray(shab.getLong(0))
        state.b2 = AtApiHelper.getByteArray(shab.getLong(8))
        state.b3 = AtApiHelper.getByteArray(shab.getLong(16))
        state.b4 = AtApiHelper.getByteArray(shab.getLong(24))
    }

    override fun checkSha256AWithB(state: AtMachineState): Long {
        if (dp.fluxCapacitor.getValue(FluxValues.AT_FIX_BLOCK_3)) {
            val b = ByteBuffer.allocate(32)
            b.order(ByteOrder.LITTLE_ENDIAN)

            b.put(state.a1)
            b.put(state.a2)
            b.put(state.a3)
            b.put(state.a4)

            val sha256 = Crypto.sha256()
            val shab = ByteBuffer.wrap(sha256.digest(b.array()))
            shab.order(ByteOrder.LITTLE_ENDIAN)

            return (if (shab.getLong(0) == AtApiHelper.getLong(state.b1) &&
                    shab.getLong(8) == AtApiHelper.getLong(state.b2) &&
                    shab.getLong(16) == AtApiHelper.getLong(state.b3) &&
                    shab.getLong(24) == AtApiHelper.getLong(state.b4))
                1
            else
                0).toLong()
        } else {
            return (if (state.a1.contentEquals(state.b1) &&
                state.a2.contentEquals(state.b2) &&
                state.a3.contentEquals(state.b3) &&
                state.a4.contentEquals(state.b4)
            )
                1
            else
                0).toLong()
        }
    }

    // TODO this entire thing needs refactoring...

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun getBlockTimestamp(state: AtMachineState): Long {
        return dp.atApiPlatformImpl.getBlockTimestamp(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun getCreationTimestamp(state: AtMachineState): Long {
        return dp.atApiPlatformImpl.getCreationTimestamp(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun getLastBlockTimestamp(state: AtMachineState): Long {
        return dp.atApiPlatformImpl.getLastBlockTimestamp(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override suspend fun putLastBlockHashInA(state: AtMachineState) {
        dp.atApiPlatformImpl.putLastBlockHashInA(state)

    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override suspend fun aToTxAfterTimestamp(`val`: Long, state: AtMachineState) {
        dp.atApiPlatformImpl.aToTxAfterTimestamp(`val`, state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override suspend fun getTypeForTxInA(state: AtMachineState): Long {
        return dp.atApiPlatformImpl.getTypeForTxInA(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override suspend fun getAmountForTxInA(state: AtMachineState): Long {
        return dp.atApiPlatformImpl.getAmountForTxInA(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override suspend fun getTimestampForTxInA(state: AtMachineState): Long {
        return dp.atApiPlatformImpl.getTimestampForTxInA(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override suspend fun getRandomIdForTxInA(state: AtMachineState): Long {
        return dp.atApiPlatformImpl.getRandomIdForTxInA(state)
    }

    override suspend fun messageFromTxInAToB(state: AtMachineState) {
        dp.atApiPlatformImpl.messageFromTxInAToB(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override suspend fun bToAddressOfTxInA(state: AtMachineState) {
        dp.atApiPlatformImpl.bToAddressOfTxInA(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun bToAddressOfCreator(state: AtMachineState) {
        dp.atApiPlatformImpl.bToAddressOfCreator(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun getCurrentBalance(state: AtMachineState): Long {
        return dp.atApiPlatformImpl.getCurrentBalance(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun getPreviousBalance(state: AtMachineState): Long {
        return dp.atApiPlatformImpl.getPreviousBalance(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun sendToAddressInB(`val`: Long, state: AtMachineState) {
        dp.atApiPlatformImpl.sendToAddressInB(`val`, state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun sendAllToAddressInB(state: AtMachineState) {
        dp.atApiPlatformImpl.sendAllToAddressInB(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun sendOldToAddressInB(state: AtMachineState) {
        dp.atApiPlatformImpl.sendOldToAddressInB(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun sendAToAddressInB(state: AtMachineState) {
        dp.atApiPlatformImpl.sendAToAddressInB(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun addMinutesToTimestamp(val1: Long, val2: Long, state: AtMachineState): Long {
        return dp.atApiPlatformImpl.addMinutesToTimestamp(val1, val2, state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun setMinActivationAmount(`val`: Long, state: AtMachineState) {
        state.setMinActivationAmount(`val`)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override suspend fun putLastBlockGenerationSignatureInA(state: AtMachineState) {
        dp.atApiPlatformImpl.putLastBlockGenerationSignatureInA(state)
    }

    @Deprecated("Use dp.atApiPlatformImpl instead")
    override fun sha256ToB(val1: Long, val2: Long, state: AtMachineState) {
        if (val1 < 0 || val2 < 0 ||
                val1 + val2 - 1 < 0 ||
                val1 * 8 + 8 > Integer.MAX_VALUE.toLong() ||
                val1 * 8 + 8 > state.getdSize() ||
                (val1 + val2 - 1) * 8 + 8 > Integer.MAX_VALUE.toLong() ||
                (val1 + val2 - 1) * 8 + 8 > state.getdSize()) {
            return
        }

        val sha256 = Crypto.sha256()
        sha256.update(state.apData.array(), val1.toInt(), (if (val2 > 256) 256 else val2).toInt())
        val shab = ByteBuffer.wrap(sha256.digest())
        shab.order(ByteOrder.LITTLE_ENDIAN)

        state.b1 = AtApiHelper.getByteArray(shab.getLong(0))
        state.b2 = AtApiHelper.getByteArray(shab.getLong(8))
        state.b3 = AtApiHelper.getByteArray(shab.getLong(16))
        state.b4 = AtApiHelper.getByteArray(shab.getLong(24))
    }
}
