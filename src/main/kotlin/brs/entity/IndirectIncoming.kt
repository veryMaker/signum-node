package brs.entity

class IndirectIncoming(val accountId: Long, val transactionId: Long, val height: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as IndirectIncoming?

        if (accountId != that!!.accountId) return false
        return if (transactionId != that.transactionId) false else height == that.height
    }

    override fun hashCode(): Int {
        var result = (accountId xor accountId.ushr(32)).toInt()
        result = 31 * result + (transactionId xor transactionId.ushr(32)).toInt()
        result = 31 * result + height
        return result
    }
}
