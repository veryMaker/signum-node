package brs.services.impl

import brs.entity.Block
import brs.entity.DependencyProvider
import brs.entity.Transaction
import brs.objects.Constants
import brs.objects.FluxValues
import brs.services.EconomicClusteringService
import brs.util.json.toJsonString
import brs.util.logging.safeDebug
import org.slf4j.LoggerFactory

/**
 * Economic Clustering concept (EC) solves the most critical flaw of "classical" Proof-of-Stake - the problem called
 * "Nothing-at-Stake".
 *
 * I ought to respect BCNext's wish and say that this concept is inspired by Economic Majority idea of Meni Rosenfeld
 * (http://en.wikipedia.org/wiki/User:Meni_Rosenfeld).
 *
 * EC is a vital part of Transparent Forging. Words "Mining in Nxt relies on cooperation of people and even forces it"
 * (https://bitcointalk.org/index.php?topic=553205.0) were said about EC.
 *
 * Keep in mind that this concept has not been peer reviewed. You are very welcome to do it...
 *
 * Come-from-Beyond (21.05.2014)
 */
class EconomicClusteringServiceImpl(private val dp: DependencyProvider) : EconomicClusteringService {
    override fun getECBlock(timestamp: Int): Block {
        var block: Block? = dp.blockchainService.lastBlock
        require(timestamp >= block!!.timestamp - 15) { "Timestamp cannot be more than 15 s earlier than last block timestamp: " + block!!.timestamp }
        var distance = 0
        while (block!!.timestamp > timestamp - Constants.EC_RULE_TERMINATOR && distance < Constants.EC_BLOCK_DISTANCE_LIMIT) {
            block = dp.blockchainService.getBlock(block.previousBlockId)
            distance += 1
        }
        return block
    }

    override fun verifyFork(transaction: Transaction): Boolean {
        try {
            if (!dp.fluxCapacitorService.getValue(FluxValues.DIGITAL_GOODS_STORE)) return true
            if (transaction.referencedTransactionFullHash != null) return true
            if (dp.blockchainService.height < Constants.EC_CHANGE_BLOCK_1 && dp.blockchainService.height - transaction.ecBlockHeight > Constants.EC_BLOCK_DISTANCE_LIMIT) return false
            val ecBlock = dp.blockchainService.getBlock(transaction.ecBlockId)
            return ecBlock != null && ecBlock.height == transaction.ecBlockHeight
        } catch (e: NullPointerException) {
            logger.safeDebug { "caught null pointer exception during verifyFork with transaction: ${transaction.toJsonObject().toJsonString()}" }
            throw e
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EconomicClusteringServiceImpl::class.java)
    }
}
