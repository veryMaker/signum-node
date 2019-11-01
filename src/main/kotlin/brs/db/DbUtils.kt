package brs.db

import org.jooq.DSLContext

/**
 * Use the DSL Context of the DB and return a value
 * @param action The action to perform using the DSL Context, which returns a value
 * @return The value fetched by the `action`
 */
inline fun <T> Db.getUsingDslContext(action: (DSLContext) -> T): T {
    getDslContext().use { context -> return action(context) }
}

/**
 * Use the DSL Context of the DB
 * @param action The action to perform using the DSL Context
 */
inline fun Db.useDslContext(action: (DSLContext) -> Unit) {
    getDslContext().use { context -> action(context) }
}
