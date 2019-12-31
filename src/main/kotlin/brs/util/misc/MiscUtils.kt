package brs.util.misc

fun <T> Iterable<T>.filterWithLimits(firstIndex: Int, lastIndex: Int, filter: (T) -> Boolean): Iterable<T> {
    return asSequence()
        .filter(filter)
        .drop(firstIndex)
        .take(lastIndex - firstIndex)
        .asIterable()
}

/**
 * Applies the given [transform] function to each element of the original collection,
 * and appends the result of the [transform] to the given [destination] unless it was null.
 * An item can be filtered out by the transform returning null.
 */
inline fun <T: Any, R: Any, C : MutableCollection<in R>> Iterable<T>.filteringMapTo(destination: C, transform: (T) -> R?): C {
    this.forEach {
        val transformed = transform(it)
        if (transformed != null) destination.add(transformed)
    }
    return destination
}
