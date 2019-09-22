package brs.transaction.coloredCoins

import brs.DependencyProvider
import brs.transaction.TransactionType

abstract class ColoredCoins(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TransactionType.TYPE_COLORED_COINS
}
