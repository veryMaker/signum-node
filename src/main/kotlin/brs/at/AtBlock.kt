/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.

*/
package brs.at

data class AtBlock internal constructor(val totalFees: Long, val totalAmountPlanck: Long, val bytesForBlock: ByteArray?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AtBlock

        if (totalFees != other.totalFees) return false
        if (totalAmountPlanck != other.totalAmountPlanck) return false
        if (bytesForBlock != null) {
            if (other.bytesForBlock == null) return false
            if (!bytesForBlock.contentEquals(other.bytesForBlock)) return false
        } else if (other.bytesForBlock != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = totalFees.hashCode()
        result = 31 * result + totalAmountPlanck.hashCode()
        result = 31 * result + (bytesForBlock?.contentHashCode() ?: 0)
        return result
    }
}
