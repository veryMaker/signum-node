package brs.feesuggestions

import java.util.Arrays

enum class FeeSuggestionType private constructor(val type: String) {
    CHEAP("cheap"), STANDARD("standard"), PRIORITY("priority");


    companion object {

        fun getByType(type: String): FeeSuggestionType {
            return Arrays.stream(values()).filter { s -> s.type == type }.findFirst().orElse(null)
        }
    }
}
