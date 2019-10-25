package brs.services

import brs.entity.Block
import brs.entity.Transaction

interface EconomicClusteringService {
    fun getECBlock(timestamp: Int): Block
    fun verifyFork(transaction: Transaction): Boolean
}