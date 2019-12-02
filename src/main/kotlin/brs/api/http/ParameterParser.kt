package brs.api.http

import brs.api.http.JSONResponses.INCORRECT_AMOUNT
import brs.api.http.JSONResponses.INCORRECT_ASSET_QUANTITY
import brs.api.http.JSONResponses.INCORRECT_CREATION_BYTES
import brs.api.http.JSONResponses.INCORRECT_DGS_ENCRYPTED_GOODS
import brs.api.http.JSONResponses.INCORRECT_FEE
import brs.api.http.JSONResponses.INCORRECT_ORDER
import brs.api.http.JSONResponses.INCORRECT_PRICE
import brs.api.http.JSONResponses.INCORRECT_QUANTITY
import brs.api.http.JSONResponses.INCORRECT_RECIPIENT
import brs.api.http.JSONResponses.INCORRECT_TIMESTAMP
import brs.api.http.JSONResponses.MISSING_AMOUNT
import brs.api.http.JSONResponses.MISSING_FEE
import brs.api.http.JSONResponses.MISSING_ORDER
import brs.api.http.JSONResponses.MISSING_PRICE
import brs.api.http.JSONResponses.MISSING_QUANTITY
import brs.api.http.JSONResponses.MISSING_RECIPIENT
import brs.api.http.JSONResponses.MISSING_SECRET_PHRASE
import brs.api.http.common.Parameters
import brs.api.http.common.Parameters.AMOUNT_PLANCK_PARAMETER
import brs.api.http.common.Parameters.BUYER_PARAMETER
import brs.api.http.common.Parameters.CREATION_BYTES_PARAMETER
import brs.api.http.common.Parameters.FEE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.api.http.common.Parameters.GOODS_DATA_PARAMETER
import brs.api.http.common.Parameters.GOODS_NONCE_PARAMETER
import brs.api.http.common.Parameters.HEX_STRING_PARAMETER
import brs.api.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.api.http.common.Parameters.ORDER_PARAMETER
import brs.api.http.common.Parameters.PRICE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_PARAMETER
import brs.api.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.api.http.common.Parameters.RECIPIENT_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.api.http.common.Parameters.SELLER_PARAMETER
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.entity.EncryptedData
import brs.objects.Constants
import brs.util.convert.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.servlet.http.HttpServletRequest

internal object ParameterParser {

    fun getFeePlanck(request: HttpServletRequest): Long {
        val feeValuePlanck = request.getParameter(FEE_PLANCK_PARAMETER).emptyToNull()
            ?: throw ParameterException(MISSING_FEE)
        val feePlanck: Long
        try {
            feePlanck = feeValuePlanck.toLong()
        } catch (e: Exception) {
            throw ParameterException(INCORRECT_FEE)
        }

        if (feePlanck < 0 || feePlanck >= Constants.MAX_BALANCE_PLANCK) {
            throw ParameterException(INCORRECT_FEE)
        }
        return feePlanck
    }

    fun getPricePlanck(request: HttpServletRequest): Long {
        val priceValuePlanck = request.getParameter(PRICE_PLANCK_PARAMETER).emptyToNull()
            ?: throw ParameterException(MISSING_PRICE)
        val pricePlanck: Long
        try {
            pricePlanck = priceValuePlanck.toLong()
        } catch (e: Exception) {
            throw ParameterException(INCORRECT_PRICE)
        }

        if (pricePlanck <= 0 || pricePlanck > Constants.MAX_BALANCE_PLANCK) {
            throw ParameterException(INCORRECT_PRICE)
        }
        return pricePlanck
    }

    fun getQuantity(request: HttpServletRequest): Long {
        val quantityValue = request.getParameter(QUANTITY_QNT_PARAMETER).emptyToNull()
            ?: throw ParameterException(MISSING_QUANTITY)
        val quantity: Long
        try {
            quantity = quantityValue.toLong()
        } catch (e: Exception) {
            throw ParameterException(INCORRECT_QUANTITY)
        }

        if (quantity <= 0 || quantity > Constants.MAX_ASSET_QUANTITY) {
            throw ParameterException(INCORRECT_ASSET_QUANTITY)
        }
        return quantity
    }

    fun getOrderId(request: HttpServletRequest): Long {
        val orderValue = request.getParameter(ORDER_PARAMETER).emptyToNull()
            ?: throw ParameterException(MISSING_ORDER)
        try {
            return orderValue.parseUnsignedLong()
        } catch (e: Exception) {
            throw ParameterException(INCORRECT_ORDER)
        }

    }

    fun getGoodsQuantity(request: HttpServletRequest): Int {
        val quantityString = request.getParameter(QUANTITY_PARAMETER).emptyToNull()
        try {
            val quantity = Integer.parseInt(quantityString!!)
            if (quantity < 0 || quantity > Constants.MAX_DGS_LISTING_QUANTITY) {
                throw ParameterException(INCORRECT_QUANTITY)
            }
            return quantity
        } catch (e: NumberFormatException) {
            throw ParameterException(INCORRECT_QUANTITY)
        }
    }

    fun getEncryptedGoods(request: HttpServletRequest): EncryptedData? {
        val data = request.getParameter(GOODS_DATA_PARAMETER).emptyToNull()
        val nonce = request.getParameter(GOODS_NONCE_PARAMETER).emptyToNull()
        if (data != null && nonce != null) {
            try {
                return EncryptedData(data.parseHexString(), nonce.parseHexString())
            } catch (e: Exception) {
                throw ParameterException(INCORRECT_DGS_ENCRYPTED_GOODS)
            }

        }
        return null
    }

    fun getSecretPhrase(request: HttpServletRequest): String {
        return request.getParameter(SECRET_PHRASE_PARAMETER).emptyToNull()
            ?: throw ParameterException(MISSING_SECRET_PHRASE)
    }

    fun getTimestamp(request: HttpServletRequest): Int {
        val timestampValue = request.getParameter(TIMESTAMP_PARAMETER).emptyToNull() ?: return 0
        val timestamp: Int
        try {
            timestamp = Integer.parseInt(timestampValue)
        } catch (e: NumberFormatException) {
            throw ParameterException(INCORRECT_TIMESTAMP)
        }

        if (timestamp < 0) {
            throw ParameterException(INCORRECT_TIMESTAMP)
        }
        return timestamp
    }

    fun getRecipientId(request: HttpServletRequest): Long {
        val recipientValue = request.getParameter(RECIPIENT_PARAMETER).emptyToNull()
        if (recipientValue == null || Parameters.isZero(recipientValue)) {
            throw ParameterException(MISSING_RECIPIENT)
        }
        val recipientId: Long
        try {
            recipientId = recipientValue.parseAccountId()
        } catch (e: Exception) {
            throw ParameterException(INCORRECT_RECIPIENT)
        }

        if (recipientId == 0L) {
            throw ParameterException(INCORRECT_RECIPIENT)
        }
        return recipientId
    }

    fun getSellerId(request: HttpServletRequest): Long {
        val sellerIdValue = request.getParameter(SELLER_PARAMETER).emptyToNull()
        try {
            return sellerIdValue!!.parseAccountId()
        } catch (e: Exception) {
            throw ParameterException(INCORRECT_RECIPIENT)
        }

    }

    fun getBuyerId(request: HttpServletRequest): Long {
        val buyerIdValue = request.getParameter(BUYER_PARAMETER).emptyToNull()
        try {
            return buyerIdValue!!.parseAccountId()
        } catch (e: Exception) {
            throw ParameterException(INCORRECT_RECIPIENT)
        }

    }

    fun getFirstIndex(request: HttpServletRequest): Int {
        val firstIndex: Int
        try {
            firstIndex = Integer.parseInt(request.getParameter(FIRST_INDEX_PARAMETER))
            if (firstIndex < 0) {
                return 0
            }
        } catch (e: NumberFormatException) {
            return 0
        }

        return firstIndex
    }

    fun getLastIndex(request: HttpServletRequest): Int {
        val firstIndex = getFirstIndex(request)
        var lastIndex: Int
        try {
            lastIndex = Integer.parseInt(request.getParameter(LAST_INDEX_PARAMETER))
            if (lastIndex < 0) {
                lastIndex = firstIndex + Constants.MAX_API_RETURNED_ITEMS
            }
        } catch (e: NumberFormatException) {
            lastIndex = firstIndex + Constants.MAX_API_RETURNED_ITEMS
        }

        require(firstIndex <= lastIndex) { "lastIndex must be greater than or equal to firstIndex" }
        if (lastIndex - firstIndex >= Constants.MAX_API_RETURNED_ITEMS) {
            // Don't reject the request, just limit it.
            lastIndex = firstIndex + Constants.MAX_API_RETURNED_ITEMS - 1
        }

        return lastIndex
    }

    fun getCreationBytes(request: HttpServletRequest): ByteArray? {
        try {
            return request.getParameter(CREATION_BYTES_PARAMETER).parseHexString()
        } catch (e: Exception) {
            throw ParameterException(INCORRECT_CREATION_BYTES)
        }


    }

    fun getATLong(request: HttpServletRequest): String {
        val hex = request.getParameter(HEX_STRING_PARAMETER)
        val bf = ByteBuffer.allocate(8)
        bf.order(ByteOrder.LITTLE_ENDIAN)
        bf.put(hex.parseHexString())
        return bf.getLong(0).toUnsignedString()
    }

    fun getAmountPlanck(request: HttpServletRequest): Long {
        val amountValuePlanck = request.getParameter(AMOUNT_PLANCK_PARAMETER).emptyToNull()
            ?: throw ParameterException(MISSING_AMOUNT)
        val amountPlanck: Long
        try {
            amountPlanck = amountValuePlanck.toLong()
        } catch (e: Exception) {
            throw ParameterException(INCORRECT_AMOUNT)
        }

        if (amountPlanck <= 0 || amountPlanck >= Constants.MAX_BALANCE_PLANCK) {
            throw ParameterException(INCORRECT_AMOUNT)
        }
        return amountPlanck
    }
}
