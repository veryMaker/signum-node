package brs.services

interface StatisticsService {
    /**
     * Alerts the service that a new block was pushed
     */
    fun blockAdded()
}
