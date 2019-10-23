package brs.at

import brs.DependencyProvider
import brs.crypto.Crypto
import brs.fluxcapacitor.FluxValues
import brs.util.convert.toUnsignedString
import brs.util.logging.safeDebug
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import kotlin.math.abs

class AtApiPlatformImpl constructor(private val dp: DependencyProvider) : AtApiImpl(dp) {
    override fun getBlockTimestamp(state: AtMachineState): Long {
        val height = state.height
        return AtApiHelper.getLongTimestamp(height, 0)
    }

    override fun getCreationTimestamp(state: AtMachineState): Long {
        return AtApiHelper.getLongTimestamp(state.creationBlockHeight, 0)
    }

    override fun getLastBlockTimestamp(state: AtMachineState): Long {
        val height = state.height - 1
        return AtApiHelper.getLongTimestamp(height, 0)
    }

    override suspend fun putLastBlockHashInA(state: AtMachineState) {
        val b = ByteBuffer.allocate(state.a1.size * 4)
        b.order(ByteOrder.LITTLE_ENDIAN)

        b.put(dp.blockchain.getBlockAtHeight(state.height - 1)!!.hash)

        b.clear()

        val temp = ByteArray(8)

        b.get(temp, 0, 8)
        state.a1 = temp

        b.get(temp, 0, 8)
        state.a2 = temp

        b.get(temp, 0, 8)
        state.a3 = temp

        b.get(temp, 0, 8)
        state.a4 = temp
    }

    override suspend fun aToTxAfterTimestamp(`val`: Long, state: AtMachineState) {
        val height = AtApiHelper.longToHeight(`val`)
        val numOfTx = AtApiHelper.longToNumOfTx(`val`)

        val b = state.id

        val tx = findTransaction(dp, height, state.height, AtApiHelper.getLong(b!!), numOfTx, state.minActivationAmount())!!
        logger.safeDebug { "tx with id ${tx.toUnsignedString()} found" }
        clearA(state)
        state.a1 = AtApiHelper.getByteArray(tx)
    }

    override suspend fun getTypeForTxInA(state: AtMachineState): Long {
        val txid = AtApiHelper.getLong(state.a1)

        val tx = dp.blockchain.getTransaction(txid)

        if (tx == null || tx.height >= state.height) {
            return -1
        }

        return if (tx.message != null) {
            1
        } else 0

    }

    override suspend fun getAmountForTxInA(state: AtMachineState): Long {
        val txId = AtApiHelper.getLong(state.a1)

        val tx = dp.blockchain.getTransaction(txId)

        if (tx == null || tx.height >= state.height) {
            return -1
        }

        return if ((tx.message == null || dp.fluxCapacitor.getValue(FluxValues.AT_FIX_BLOCK_2, state.height)) && state.minActivationAmount() <= tx.amountNQT) {
            tx.amountNQT - state.minActivationAmount()
        } else 0

    }

    override suspend fun getTimestampForTxInA(state: AtMachineState): Long {
        val txId = AtApiHelper.getLong(state.a1)
        logger.safeDebug { "get timestamp for tx with id ${txId.toUnsignedString()} found" }
        val tx = dp.blockchain.getTransaction(txId)

        if (tx == null || tx.height >= state.height) {
            return -1
        }

        val b = state.id
        val blockHeight = tx.height
        val txHeight = findTransactionHeight(dp, txId, blockHeight, AtApiHelper.getLong(b!!), state.minActivationAmount())

        return AtApiHelper.getLongTimestamp(blockHeight, txHeight)
    }

    override suspend fun getRandomIdForTxInA(state: AtMachineState): Long {
        val txId = AtApiHelper.getLong(state.a1)

        val tx = dp.blockchain.getTransaction(txId)

        if (tx == null || tx.height >= state.height) {
            return -1
        }

        val txBlockHeight = tx.height
        val blockHeight = state.height

        if (blockHeight - txBlockHeight < dp.atConstants.blocksForRandom(blockHeight)) { //for tests - for real case 1440
            state.waitForNumberOfBlocks = dp.atConstants.blocksForRandom(blockHeight).toInt() - (blockHeight - txBlockHeight)
            state.machineState.pc -= 7
            state.machineState.stopped = true
            return 0
        }

        val digest = Crypto.sha256()

        val senderPublicKey = tx.senderPublicKey

        val bf = ByteBuffer.allocate(32 + java.lang.Long.SIZE + senderPublicKey.size)
        bf.order(ByteOrder.LITTLE_ENDIAN)
        bf.put(dp.blockchain.getBlockAtHeight(blockHeight - 1)!!.generationSignature)
        bf.putLong(tx.id)
        bf.put(senderPublicKey)

        digest.update(bf.array())
        val byteRandom = digest.digest()

        return abs(AtApiHelper.getLong(Arrays.copyOfRange(byteRandom, 0, 8)))
    }

    override suspend fun messageFromTxInAToB(state: AtMachineState) {
        val txid = AtApiHelper.getLong(state.a1)

        var tx = dp.blockchain.getTransaction(txid)
        if (tx != null && tx.height >= state.height) {
            tx = null
        }

        val b = ByteBuffer.allocate(state.a1.size * 4)
        b.order(ByteOrder.LITTLE_ENDIAN)
        if (tx != null) {
            val txMessage = tx.message
            if (txMessage != null) {
                val message = txMessage.messageBytes
                if (message.size <= state.a1.size * 4) {
                    b.put(message)
                }
            }
        }

        b.clear()

        val temp = ByteArray(8)

        b.get(temp, 0, 8)
        state.b1 = temp

        b.get(temp, 0, 8)
        state.b2 = temp

        b.get(temp, 0, 8)
        state.b3 = temp

        b.get(temp, 0, 8)
        state.b4 = temp

    }

    override suspend fun bToAddressOfTxInA(state: AtMachineState) {
        val txId = AtApiHelper.getLong(state.a1)

        clearB(state)

        var tx = dp.blockchain.getTransaction(txId)
        if (tx != null && tx.height >= state.height) {
            tx = null
        }
        if (tx != null) {
            val address = tx.senderId
            state.b1 = AtApiHelper.getByteArray(address)
        }
    }

    override fun bToAddressOfCreator(state: AtMachineState) {
        val creator = AtApiHelper.getLong(state.creator!!)

        clearB(state)

        state.b1 = AtApiHelper.getByteArray(creator)

    }

    override suspend fun putLastBlockGenerationSignatureInA(state: AtMachineState) {
        val b = ByteBuffer.allocate(state.a1.size * 4)
        b.order(ByteOrder.LITTLE_ENDIAN)

        b.put(dp.blockchain.getBlockAtHeight(state.height - 1)!!.generationSignature)

        val temp = ByteArray(8)

        b.get(temp, 0, 8)
        state.a1 = temp

        b.get(temp, 0, 8)
        state.a2 = temp

        b.get(temp, 0, 8)
        state.a3 = temp

        b.get(temp, 0, 8)
        state.a4 = temp
    }

    override fun getCurrentBalance(state: AtMachineState): Long {
        return if (!dp.fluxCapacitor.getValue(FluxValues.AT_FIX_BLOCK_2, state.height)) {
            0
        } else state.getgBalance()

    }

    override fun getPreviousBalance(state: AtMachineState): Long {
        return if (!dp.fluxCapacitor.getValue(FluxValues.AT_FIX_BLOCK_2, state.height)) {
            0
        } else state.getpBalance()

    }

    override fun sendToAddressInB(`val`: Long, state: AtMachineState) {
        if (`val` < 1)
            return

        if (`val` < state.getgBalance()) {
            val tx = AtTransaction(state.id!!, state.b1.clone(), `val`, null)
            state.addTransaction(tx)

            state.setgBalance(state.getgBalance() - `val`)
        } else {
            val tx = AtTransaction(state.id!!, state.b1.clone(), state.getgBalance(), null)
            state.addTransaction(tx)

            state.setgBalance(0L)
        }
    }

    override fun sendAllToAddressInB(state: AtMachineState) {
        val tx = AtTransaction(state.id!!, state.b1.clone(), state.getgBalance(), null)
        state.addTransaction(tx)
        state.setgBalance(0L)
    }

    override fun sendOldToAddressInB(state: AtMachineState) {
        if (state.getpBalance() > state.getgBalance()) {
            val tx = AtTransaction(state.id!!, state.b1, state.getgBalance(), null)
            state.addTransaction(tx)

            state.setgBalance(0L)
            state.setpBalance(0L)
        } else {
            val tx = AtTransaction(state.id!!, state.b1, state.getpBalance(), null)
            state.addTransaction(tx)

            state.setgBalance(state.getgBalance() - state.getpBalance())
            state.setpBalance(0L)
        }
    }

    override fun sendAToAddressInB(state: AtMachineState) {
        val b = ByteBuffer.allocate(32)
        b.order(ByteOrder.LITTLE_ENDIAN)
        b.put(state.a1)
        b.put(state.a2)
        b.put(state.a3)
        b.put(state.a4)
        b.clear()

        val tx = AtTransaction(state.id!!, state.b1, 0L, b.array())
        state.addTransaction(tx)
    }

    override fun addMinutesToTimestamp(val1: Long, val2: Long, state: AtMachineState): Long {
        val height = AtApiHelper.longToHeight(val1)
        val numOfTx = AtApiHelper.longToNumOfTx(val1)
        val addHeight = height + (val2 / dp.atConstants.averageBlockMinutes(state.height)).toInt()

        return AtApiHelper.getLongTimestamp(addHeight, numOfTx)
    }

    companion object {

        private val logger = LoggerFactory.getLogger(AtApiPlatformImpl::class.java)

        // TODO remove methods taking dp
        private suspend fun findTransaction(dp: DependencyProvider, startHeight: Int, endHeight: Int, atID: Long?, numOfTx: Int, minAmount: Long): Long? {
            return dp.atStore.findTransaction(startHeight, endHeight, atID, numOfTx, minAmount)
        }

        private suspend fun findTransactionHeight(dp: DependencyProvider, transactionId: Long?, height: Int, atID: Long?, minAmount: Long): Int {
            return dp.atStore.findTransactionHeight(transactionId, height, atID, minAmount)
        }
    }
}
