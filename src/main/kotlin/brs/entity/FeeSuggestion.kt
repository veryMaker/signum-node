package brs.entity

class FeeSuggestion(val cheapFee: Long, val standardFee: Long, val priorityFee: Long) {
    enum class Type(val type: String) {
        CHEAP("cheap"),
        STANDARD("standard"),
        PRIORITY("priority");

        companion object {
            fun getByType(type: String): Type? {
                return when (type) {
                    CHEAP.type -> CHEAP
                    STANDARD.type -> STANDARD
                    PRIORITY.type -> PRIORITY
                    else -> null
                }
            }
        }
    }
}
