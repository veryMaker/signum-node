package brs.services

interface StatisticsService {
    fun foundObjectInCache(cacheName: String)
    fun didNotFindObjectInCache(cacheName: String)
    fun blockAdded()
}