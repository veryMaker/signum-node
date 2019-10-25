package brs.transaction.unconfirmed

import brs.entity.Transaction

internal class UnconfirmedTransactionTiming(val transaction: Transaction, val timestamp: Long)
