package brs.services

import brs.entity.FeeSuggestion

interface FeeSuggestionService {
    /**
     * @return A transaction fee suggestion based on recent blockchain activity
     */
    fun giveFeeSuggestion(): FeeSuggestion
}