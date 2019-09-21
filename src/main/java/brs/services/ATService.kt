package brs.services

import brs.at.AT

interface ATService {
    val allATIds: Collection<Long>

    fun getATsIssuedBy(accountId: Long?): List<Long>

    fun getAT(id: Long?): AT?
}
