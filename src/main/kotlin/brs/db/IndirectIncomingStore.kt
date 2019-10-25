package brs.db

interface IndirectIncomingStore {
    fun addIndirectIncomings(indirectIncomings: Collection<IndirectIncoming>)
    fun getIndirectIncomings(accountId: Long, from: Int, to: Int): List<Long>

    class IndirectIncoming(val accountId: Long, val transactionId: Long, val height: Int) {

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false

            val that = o as IndirectIncoming?

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
}
