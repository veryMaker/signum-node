package brs.util.db

import org.jooq.Record
import org.jooq.Result
import org.jooq.ResultQuery

inline fun <R : Record, T> ResultQuery<out R>.fetchAndMap(mapper: (R) -> T): List<T> {
    // If it is not cast to a list, we get the member function map instead of Kotlin's extension function map.
    val result = this.fetch() as List<R>
    return result.map(mapper)
}

inline fun <R : Record, T> Result<R>.inlineMap(mapper: (R) -> T): Collection<T> {
    // If it is not cast to a list, we get the member function map instead of Kotlin's extension function map.
    return (this as List<R>).map(mapper)
}