/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.
*/

package brs.at

import java.util.SortedMap
import java.util.TreeMap

class AtTransaction internal constructor(val senderId: ByteArray, val recipientId: ByteArray, val amount: Long, val message: ByteArray?) {

    fun getAmount(): Long? {
        return amount
    }

    fun addTransaction(atId: Long, height: Long) {
        if (all_AT_Txs.containsKey(atId)) {
            all_AT_Txs[atId]!![height] = this
        } else {
            val temp = TreeMap<Long, AtTransaction>()
            temp[height] = this
            all_AT_Txs[atId] = temp
        }
    }

    companion object {
        private val all_AT_Txs = TreeMap<Long, SortedMap<Long, AtTransaction>>()

        fun getATTransaction(atId: Long?, height: Long?): AtTransaction? {
            return if (all_AT_Txs.containsKey(atId)) {
                all_AT_Txs[atId]!![height]
            } else null

        }
    }
}
