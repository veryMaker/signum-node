package brs.feesuggestions

enum class FeeSuggestionType constructor(val type: String) {
    CHEAP("cheap"),
    STANDARD("standard"),
    PRIORITY("priority");

    companion object {
        fun getByType(type: String): FeeSuggestionType? {
            return when (type) {
                CHEAP.type -> CHEAP
                STANDARD.type -> STANDARD
                PRIORITY.type -> PRIORITY
                else -> null
            }
        }
    }
}
