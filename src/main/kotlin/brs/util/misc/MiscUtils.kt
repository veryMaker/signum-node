package brs.util.misc

fun <T> Iterable<T>.filterWithLimits(firstIndex: Int, lastIndex: Int, filter: (T) -> Boolean): Iterable<T> {
    return asSequence()
        .filter(filter)
        .drop(firstIndex)
        .take(lastIndex - firstIndex)
        .asIterable()
}
