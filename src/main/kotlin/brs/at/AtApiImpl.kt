/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.
*/

package brs.at

import brs.at.AtApi.Companion.REGISTER_PART_SIZE
import brs.entity.DependencyProvider
import brs.objects.FluxValues
import brs.util.byteArray.*
import brs.util.crypto.Crypto
import java.math.BigInteger

abstract class AtApiImpl(private val dp: DependencyProvider) : AtApi {
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

    override fun setA1(value: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(value, state.a1)
    }

    override fun setA2(value: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(value, state.a2)
    }

    override fun setA3(value: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(value, state.a3)
    }

    override fun setA4(value: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(value, state.a4)
    }

    override fun setA1A2(val1: Long, val2: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(val1, state.a1)
        AtApiHelper.getByteArray(val2, state.a2)
    }

    override fun setA3A4(val1: Long, val2: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(val1, state.a3)
        AtApiHelper.getByteArray(val2, state.a4)
    }

    override fun setB1(value: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(value, state.b1)
    }

    override fun setB2(value: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(value, state.b2)
    }

    override fun setB3(value: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(value, state.b3)
    }

    override fun setB4(value: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(value, state.b4)
    }

    override fun setB1B2(val1: Long, val2: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(val1, state.b1)
        AtApiHelper.getByteArray(val2, state.b2)
    }

    override fun setB3B4(val3: Long, val4: Long, state: AtMachineState) {
        AtApiHelper.getByteArray(val3, state.b3)
        AtApiHelper.getByteArray(val4, state.b4)
    }

    override fun clearA(state: AtMachineState) {
        state.a1.zero()
        state.a2.zero()
        state.a3.zero()
        state.a4.zero()
    }

    override fun clearB(state: AtMachineState) {
        state.b1.zero()
        state.b2.zero()
        state.b3.zero()
        state.b4.zero()
    }

    override fun copyAFromB(state: AtMachineState) {
        state.b1.copyInto(state.a1)
        state.b2.copyInto(state.a2)
        state.b3.copyInto(state.a3)
        state.b4.copyInto(state.a4)
    }

    override fun copyBFromA(state: AtMachineState) {
        state.a1.copyInto(state.b1)
        state.a2.copyInto(state.b2)
        state.a3.copyInto(state.b3)
        state.a4.copyInto(state.b4)
    }

    override fun checkAIsZero(state: AtMachineState): Long {
        return (if (state.a1.isZero()
            && state.a2.isZero()
            && state.a3.isZero()
            && state.a4.isZero()
        ) 0 else 1).toLong()
    }

    override fun checkBIsZero(state: AtMachineState): Long {
        return (if (state.b1.isZero()
            && state.b2.isZero()
            && state.b3.isZero()
            && state.b4.isZero()
        ) 0 else 1).toLong()
    }

    override fun checkAEqualsB(state: AtMachineState): Long {
        return (if (state.a1.contentEquals(state.b1) &&
            state.a2.contentEquals(state.b2) &&
            state.a3.contentEquals(state.b3) &&
            state.a4.contentEquals(state.b4)
        ) 1 else 0).toLong()
    }

    override fun swapAAndB(state: AtMachineState) {
        val temp = ByteArray(REGISTER_PART_SIZE)

        state.a1.copyInto(temp)
        state.b1.copyInto(state.a1)
        temp.copyInto(state.b1)

        state.a2.copyInto(temp)
        state.b2.copyInto(state.a2)
        temp.copyInto(state.b2)

        state.a3.copyInto(temp)
        state.b3.copyInto(state.a3)
        temp.copyInto(state.b3)

        state.a4.copyInto(temp)
        state.b4.copyInto(state.a4)
        temp.copyInto(state.b4)
    }

    override fun addAToB(state: AtMachineState) {
        val a = AtApiHelper.getA(state)
        val b = AtApiHelper.getB(state)
        val result = a.add(b)
        AtApiHelper.getByteArray(result, state.b1, state.b2, state.b3, state.b4)
    }

    override fun addBToA(state: AtMachineState) {
        val a = AtApiHelper.getA(state)
        val b = AtApiHelper.getB(state)
        val result = a.add(b)
        AtApiHelper.getByteArray(result, state.a1, state.a2, state.a3, state.a4)
    }

    override fun subAFromB(state: AtMachineState) {
        val a = AtApiHelper.getA(state)
        val b = AtApiHelper.getB(state)
        val result = b.subtract(a)
        AtApiHelper.getByteArray(result, state.b1, state.b2, state.b3, state.b4)
    }

    override fun subBFromA(state: AtMachineState) {
        val a = AtApiHelper.getA(state)
        val b = AtApiHelper.getB(state)
        val result = a.subtract(b)
        AtApiHelper.getByteArray(result, state.a1, state.a2, state.a3, state.a4)
    }

    override fun mulAByB(state: AtMachineState) {
        val a = AtApiHelper.getA(state)
        val b = AtApiHelper.getB(state)
        val result = a.multiply(b)
        AtApiHelper.getByteArray(result, state.b1, state.b2, state.b3, state.b4)
    }

    override fun mulBByA(state: AtMachineState) {
        val a = AtApiHelper.getA(state)
        val b = AtApiHelper.getB(state)
        val result = a.multiply(b)
        AtApiHelper.getByteArray(result, state.a1, state.a2, state.a3, state.a4)
    }

    override fun divAByB(state: AtMachineState) {
        val a = AtApiHelper.getA(state)
        val b = AtApiHelper.getB(state)
        if (b.compareTo(BigInteger.ZERO) == 0)
            return
        val result = a.divide(b)
        AtApiHelper.getByteArray(result, state.b1, state.b2, state.b3, state.b4)
    }

    override fun divBByA(state: AtMachineState) {
        val a = AtApiHelper.getA(state)
        val b = AtApiHelper.getB(state)
        if (a.compareTo(BigInteger.ZERO) == 0)
            return
        val result = b.divide(a)
        AtApiHelper.getByteArray(result, state.a1, state.a2, state.a3, state.a4)
    }

    override fun orAWithB(state: AtMachineState) {
        state.a1.orWith(state.b1)
        state.a2.orWith(state.b2)
        state.a3.orWith(state.b3)
        state.a4.orWith(state.b4)
    }

    override fun orBWithA(state: AtMachineState) {
        state.b1.orWith(state.a1)
        state.b2.orWith(state.a2)
        state.b3.orWith(state.a3)
        state.b4.orWith(state.a4)
    }

    override fun andAWithB(state: AtMachineState) {
        state.a1.andWith(state.b1)
        state.a2.andWith(state.b2)
        state.a3.andWith(state.b3)
        state.a4.andWith(state.b4)
    }

    override fun andBWithA(state: AtMachineState) {
        state.b1.andWith(state.a1)
        state.b2.andWith(state.a2)
        state.b3.andWith(state.a3)
        state.b4.andWith(state.a4)
    }

    override fun xorAWithB(state: AtMachineState) {
        state.a1.xorWith(state.b1)
        state.a2.xorWith(state.b2)
        state.a3.xorWith(state.b3)
        state.a4.xorWith(state.b4)
    }

    override fun xorBWithA(state: AtMachineState) {
        state.b1.xorWith(state.a1)
        state.b2.xorWith(state.a2)
        state.b3.xorWith(state.a3)
        state.b4.xorWith(state.a4)
    }

    override fun md5Atob(state: AtMachineState) {
        val md5 = Crypto.md5()
        md5.update(state.a1)
        md5.update(state.a2)
        val hash = md5.digest()

        hash.copyInto(state.b1, 0, 0, 8)
        if (dp.fluxCapacitorService.getValue(FluxValues.NEXT_FORK)) {
            hash.copyInto(state.b2, 0, 8, 16)
        } else {
            hash.copyInto(state.b1, 0, 8, 16)
        }
    }


    override fun checkMd5AWithB(state: AtMachineState): Long {
        return if (dp.fluxCapacitorService.getValue(FluxValues.AT_FIX_BLOCK_3)) {
            val md5 = Crypto.md5()
            md5.update(state.a1)
            md5.update(state.a2)
            val hash = md5.digest()
            if (hash.partEquals(state.b1, 0, 8) && hash.partEquals(state.b2, 8, 8)) 1L else 0L
        } else {
            (if (state.a1.contentEquals(state.b1) && state.a2.contentEquals(state.b2)) 1 else 0).toLong()
        }
    }

    override fun hash160AToB(state: AtMachineState) {
        val ripeMD = Crypto.ripeMD160()
        ripeMD.update(state.a1)
        ripeMD.update(state.a2)
        ripeMD.update(state.a3)
        ripeMD.update(state.a4)
        val hash = ripeMD.digest()

        hash.copyInto(state.b1, 0, 0, 8)
        hash.copyInto(state.b2, 0, 8, 16)
        hash.copyInto(state.b3, 0, 16, 24)
    }

    override fun checkHash160AWithB(state: AtMachineState): Long {
        return if (dp.fluxCapacitorService.getValue(FluxValues.AT_FIX_BLOCK_3)) {
            val ripeMD = Crypto.ripeMD160()
            ripeMD.update(state.a1)
            ripeMD.update(state.a2)
            ripeMD.update(state.a3)
            ripeMD.update(state.a4)
            val hash = ripeMD.digest()

            if (
                hash.partEquals(state.b1, 0, 8)
                && hash.partEquals(state.b2, 8, 8)
                && hash.partEquals(state.b3, 16, 4)
            ) 1L else 0L
        } else {
            if (
                state.a1.contentEquals(state.b1)
                && state.a2.contentEquals(state.b2)
                && state.a3.partEquals(state.b3, 0, 4)
            ) 1L else 0L
        }
    }

    override fun sha256AToB(state: AtMachineState) {
        val sha256 = Crypto.sha256()
        sha256.update(state.a1)
        sha256.update(state.a2)
        sha256.update(state.a3)
        sha256.update(state.a4)
        state.putInB(sha256.digest())
    }

    override fun checkSha256AWithB(state: AtMachineState): Long {
        return if (dp.fluxCapacitorService.getValue(FluxValues.AT_FIX_BLOCK_3)) {
            val sha256 = Crypto.sha256()
            sha256.update(state.a1)
            sha256.update(state.a2)
            sha256.update(state.a3)
            sha256.update(state.a4)
            state.bEquals(sha256.digest()).toLong()
        } else {
            (if (state.a1.contentEquals(state.b1) &&
                state.a2.contentEquals(state.b2) &&
                state.a3.contentEquals(state.b3) &&
                state.a4.contentEquals(state.b4)
            ) 1 else 0).toLong()
        }
    }

    override fun setMinActivationAmount(value: Long, state: AtMachineState) {
        state.setMinActivationAmount(value)
    }

    override fun sha256ToB(val1: Long, val2: Long, state: AtMachineState) {
        if (val1 < 0 || val2 < 0 ||
            val1 + val2 - 1 < 0 ||
            val1 * 8 + 8 > Integer.MAX_VALUE.toLong() ||
            val1 * 8 + 8 > state.dSize ||
            (val1 + val2 - 1) * 8 + 8 > Integer.MAX_VALUE.toLong() ||
            (val1 + val2 - 1) * 8 + 8 > state.dSize
        ) {
            return
        }

        val sha256 = Crypto.sha256()
        sha256.update(state.apData.array(), val1.toInt(), (if (val2 > 256) 256 else val2).toInt())
        state.putInB(sha256.digest())
    }
}
