package brs.services

import brs.entity.Block
import brs.entity.Transaction

interface EconomicClusteringService {
    /**
     * TODO
     */
    fun getECBlock(timestamp: Int): Block

    /**
     * TODO
     */
    fun verifyFork(transaction: Transaction): Boolean
}