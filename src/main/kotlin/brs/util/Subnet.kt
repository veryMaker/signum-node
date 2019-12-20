package brs.util

import java.math.BigInteger
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Blatantly stolen from https://stackoverflow.com/a/25165891
 * @author c3oe.de, based on snippets from Scott Plante, John Kugelmann
 */
class Subnet {
    private val bytesSubnetCount: Int
    private val bigMask: BigInteger
    private val bigSubnetMasked: BigInteger

    /**
     * For use via format "192.168.0.0/24" or "2001:db8:85a3:880:0:0:0:0/57"
     * @param subnetAddress java.net.InetAddress (IP address)
     * @param bits          subnet mask in /bits notation
     */
    private constructor(subnetAddress: InetAddress, bits: Int) {
        this.bytesSubnetCount = subnetAddress.address.size // 4 or 16
        this.bigMask = BigInteger.valueOf(-1).shiftLeft(this.bytesSubnetCount * 8 - bits) // mask = -1 << 32 - bits
        this.bigSubnetMasked = BigInteger(subnetAddress.address).and(this.bigMask)
    }

    /**
     * For use via format "192.168.0.0/255.255.255.0" or single address
     * @param subnetAddress java.net.InetAddress (IP address)
     * @param mask          java.net.InetAddress (IP address, here used as network mask)
     */
    private constructor(subnetAddress: InetAddress, mask: InetAddress?) {
        this.bytesSubnetCount = subnetAddress.address.size
        this.bigMask =
            if (null == mask) BigInteger.valueOf(-1) else BigInteger(mask.address) // no mask given case is handled here.
        this.bigSubnetMasked = BigInteger(subnetAddress.address).and(this.bigMask)
    }

    fun isInNet(address: InetAddress): Boolean {
        val bytesAddress = address.address
        if (this.bytesSubnetCount != bytesAddress.size)
            return false
        val bigAddress = BigInteger(bytesAddress)
        return bigAddress.and(this.bigMask) == this.bigSubnetMasked
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is Subnet)
            return false
        val other = obj as Subnet?
        return this.bigSubnetMasked == other!!.bigSubnetMasked &&
                this.bigMask == other.bigMask &&
                this.bytesSubnetCount == other.bytesSubnetCount
    }

    override fun hashCode(): Int {
        return this.bytesSubnetCount
    }

    override fun toString(): String {
        val buf = StringBuilder()
        bigInteger2IpString(buf, this.bigSubnetMasked, this.bytesSubnetCount)
        buf.append('/')
        bigInteger2IpString(buf, this.bigMask, this.bytesSubnetCount)
        return buf.toString()
    }

    companion object {

        /**
         * Subnet factory method.
         *
         * @param subnetMask format: "192.168.0.0/24" or "192.168.0.0/255.255.255.0"
         * or single address or "2001:db8:85a3:880:0:0:0:0/57"
         * @return a new instance
         * @throws UnknownHostException thrown if unsupported subnet mask.
         */
        fun createInstance(subnetMask: String): Subnet {
            val stringArr = subnetMask.split('/').dropLastWhile { it.isEmpty() }.toTypedArray()
            return if (2 > stringArr.size)
                Subnet(InetAddress.getByName(stringArr[0]), null)
            else if (stringArr[1].contains(".") || stringArr[1].contains(":"))
                Subnet(InetAddress.getByName(stringArr[0]), InetAddress.getByName(stringArr[1]))
            else
                Subnet(InetAddress.getByName(stringArr[0]), Integer.parseInt(stringArr[1]))
        }

        private fun bigInteger2IpString(buf: StringBuilder, bigInteger: BigInteger, displayBytes: Int) {
            val isIPv4 = 4 == displayBytes
            val bytes = bigInteger.toByteArray()
            val diffLen = displayBytes - bytes.size
            val fillByte = if (0 > bytes[0].toInt()) 0xFF.toByte() else 0x00.toByte()

            var integer: Int
            for (i in 0 until displayBytes) {
                if (0 < i && !isIPv4 && i % 2 == 0)
                    buf.append(':')
                else if (0 < i && isIPv4)
                    buf.append('.')
                integer = 0xFF and if (i < diffLen) fillByte.toInt() else bytes[i - diffLen].toInt()
                if (!isIPv4 && 0x10 > integer)
                    buf.append('0')
                buf.append(if (isIPv4) integer else Integer.toHexString(integer))
            }
        }
    }
}
