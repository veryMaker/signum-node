package brs.services

interface StatisticsService {
    /**
     * Alerts the service that an object was found in the Cache with name [cacheName]
     */
    fun foundObjectInCache(cacheName: String)

    /**
     * Alerts the service that an object was not found in the Cache with name [cacheName]
     */
    fun didNotFindObjectInCache(cacheName: String)

    /**
     * Alerts the service that a new block was pushed
     */
    fun blockAdded()
}