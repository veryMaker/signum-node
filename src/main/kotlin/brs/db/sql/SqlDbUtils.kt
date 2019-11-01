package brs.db.sql

import org.jooq.SelectQuery

internal object SqlDbUtils {
    fun applyLimits(query: SelectQuery<*>, from: Int, to: Int) {
        val limit = if (to >= 0 && to >= from && to < Integer.MAX_VALUE) to - from + 1 else 0
        if (limit > 0 && from > 0) {
            query.addLimit(from, limit)
        } else if (limit > 0) {
            query.addLimit(limit)
        } else if (from > 0) {
            query.addOffset(from)
        }
    }
}
