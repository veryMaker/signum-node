package brs.services

interface TimeService {
    /**
     * The current time, in number of seconds since Burst Epoch
     */
    val epochTime: Int
}
