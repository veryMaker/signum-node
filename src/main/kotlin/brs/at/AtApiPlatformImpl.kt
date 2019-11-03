package brs.at

import brs.at.AtApi.Companion.REGISTER_PART_SIZE
import brs.at.AtApi.Companion.REGISTER_SIZE
import brs.entity.DependencyProvider
import brs.objects.FluxValues
import brs.util.byteArray.zero
import brs.util.convert.toUnsignedString
import brs.util.crypto.Crypto
import brs.util.logging.safeDebug
import org.slf4j.LoggerFactory
import java.nio.BufferUnderflowException
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

    override fun putLastBlockHashInA(state: AtMachineState) {
        state.putInA(dp.blockchainService.getBlockAtHeight(state.height - 1)!!.hash)
    }

    override fun aToTxAfterTimestamp(value: Long, state: AtMachineState) {
        val height = AtApiHelper.longToHeight(value)
        val numOfTx = AtApiHelper.longToNumOfTx(value)

        val b = state.id

        val tx = dp.atStore.findTransaction(
            height,
            state.height,
            AtApiHelper.getLong(b!!),
            numOfTx,
            state.minActivationAmount()
        )!!
        logger.safeDebug { "tx with id ${tx.toUnsignedString()} found" }
        clearA(state)
        AtApiHelper.getByteArray(tx, state.a1)
    }

    override fun getTypeForTxInA(state: AtMachineState): Long {
        val txid = AtApiHelper.getLong(state.a1)

        val tx = dp.blockchainService.getTransaction(txid)

        if (tx == null || tx.height >= state.height) {
            return -1
        }

        return if (tx.message != null) {
            1
        } else 0

    }

    override fun getAmountForTxInA(state: AtMachineState): Long {
        val txId = AtApiHelper.getLong(state.a1)

        val tx = dp.blockchainService.getTransaction(txId)

        if (tx == null || tx.height >= state.height) {
            return -1
        }

        return if ((tx.message == null || dp.fluxCapacitorService.getValue(
                FluxValues.AT_FIX_BLOCK_2,
                state.height
            )) && state.minActivationAmount() <= tx.amountPlanck
        ) {
            tx.amountPlanck - state.minActivationAmount()
        } else 0

    }

    override fun getTimestampForTxInA(state: AtMachineState): Long {
        val txId = AtApiHelper.getLong(state.a1)
        logger.safeDebug { "get timestamp for tx with id ${txId.toUnsignedString()} found" }
        val tx = dp.blockchainService.getTransaction(txId)

        if (tx == null || tx.height >= state.height) {
            return -1
        }

        val b = state.id
        val blockHeight = tx.height
        val txHeight =
            dp.atStore.findTransactionHeight(txId, blockHeight, AtApiHelper.getLong(b!!), state.minActivationAmount())

        return AtApiHelper.getLongTimestamp(blockHeight, txHeight)
    }

    override fun getRandomIdForTxInA(state: AtMachineState): Long {
        val txId = AtApiHelper.getLong(state.a1)

        val tx = dp.blockchainService.getTransaction(txId)

        if (tx == null || tx.height >= state.height) {
            return -1
        }

        val txBlockHeight = tx.height
        val blockHeight = state.height

        if (blockHeight - txBlockHeight < dp.atConstants.blocksForRandom(blockHeight)) { //for tests - for real case 1440
            state.waitForNumberOfBlocks =
                dp.atConstants.blocksForRandom(blockHeight).toInt() - (blockHeight - txBlockHeight)
            state.machineState.pc -= 7
            state.machineState.stopped = true
            return 0
        }

        val digest = Crypto.sha256()
        digest.update(dp.blockchainService.getBlockAtHeight(blockHeight - 1)!!.generationSignature)
        digest.update(AtApiHelper.getByteArray(tx.id))
        digest.update(tx.senderPublicKey)
        digest.update(ByteArray(56)) // FIXME remove this in next fork
        val byteRandom = digest.digest()

        // TODO don't copy of range
        return abs(AtApiHelper.getLong(byteRandom.copyOfRange(0, REGISTER_PART_SIZE)))
    }

    override fun messageFromTxInAToB(state: AtMachineState) {
        val txid = AtApiHelper.getLong(state.a1)

        var tx = dp.blockchainService.getTransaction(txid)
        if (tx != null && tx.height >= state.height) {
            tx = null
        }

        if (tx != null) {
            val txMessage = tx.message
            if (txMessage != null) {
                var message = txMessage.messageBytes
                if (message.size <= REGISTER_SIZE) {
                    if (message.size < REGISTER_SIZE) {
                        val newMessage = ByteArray(REGISTER_SIZE)
                        message.copyInto(newMessage)
                        message = newMessage
                    }
                    state.putInB(message)
                    return
                }
            }
        }
        state.b1.zero()
        state.b2.zero()
        state.b3.zero()
        state.b4.zero()
    }

    override fun bToAddressOfTxInA(state: AtMachineState) {
        val txId = AtApiHelper.getLong(state.a1)

        clearB(state)

        var tx = dp.blockchainService.getTransaction(txId)
        if (tx != null && tx.height >= state.height) {
            tx = null
        }
        if (tx != null) {
            val address = tx.senderId
            AtApiHelper.getByteArray(address, state.b1)
        }
    }

    override fun bToAddressOfCreator(state: AtMachineState) {
        val creator = AtApiHelper.getLong(state.creator!!)

        clearB(state)

        AtApiHelper.getByteArray(creator, state.b1)
    }

    override fun putLastBlockGenerationSignatureInA(state: AtMachineState) {
        if (dp.fluxCapacitorService.getValue(FluxValues.NEXT_FORK)) {
            state.putInA(dp.blockchainService.getBlockAtHeight(state.height - 1)!!.generationSignature)
        } else {
            // Fast fail.
            throw BufferUnderflowException()
        }
    }

    override fun getCurrentBalance(state: AtMachineState): Long {
        return if (!dp.fluxCapacitorService.getValue(FluxValues.AT_FIX_BLOCK_2, state.height)) {
            0
        } else state.getgBalance()

    }

    override fun getPreviousBalance(state: AtMachineState): Long {
        return if (!dp.fluxCapacitorService.getValue(FluxValues.AT_FIX_BLOCK_2, state.height)) {
            0
        } else state.getpBalance()
    }

    override fun sendToAddressInB(value: Long, state: AtMachineState) {
        if (value < 1)
            return

        if (value < state.getgBalance()) {
            val tx = AtTransaction(state.id!!, state.b1.clone(), value, null)
            state.addTransaction(tx)

            state.setgBalance(state.getgBalance() - value)
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
        val b = ByteArray(REGISTER_SIZE)
        state.a1.copyInto(b, 0)
        state.a2.copyInto(b, 8)
        state.a3.copyInto(b, 16)
        state.a4.copyInto(b, 24)

        val tx = AtTransaction(state.id!!, state.b1, 0L, b)
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
    }
}
