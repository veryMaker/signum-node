/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.
*/

package brs.at

data class AtTransaction internal constructor(
    val senderId: Long,
    val recipientId: Long,
    val amount: Long,
    val message: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AtTransaction

        if (senderId != other.senderId) return false
        if (recipientId != other.recipientId) return false
        if (amount != other.amount) return false
        if (message != null) {
            if (other.message == null) return false
            if (!message.contentEquals(other.message)) return false
        } else if (other.message != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = senderId.hashCode()
        result = 31 * result + recipientId.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (message?.contentHashCode() ?: 0)
        return result
    }
}
