package brs.transaction.type.advancedPayment

import brs.DependencyProvider
import brs.transaction.type.TransactionType

abstract class AdvancedPayment(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TYPE_ADVANCED_PAYMENT
}