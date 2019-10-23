package brs.services

import brs.at.AT

interface ATService {
    suspend fun getAllATIds(): Collection<Long>

    suspend fun getATsIssuedBy(accountId: Long?): List<Long>

    suspend fun getAT(id: Long?): AT?
}
