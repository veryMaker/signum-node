/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.
*/

package brs.at

class AtTransaction internal constructor(
    val senderId: ByteArray,
    val recipientId: ByteArray,
    val amount: Long,
    val message: ByteArray?
) {
    fun getAmount(): Long? {
        return amount
    }
}
