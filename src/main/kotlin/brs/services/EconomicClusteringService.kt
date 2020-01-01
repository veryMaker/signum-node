package brs.services

import brs.entity.Block
import brs.entity.Transaction

interface EconomicClusteringService {
    /**
     * Gets the EC block at a certain timestamp.
     * @param timestamp The timestamp at which the EC block should be valid. Must not be more than [brs.objects.Constants.MAX_TIMESTAMP_DIFFERENCE] seconds earlier than the most recent block.
     */
    fun getECBlock(timestamp: Int): Block

    /**
     * TODO
     */
    fun verifyFork(transaction: Transaction): Boolean
}