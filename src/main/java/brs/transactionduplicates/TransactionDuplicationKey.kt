package brs.transactionduplicates

import brs.TransactionType

class TransactionDuplicationKey(internal val transactionType: TransactionType?, internal val key: String) {
    companion object {
        val IS_ALWAYS_DUPLICATE = TransactionDuplicationKey(null, "always")

        val IS_NEVER_DUPLICATE = TransactionDuplicationKey(null, "never")
    }
}
