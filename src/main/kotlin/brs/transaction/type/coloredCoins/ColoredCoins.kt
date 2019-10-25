package brs.transaction.type.coloredCoins

import brs.DependencyProvider
import brs.transaction.type.TransactionType

abstract class ColoredCoins(dp: DependencyProvider) : TransactionType(dp) {
    override val type = TYPE_COLORED_COINS
}
