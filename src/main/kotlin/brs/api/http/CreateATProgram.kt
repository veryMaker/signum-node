package brs.api.http

import brs.api.http.JSONResponses.INCORRECT_AUTOMATED_TRANSACTION_DESCRIPTION
import brs.api.http.JSONResponses.INCORRECT_AUTOMATED_TRANSACTION_NAME
import brs.api.http.JSONResponses.INCORRECT_AUTOMATED_TRANSACTION_NAME_LENGTH
import brs.api.http.JSONResponses.MISSING_CODE
import brs.api.http.JSONResponses.MISSING_NAME
import brs.api.http.common.Parameters.CODE_PARAMETER
import brs.api.http.common.Parameters.CREATION_BYTES_PARAMETER
import brs.api.http.common.Parameters.CSPAGES_PARAMETER
import brs.api.http.common.Parameters.DATA_PARAMETER
import brs.api.http.common.Parameters.DESCRIPTION_PARAMETER
import brs.api.http.common.Parameters.DPAGES_PARAMETER
import brs.api.http.common.Parameters.MIN_ACTIVATION_AMOUNT_PLANCK_PARAMETER
import brs.api.http.common.Parameters.NAME_PARAMETER
import brs.api.http.common.Parameters.USPAGES_PARAMETER
import brs.api.http.common.ResultFields.ERROR_CODE_RESPONSE
import brs.api.http.common.ResultFields.ERROR_DESCRIPTION_RESPONSE
import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.objects.Constants.EMPTY_BYTE_ARRAY
import brs.transaction.appendix.Attachment
import brs.util.convert.parseHexString
import brs.util.convert.parseUnsignedLong
import brs.util.jetty.get
import brs.util.logging.safeDebug
import brs.util.string.isInAlphabet
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class CreateATProgram(private val dp: DependencyProvider) : CreateTransaction(
    dp,
    arrayOf(APITag.AT, APITag.CREATE_TRANSACTION),
    NAME_PARAMETER,
    DESCRIPTION_PARAMETER,
    CREATION_BYTES_PARAMETER,
    CODE_PARAMETER,
    DATA_PARAMETER,
    DPAGES_PARAMETER,
    CSPAGES_PARAMETER,
    USPAGES_PARAMETER,
    MIN_ACTIVATION_AMOUNT_PLANCK_PARAMETER
) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        var name = request[NAME_PARAMETER] ?: return MISSING_NAME
        val description: String? = request[DESCRIPTION_PARAMETER]

        name = name.trim { it <= ' ' }
        if (name.length > Constants.MAX_AUTOMATED_TRANSACTION_NAME_LENGTH) {
            return INCORRECT_AUTOMATED_TRANSACTION_NAME_LENGTH
        }

        if (!name.isInAlphabet()) {
            return INCORRECT_AUTOMATED_TRANSACTION_NAME
        }

        if (description != null && description.length > Constants.MAX_AUTOMATED_TRANSACTION_DESCRIPTION_LENGTH) {
            return INCORRECT_AUTOMATED_TRANSACTION_DESCRIPTION
        }

        var creationBytes: ByteArray? = null

        if (request[CODE_PARAMETER] != null) {
            try {
                val code = request[CODE_PARAMETER]?.parseHexString() ?: return MISSING_CODE
                val data = request[DATA_PARAMETER]?.parseHexString() ?: EMPTY_BYTE_ARRAY

                val cpages = code.size / 256 + if (code.size % 256 != 0) 1 else 0
                val dpages = Integer.parseInt(request[DPAGES_PARAMETER])
                val cspages = Integer.parseInt(request[CSPAGES_PARAMETER])
                val uspages = Integer.parseInt(request[USPAGES_PARAMETER])

                require(dpages >= 0)
                require(cspages >= 0)
                require(uspages >= 0)

                val minActivationAmount =
                    request[MIN_ACTIVATION_AMOUNT_PLANCK_PARAMETER].parseUnsignedLong()

                var creationLength = 4 // version + reserved
                creationLength += 8 // pages
                creationLength += 8 // minActivationAmount
                creationLength += if (cpages <= 1) 1 else if (cpages < 128) 2 else 4 // code size
                creationLength += code.size
                creationLength += if (dpages <= 1) 1 else if (dpages < 128) 2 else 4 // data size
                creationLength += data.size

                val creation = ByteBuffer.allocate(creationLength)
                creation.order(ByteOrder.LITTLE_ENDIAN)
                creation.putShort(dp.atConstants.atVersion(dp.blockchainService.height))
                creation.putShort(0.toShort())
                creation.putShort(cpages.toShort())
                creation.putShort(dpages.toShort())
                creation.putShort(cspages.toShort())
                creation.putShort(uspages.toShort())
                creation.putLong(minActivationAmount)
                putLength(cpages, code, creation)
                creation.put(code)
                putLength(dpages, data, creation)
                creation.put(data)

                creationBytes = creation.array()
            } catch (e: Exception) {
                val response = JsonObject()
                response.addProperty(ERROR_CODE_RESPONSE, 5)
                response.addProperty(ERROR_DESCRIPTION_RESPONSE, "Invalid or not specified parameters")
                return response
            }

        }

        if (creationBytes == null) {
            creationBytes = ParameterParser.getCreationBytes(request)
        }

        val account = dp.parameterService.getSenderAccount(request)
        val attachment = Attachment.AutomatedTransactionsCreation(
            dp,
            name,
            description!!,
            creationBytes!!,
            dp.blockchainService.height
        )

        logger.safeDebug { "AT $name added successfully" }
        return createTransaction(request, account, attachment)
    }

    private fun putLength(nPages: Int, data: ByteArray, buffer: ByteBuffer) {
        when {
            nPages <= 1 -> buffer.put((data.size).toByte())
            nPages < 128 -> buffer.putShort((data.size).toShort())
            else -> buffer.putInt(data.size)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CreateATProgram::class.java)
    }
}
