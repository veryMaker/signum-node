package brs.util.misc

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <T> Iterable<T>.filterWithLimits(firstIndex: Int, lastIndex: Int, filter: (T) -> Boolean): Iterable<T> {
    return asSequence()
        .filter(filter)
        .drop(firstIndex)
        .take(lastIndex - firstIndex)
        .asIterable()
}

@UseExperimental(ExperimentalContracts::class)
inline fun <T> Iterable<T>.countFilterResults(filter: (T) -> Boolean): Int {
    contract { callsInPlace(filter, InvocationKind.UNKNOWN) }
    var sum = 0
    for (element in this) {
        if (filter(element)) sum++
    }
    return sum
}
