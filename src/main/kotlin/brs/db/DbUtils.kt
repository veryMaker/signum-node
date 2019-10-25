package brs.db

import org.jooq.DSLContext

inline fun <T> Db.getUsingDslContext(function: (DSLContext) -> T): T {
    getDslContext().use { context -> return function(context) }
}

inline fun Db.useDslContext(consumer: (DSLContext) -> Unit) {
    getDslContext().use { context -> consumer(context) }
}
