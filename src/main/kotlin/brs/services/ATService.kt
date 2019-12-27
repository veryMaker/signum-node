package brs.services

import brs.at.AT

interface ATService {
    /**
     * TODO
     */
    fun getAllATIds(): Collection<Long>

    /**
     * TODO
     */
    fun getATsIssuedBy(accountId: Long): List<Long>

    /**
     * TODO
     */
    fun getAT(id: Long): AT?
}
