package brs.services

interface StatisticsService {
    /**
     * TODO
     */
    fun foundObjectInCache(cacheName: String)

    /**
     * TODO
     */
    fun didNotFindObjectInCache(cacheName: String)

    /**
     * TODO
     */
    fun blockAdded()
}