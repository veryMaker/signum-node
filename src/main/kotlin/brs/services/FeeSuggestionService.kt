package brs.services

import brs.entity.FeeSuggestion

interface FeeSuggestionService {
    fun giveFeeSuggestion(): FeeSuggestion
}