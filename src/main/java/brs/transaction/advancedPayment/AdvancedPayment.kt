package brs.transaction.advancedPayment

import brs.DependencyProvider
import brs.transaction.TransactionType

abstract class AdvancedPayment(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TYPE_ADVANCED_PAYMENT
}