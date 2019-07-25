package brs.common

import java.util.Arrays

abstract class AbstractUnitTest {
    @SafeVarargs
    protected fun <T> mockCollection(vararg items: T): Collection<T> {
        return listOf<T>(*items)
    }

    protected fun stringWithLength(length: Int): String {
        val result = StringBuilder()

        for (i in 0 until length) {
            result.append("a")
        }

        return result.toString()
    }

}
