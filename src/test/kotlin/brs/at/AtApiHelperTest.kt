package brs.at

import brs.util.convert.parseHexString
import brs.util.convert.toHexString
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.math.BigInteger

@RunWith(JUnit4::class)
class AtApiHelperTest {
    @Test
    fun testGetLong() {
        assertEquals(0x0000000000000000L, AtApiHelper.getLong(ByteArray(0)))
        assertEquals(0x0000000000000001L, AtApiHelper.getLong("0100000000000000".parseHexString()))
        assertEquals(0x0000000000002301L, AtApiHelper.getLong("0123000000000000".parseHexString()))
        assertEquals(0x0000000000452301L, AtApiHelper.getLong("0123450000000000".parseHexString()))
        assertEquals(0x0000000067452301L, AtApiHelper.getLong("0123456700000000".parseHexString()))
        assertEquals(0x0000008967452301L, AtApiHelper.getLong("0123456789000000".parseHexString()))
        assertEquals(0x0000ab8967452301L, AtApiHelper.getLong("0123456789ab0000".parseHexString()))
        assertEquals(0x00cdab8967452301L, AtApiHelper.getLong("0123456789abcd00".parseHexString()))
        assertEquals(-0x1032547698badcffL, AtApiHelper.getLong("0123456789abcdef".parseHexString()))
    }

    @Test
    fun testGetLong_overflow() {
        assertEquals(-0x1032547698badcffL, AtApiHelper.getLong("0123456789abcdef0123456789abcdef".parseHexString()))
        assertEquals(-0x1032547698badcffL, AtApiHelper.getLong("0123456789abcdef2222222222222222".parseHexString()))
    }

    @Test
    fun testGetByteArray_long() {
        assertEquals("0100000000000000", AtApiHelper.getByteArray(0x0000000000000001L).toHexString())
        assertEquals("0123000000000000", AtApiHelper.getByteArray(0x0000000000002301L).toHexString())
        assertEquals("0123450000000000", AtApiHelper.getByteArray(0x0000000000452301L).toHexString())
        assertEquals("0123456700000000", AtApiHelper.getByteArray(0x0000000067452301L).toHexString())
        assertEquals("0123456789000000", AtApiHelper.getByteArray(0x0000008967452301L).toHexString())
        assertEquals("0123456789ab0000", AtApiHelper.getByteArray(0x0000ab8967452301L).toHexString())
        assertEquals("0123456789abcd00", AtApiHelper.getByteArray(0x00cdab8967452301L).toHexString())
        assertEquals("0123456789abcdef", AtApiHelper.getByteArray(-0x1032547698badcffL).toHexString())
    }

    @Test
    fun testGetByteArray_long_direct() {
        val buffer = ByteArray(8)
        AtApiHelper.getByteArray(0x0000000000000001L, buffer)
        assertEquals("0100000000000000", buffer.toHexString())
        AtApiHelper.getByteArray(0x0000000000002301L, buffer)
        assertEquals("0123000000000000", buffer.toHexString())
        AtApiHelper.getByteArray(0x0000000000452301L, buffer)
        assertEquals("0123450000000000", buffer.toHexString())
        AtApiHelper.getByteArray(0x0000000067452301L, buffer)
        assertEquals("0123456700000000", buffer.toHexString())
        AtApiHelper.getByteArray(0x0000008967452301L, buffer)
        assertEquals("0123456789000000", buffer.toHexString())
        AtApiHelper.getByteArray(0x0000ab8967452301L, buffer)
        assertEquals("0123456789ab0000", buffer.toHexString())
        AtApiHelper.getByteArray(0x00cdab8967452301L, buffer)
        assertEquals("0123456789abcd00", buffer.toHexString())
        AtApiHelper.getByteArray(-0x1032547698badcffL, buffer)
        assertEquals("0123456789abcdef", buffer.toHexString())
    }

    @Test
    fun testGetByteArray_bigInteger() {
        val first = ByteArray(8)
        val second = ByteArray(8)
        val third = ByteArray(8)
        val fourth = ByteArray(8)

        fun arraySum(): String {
            return first.toHexString() + second.toHexString() + third.toHexString() + fourth.toHexString()
        }

        fun fillArray(array: ByteArray) {
            for (i in array.indices) {
                array[i] = 0xAA.toByte()
            }
        }

        // Protect against padding failures
        fillArray(first)
        fillArray(second)
        fillArray(third)
        fillArray(fourth)

        AtApiHelper.getByteArray(BigInteger.valueOf(0x0000000000000001L), first, second, third, fourth)
        assertEquals("0100000000000000000000000000000000000000000000000000000000000000", arraySum())
        AtApiHelper.getByteArray(BigInteger.valueOf(0x0000000000002301L), first, second, third, fourth)
        assertEquals("0123000000000000000000000000000000000000000000000000000000000000", arraySum())
        AtApiHelper.getByteArray(BigInteger.valueOf(0x0000000000452301L), first, second, third, fourth)
        assertEquals("0123450000000000000000000000000000000000000000000000000000000000", arraySum())
        AtApiHelper.getByteArray(BigInteger.valueOf(0x0000000067452301L), first, second, third, fourth)
        assertEquals("0123456700000000000000000000000000000000000000000000000000000000", arraySum())
        AtApiHelper.getByteArray(BigInteger.valueOf(0x0000008967452301L), first, second, third, fourth)
        assertEquals("0123456789000000000000000000000000000000000000000000000000000000", arraySum())
        AtApiHelper.getByteArray(BigInteger.valueOf(0x0000ab8967452301L), first, second, third, fourth)
        assertEquals("0123456789ab0000000000000000000000000000000000000000000000000000", arraySum())
        AtApiHelper.getByteArray(BigInteger.valueOf(0x00cdab8967452301L), first, second, third, fourth)
        assertEquals("0123456789abcd00000000000000000000000000000000000000000000000000", arraySum())
        AtApiHelper.getByteArray(BigInteger.valueOf(-0x1032547698badcffL), first, second, third, fourth)
        assertEquals("0123456789abcdefffffffffffffffffffffffffffffffffffffffffffffffff", arraySum())
        AtApiHelper.getByteArray(BigInteger("efcdab8967452301efcdab8967452301", 16), first, second, third, fourth)
        assertEquals("0123456789abcdef0123456789abcdef00000000000000000000000000000000", arraySum())
        AtApiHelper.getByteArray(BigInteger("efcdab8967452301efcdab8967452301efcdab8967452301efcdab8967452301", 16), first, second, third, fourth)
        assertEquals("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef", arraySum())
    }

    @Test
    fun testLongToHeight() {
        assertEquals(0, AtApiHelper.longToHeight(0x0000000000000001L).toLong())
        assertEquals(0, AtApiHelper.longToHeight(0x0000000000002301L).toLong())
        assertEquals(0, AtApiHelper.longToHeight(0x0000000000452301L).toLong())
        assertEquals(0, AtApiHelper.longToHeight(0x0000000067452301L).toLong())
        assertEquals(0x89, AtApiHelper.longToHeight(0x0000008967452301L).toLong())
        assertEquals(0xab89, AtApiHelper.longToHeight(0x0000ab8967452301L).toLong())
        assertEquals(0xcdab89, AtApiHelper.longToHeight(0x00cdab8967452301L).toLong())
        assertEquals(-0x10325477, AtApiHelper.longToHeight(-0x1032547698badcffL).toLong())
    }

    @Test
    fun testLongToNumberOfTx() {
        assertEquals(0x01, AtApiHelper.longToNumOfTx(0x0000000000000001L).toLong())
        assertEquals(0x2301, AtApiHelper.longToNumOfTx(0x0000000000002301L).toLong())
        assertEquals(0x452301, AtApiHelper.longToNumOfTx(0x0000000000452301L).toLong())
        assertEquals(0x67452301, AtApiHelper.longToNumOfTx(0x0000000067452301L).toLong())
        assertEquals(0x67452301, AtApiHelper.longToNumOfTx(0x0000008967452301L).toLong())
        assertEquals(0x67452301, AtApiHelper.longToNumOfTx(0x0000ab8967452301L).toLong())
        assertEquals(0x67452301, AtApiHelper.longToNumOfTx(0x00cdab8967452301L).toLong())
        assertEquals(0x67452301, AtApiHelper.longToNumOfTx(-0x1032547698badcffL).toLong())
    }

    @Test
    fun testGetLongTimestamp() {
        assertEquals(0x0000000100000000L, AtApiHelper.getLongTimestamp(0x01, 0x00))
        assertEquals(0x0000230100000000L, AtApiHelper.getLongTimestamp(0x2301, 0x00))
        assertEquals(0x0045230100000000L, AtApiHelper.getLongTimestamp(0x452301, 0x00))
        assertEquals(0x6745230100000000L, AtApiHelper.getLongTimestamp(0x67452301, 0x00))
        assertEquals(0x0000000000000001L, AtApiHelper.getLongTimestamp(0x00, 0x01))
        assertEquals(0x0000000000002301L, AtApiHelper.getLongTimestamp(0x00, 0x2301))
        assertEquals(0x0000000000452301L, AtApiHelper.getLongTimestamp(0x00, 0x452301))
        assertEquals(0x0000000067452301L, AtApiHelper.getLongTimestamp(0x00, 0x67452301))
        assertEquals(0x0000000100000001L, AtApiHelper.getLongTimestamp(0x01, 0x01))
        assertEquals(0x0000230100002301L, AtApiHelper.getLongTimestamp(0x2301, 0x2301))
        assertEquals(0x0045230100452301L, AtApiHelper.getLongTimestamp(0x452301, 0x452301))
        assertEquals(0x6745230167452301L, AtApiHelper.getLongTimestamp(0x67452301, 0x67452301))
    }

    @Test
    fun testGetBigInteger() {
        assertEquals(BigInteger("0000000000000000000000000000000000000000000000000000000000000012".parseHexString()), AtApiHelper.getBigInteger("1200000000000000".parseHexString(), "0000000000000000".parseHexString(), "0000000000000000".parseHexString(), "0000000000000000".parseHexString()))
        assertEquals(BigInteger("0000000000000000000000000000000000000000000000120000000000000012".parseHexString()), AtApiHelper.getBigInteger("1200000000000000".parseHexString(), "1200000000000000".parseHexString(), "0000000000000000".parseHexString(), "0000000000000000".parseHexString()))
        assertEquals(BigInteger("0000000000000000000000000000001200000000000000120000000000000012".parseHexString()), AtApiHelper.getBigInteger("1200000000000000".parseHexString(), "1200000000000000".parseHexString(), "1200000000000000".parseHexString(), "0000000000000000".parseHexString()))
        assertEquals(BigInteger("0000000000000012000000000000001200000000000000120000000000000012".parseHexString()), AtApiHelper.getBigInteger("1200000000000000".parseHexString(), "1200000000000000".parseHexString(), "1200000000000000".parseHexString(), "1200000000000000".parseHexString()))

        assertEquals(BigInteger("0000000000000012000000000000001200000000000000120000000000003412".parseHexString()), AtApiHelper.getBigInteger("1234000000000000".parseHexString(), "1200000000000000".parseHexString(), "1200000000000000".parseHexString(), "1200000000000000".parseHexString()))
        assertEquals(BigInteger("0000000000000012000000000000001200000000000034120000000000003412".parseHexString()), AtApiHelper.getBigInteger("1234000000000000".parseHexString(), "1234000000000000".parseHexString(), "1200000000000000".parseHexString(), "1200000000000000".parseHexString()))
        assertEquals(BigInteger("0000000000000012000000000000341200000000000034120000000000003412".parseHexString()), AtApiHelper.getBigInteger("1234000000000000".parseHexString(), "1234000000000000".parseHexString(), "1234000000000000".parseHexString(), "1200000000000000".parseHexString()))
        assertEquals(BigInteger("0000000000003412000000000000341200000000000034120000000000003412".parseHexString()), AtApiHelper.getBigInteger("1234000000000000".parseHexString(), "1234000000000000".parseHexString(), "1234000000000000".parseHexString(), "1234000000000000".parseHexString()))

        assertEquals(BigInteger("0000000000003412000000000000341200000000000034120000000000563412".parseHexString()), AtApiHelper.getBigInteger("1234560000000000".parseHexString(), "1234000000000000".parseHexString(), "1234000000000000".parseHexString(), "1234000000000000".parseHexString()))
        assertEquals(BigInteger("0000000000003412000000000000341200000000005634120000000000563412".parseHexString()), AtApiHelper.getBigInteger("1234560000000000".parseHexString(), "1234560000000000".parseHexString(), "1234000000000000".parseHexString(), "1234000000000000".parseHexString()))
        assertEquals(BigInteger("0000000000003412000000000056341200000000005634120000000000563412".parseHexString()), AtApiHelper.getBigInteger("1234560000000000".parseHexString(), "1234560000000000".parseHexString(), "1234560000000000".parseHexString(), "1234000000000000".parseHexString()))
        assertEquals(BigInteger("0000000000563412000000000056341200000000005634120000000000563412".parseHexString()), AtApiHelper.getBigInteger("1234560000000000".parseHexString(), "1234560000000000".parseHexString(), "1234560000000000".parseHexString(), "1234560000000000".parseHexString()))

        assertEquals(BigInteger("0000000000563412000000000056341200000000005634120000000078563412".parseHexString()), AtApiHelper.getBigInteger("1234567800000000".parseHexString(), "1234560000000000".parseHexString(), "1234560000000000".parseHexString(), "1234560000000000".parseHexString()))
        assertEquals(BigInteger("0000000000563412000000000056341200000000785634120000000078563412".parseHexString()), AtApiHelper.getBigInteger("1234567800000000".parseHexString(), "1234567800000000".parseHexString(), "1234560000000000".parseHexString(), "1234560000000000".parseHexString()))
        assertEquals(BigInteger("0000000000563412000000007856341200000000785634120000000078563412".parseHexString()), AtApiHelper.getBigInteger("1234567800000000".parseHexString(), "1234567800000000".parseHexString(), "1234567800000000".parseHexString(), "1234560000000000".parseHexString()))
        assertEquals(BigInteger("0000000078563412000000007856341200000000785634120000000078563412".parseHexString()), AtApiHelper.getBigInteger("1234567800000000".parseHexString(), "1234567800000000".parseHexString(), "1234567800000000".parseHexString(), "1234567800000000".parseHexString()))

        assertEquals(BigInteger("0000000078563412000000007856341200000000785634120000009078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890000000".parseHexString(), "1234567800000000".parseHexString(), "1234567800000000".parseHexString(), "1234567800000000".parseHexString()))
        assertEquals(BigInteger("0000000078563412000000007856341200000090785634120000009078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890000000".parseHexString(), "1234567890000000".parseHexString(), "1234567800000000".parseHexString(), "1234567800000000".parseHexString()))
        assertEquals(BigInteger("0000000078563412000000907856341200000090785634120000009078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890000000".parseHexString(), "1234567890000000".parseHexString(), "1234567890000000".parseHexString(), "1234567800000000".parseHexString()))
        assertEquals(BigInteger("0000009078563412000000907856341200000090785634120000009078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890000000".parseHexString(), "1234567890000000".parseHexString(), "1234567890000000".parseHexString(), "1234567890000000".parseHexString()))

        assertEquals(BigInteger("0000009078563412000000907856341200000090785634120000ab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890ab0000".parseHexString(), "1234567890000000".parseHexString(), "1234567890000000".parseHexString(), "1234567890000000".parseHexString()))
        assertEquals(BigInteger("000000907856341200000090785634120000ab90785634120000ab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890ab0000".parseHexString(), "1234567890ab0000".parseHexString(), "1234567890000000".parseHexString(), "1234567890000000".parseHexString()))
        assertEquals(BigInteger("00000090785634120000ab90785634120000ab90785634120000ab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890ab0000".parseHexString(), "1234567890ab0000".parseHexString(), "1234567890ab0000".parseHexString(), "1234567890000000".parseHexString()))
        assertEquals(BigInteger("0000ab90785634120000ab90785634120000ab90785634120000ab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890ab0000".parseHexString(), "1234567890ab0000".parseHexString(), "1234567890ab0000".parseHexString(), "1234567890ab0000".parseHexString()))

        assertEquals(BigInteger("0000ab90785634120000ab90785634120000ab907856341200cdab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890abcd00".parseHexString(), "1234567890ab0000".parseHexString(), "1234567890ab0000".parseHexString(), "1234567890ab0000".parseHexString()))
        assertEquals(BigInteger("0000ab90785634120000ab907856341200cdab907856341200cdab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890abcd00".parseHexString(), "1234567890abcd00".parseHexString(), "1234567890ab0000".parseHexString(), "1234567890ab0000".parseHexString()))
        assertEquals(BigInteger("0000ab907856341200cdab907856341200cdab907856341200cdab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890abcd00".parseHexString(), "1234567890abcd00".parseHexString(), "1234567890abcd00".parseHexString(), "1234567890ab0000".parseHexString()))
        assertEquals(BigInteger("00cdab907856341200cdab907856341200cdab907856341200cdab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890abcd00".parseHexString(), "1234567890abcd00".parseHexString(), "1234567890abcd00".parseHexString(), "1234567890abcd00".parseHexString()))

        assertEquals(BigInteger("00cdab907856341200cdab907856341200cdab9078563412efcdab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890abcdef".parseHexString(), "1234567890abcd00".parseHexString(), "1234567890abcd00".parseHexString(), "1234567890abcd00".parseHexString()))
        assertEquals(BigInteger("00cdab907856341200cdab9078563412efcdab9078563412efcdab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890abcdef".parseHexString(), "1234567890abcdef".parseHexString(), "1234567890abcd00".parseHexString(), "1234567890abcd00".parseHexString()))
        assertEquals(BigInteger("00cdab9078563412efcdab9078563412efcdab9078563412efcdab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890abcdef".parseHexString(), "1234567890abcdef".parseHexString(), "1234567890abcdef".parseHexString(), "1234567890abcd00".parseHexString()))
        assertEquals(BigInteger("efcdab9078563412efcdab9078563412efcdab9078563412efcdab9078563412".parseHexString()), AtApiHelper.getBigInteger("1234567890abcdef".parseHexString(), "1234567890abcdef".parseHexString(), "1234567890abcdef".parseHexString(), "1234567890abcdef".parseHexString()))
    }
}
