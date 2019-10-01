package brs

import brs.crypto.Crypto
import brs.fluxcapacitor.FluxValues
import brs.props.Props
import brs.taskScheduler.RepeatingTask
import brs.util.Convert
import brs.util.Listeners
import brs.util.toUnsignedString
import burst.kit.crypto.BurstCrypto
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

open class GeneratorImpl private constructor(private val dp: DependencyProvider) : Generator {
    private val listeners = Listeners<Generator.GeneratorState, Generator.Event>()
    private val generators = ConcurrentHashMap<Long, GeneratorStateImpl>() // Remember, this map type cannot take null keys.
    private val burstCrypto = BurstCrypto.getInstance()

    internal val forgeTask: RepeatingTask = {
        try {
            val currentBlock = dp.blockchain.lastBlock.height.toLong()
            val it = generators.entries.iterator()
            while (it.hasNext()) {
                val generator = it.next()
                if (currentBlock < generator.value.block) {
                    generator.value.forge(dp.blockchainProcessor)
                } else {
                    it.remove()
                }
            }
            true
        } catch (e: BlockchainProcessor.BlockNotAcceptedException) {
            logger.debug("Error in block generation thread", e)
            false
        }
    }

    override val allGenerators: Collection<Generator.GeneratorState>
        get() = generators.values

    override fun addListener(listener: (Generator.GeneratorState) -> Unit, eventType: Generator.Event): Boolean {
        return listeners.addListener(listener, eventType)
    }

    override fun removeListener(listener: (Generator.GeneratorState) -> Unit, eventType: Generator.Event): Boolean {
        return listeners.removeListener(listener, eventType)
    }

    override fun addNonce(secretPhrase: String, nonce: Long?): Generator.GeneratorState {
        val publicKey = Crypto.getPublicKey(secretPhrase)
        return addNonce(secretPhrase, nonce, publicKey)
    }

    override fun addNonce(secretPhrase: String, nonce: Long?, publicKey: ByteArray): Generator.GeneratorState {
        val publicKeyHash = Crypto.sha256().digest(publicKey)
        val id = Convert.fullHashToId(publicKeyHash)

        val generator = GeneratorStateImpl(secretPhrase, nonce, publicKey, id)
        val curGen = generators[id]
        if (curGen == null || generator.block > curGen.block || generator.deadline < curGen.deadline) {
            generators[id] = generator
            listeners.accept(generator, Generator.Event.NONCE_SUBMITTED)
            if (logger.isDebugEnabled) {
                logger.debug("Account {} started mining, deadline {} seconds", id.toUnsignedString(), generator.deadline)
            }
        } else {
            if (logger.isDebugEnabled) {
                logger.debug("Account {} already has a better nonce", id.toUnsignedString())
            }
        }

        return generator
    }

    override fun calculateGenerationSignature(lastGenSig: ByteArray, lastGenId: Long): ByteArray {
        return burstCrypto.calculateGenerationSignature(lastGenSig, lastGenId)
    }

    override fun calculateScoop(genSig: ByteArray, height: Long): Int {
        return burstCrypto.calculateScoop(genSig, height)
    }

    private fun getPocVersion(blockHeight: Int): Int {
        return if (dp.fluxCapacitor.getValue(FluxValues.POC2, blockHeight)) 2 else 1
    }

    override fun calculateHit(accountId: Long, nonce: Long, genSig: ByteArray, scoop: Int, blockHeight: Int): BigInteger {
        return burstCrypto.calculateHit(accountId, nonce, genSig, scoop, getPocVersion(blockHeight))
    }

    override fun calculateHit(accountId: Long, nonce: Long, genSig: ByteArray, scoopData: ByteArray): BigInteger {
        return burstCrypto.calculateHit(accountId, nonce, genSig, scoopData)
    }

    override fun calculateDeadline(accountId: Long, nonce: Long, genSig: ByteArray, scoop: Int, baseTarget: Long, blockHeight: Int): BigInteger {
        return burstCrypto.calculateDeadline(accountId, nonce, genSig, scoop, baseTarget, getPocVersion(blockHeight))
    }

    inner class GeneratorStateImpl internal constructor(private val secretPhrase: String, private val nonce: Long?, override val publicKey: ByteArray, override val accountId: Long?) : Generator.GeneratorState {
        override val deadline: BigInteger
        override val block: Long

        init {

            val lastBlock = dp.blockchain.lastBlock

            this.block = lastBlock.height.toLong() + 1

            val lastGenSig = lastBlock.generationSignature
            val lastGenerator = lastBlock.generatorId

            val newGenSig = calculateGenerationSignature(lastGenSig, lastGenerator)

            val scoopNum = calculateScoop(newGenSig, lastBlock.height + 1L)

            deadline = calculateDeadline(accountId!!, nonce!!, newGenSig, scoopNum, lastBlock.baseTarget, lastBlock.height + 1)
        }// need to store publicKey in addition to accountId, because the account may not have had its publicKey set yet

        internal fun forge(blockchainProcessor: BlockchainProcessor) {
            val lastBlock = dp.blockchain.lastBlock

            val elapsedTime = dp.timeService.epochTime - lastBlock.timestamp
            if (BigInteger.valueOf(elapsedTime.toLong()) > deadline) {
                blockchainProcessor.generateBlock(secretPhrase, publicKey, nonce)
            }
        }
    }

    class MockGenerator(private val dp: DependencyProvider) : GeneratorImpl(dp) {

        override fun calculateHit(accountId: Long, nonce: Long, genSig: ByteArray, scoop: Int, blockHeight: Int): BigInteger {
            return BigInteger.valueOf(dp.propertyService.get(Props.DEV_MOCK_MINING_DEADLINE).toLong())
        }

        override fun calculateHit(accountId: Long, nonce: Long, genSig: ByteArray, scoopData: ByteArray): BigInteger {
            return BigInteger.valueOf(dp.propertyService.get(Props.DEV_MOCK_MINING_DEADLINE).toLong())
        }

        override fun calculateDeadline(accountId: Long, nonce: Long, genSig: ByteArray, scoop: Int, baseTarget: Long, blockHeight: Int): BigInteger {
            return BigInteger.valueOf(dp.propertyService.get(Props.DEV_MOCK_MINING_DEADLINE).toLong())
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GeneratorImpl::class.java)

        suspend fun new(dp: DependencyProvider): Generator {
            val generator = GeneratorImpl(dp)

            dp.taskScheduler.scheduleTask(generator.forgeTask)

            return generator
        }
    }
}
