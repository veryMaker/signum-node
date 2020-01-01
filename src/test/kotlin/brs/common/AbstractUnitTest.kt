package brs.common

abstract class AbstractUnitTest {
    @SafeVarargs
    protected fun <T> mockCollection(vararg items: T): Collection<T> {
        return listOf(*items)
    }

    protected fun stringWithLength(length: Int): String {
        val result = StringBuilder()

        repeat(length) {
            result.append("a")
        }

        return result.toString()
    }
}
