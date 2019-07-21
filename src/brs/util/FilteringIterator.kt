package brs.util

import java.util.*

class FilteringIterator<T> @JvmOverloads constructor(collection: Collection<T>, private val filter: (T?) -> Boolean, private val from: Int = 0, private val to: Int = Integer.MAX_VALUE) : MutableIterator<T> {
    private val dbIterator: Iterator<T> = collection.iterator()
    private var next: T? = null
    private var hasNext: Boolean = false
    private var count: Int = 0

    override fun hasNext(): Boolean {
        if (hasNext) {
            return true
        }
        while (dbIterator.hasNext() && count <= to) {
            next = dbIterator.next()
            if (filter(next)) {
                if (count >= from) {
                    count += 1
                    hasNext = true
                    return true
                }
                count += 1
            }
        }
        hasNext = false
        return false
    }

    override fun next(): T {
        if (hasNext) {
            hasNext = false
            // TODO remove null assertions
            return next!!
        }
        while (dbIterator.hasNext() && count <= to) {
            next = dbIterator.next()
            if (filter(next)) {
                if (count >= from) {
                    count += 1
                    hasNext = false
                    return next!!
                }
                count += 1
            }
        }
        throw NoSuchElementException()
    }

    override fun remove() {
        throw UnsupportedOperationException()
    }
}
