package brs.http

import brs.Constants
import brs.crypto.EncryptedData
import brs.http.JSONResponses.INCORRECT_AMOUNT
import brs.http.JSONResponses.INCORRECT_ASSET_QUANTITY
import brs.http.JSONResponses.INCORRECT_CREATION_BYTES
import brs.http.JSONResponses.INCORRECT_DGS_ENCRYPTED_GOODS
import brs.http.JSONResponses.INCORRECT_FEE
import brs.http.JSONResponses.INCORRECT_ORDER
import brs.http.JSONResponses.INCORRECT_PRICE
import brs.http.JSONResponses.INCORRECT_QUANTITY
import brs.http.JSONResponses.INCORRECT_RECIPIENT
import brs.http.JSONResponses.INCORRECT_TIMESTAMP
import brs.http.JSONResponses.MISSING_AMOUNT
import brs.http.JSONResponses.MISSING_FEE
import brs.http.JSONResponses.MISSING_ORDER
import brs.http.JSONResponses.MISSING_PRICE
import brs.http.JSONResponses.MISSING_QUANTITY
import brs.http.JSONResponses.MISSING_RECIPIENT
import brs.http.JSONResponses.MISSING_SECRET_PHRASE
import brs.http.common.Parameters
import brs.http.common.Parameters.AMOUNT_NQT_PARAMETER
import brs.http.common.Parameters.BUYER_PARAMETER
import brs.http.common.Parameters.CREATION_BYTES_PARAMETER
import brs.http.common.Parameters.FEE_NQT_PARAMETER
import brs.http.common.Parameters.FIRST_INDEX_PARAMETER
import brs.http.common.Parameters.GOODS_DATA_PARAMETER
import brs.http.common.Parameters.GOODS_NONCE_PARAMETER
import brs.http.common.Parameters.HEX_STRING_PARAMETER
import brs.http.common.Parameters.LAST_INDEX_PARAMETER
import brs.http.common.Parameters.ORDER_PARAMETER
import brs.http.common.Parameters.PRICE_NQT_PARAMETER
import brs.http.common.Parameters.QUANTITY_PARAMETER
import brs.http.common.Parameters.QUANTITY_QNT_PARAMETER
import brs.http.common.Parameters.RECIPIENT_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.http.common.Parameters.SELLER_PARAMETER
import brs.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.util.convert.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.servlet.http.HttpServletRequest

internal object ParameterParser {

    fun getFeeNQT(request: HttpServletRequest): Long {
        val feeValueNQT = request.getParameter(FEE_NQT_PARAMETER).emptyToNull()
                ?: throw ParameterException(MISSING_FEE)
        val feeNQT: Long
        try {
            feeNQT = java.lang.Long.parseLong(feeValueNQT)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_FEE)
        }

        if (feeNQT < 0 || feeNQT >= Constants.MAX_BALANCE_NQT) {
            throw ParameterException(INCORRECT_FEE)
        }
        return feeNQT
    }

    fun getPriceNQT(request: HttpServletRequest): Long {
        val priceValueNQT = request.getParameter(PRICE_NQT_PARAMETER).emptyToNull()
                ?: throw ParameterException(MISSING_PRICE)
        val priceNQT: Long
        try {
            priceNQT = java.lang.Long.parseLong(priceValueNQT)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_PRICE)
        }

        if (priceNQT <= 0 || priceNQT > Constants.MAX_BALANCE_NQT) {
            throw ParameterException(INCORRECT_PRICE)
        }
        return priceNQT
    }

    fun getQuantityQNT(request: HttpServletRequest): Long {
        val quantityValueQNT = request.getParameter(QUANTITY_QNT_PARAMETER).emptyToNull()
                ?: throw ParameterException(MISSING_QUANTITY)
        val quantityQNT: Long
        try {
            quantityQNT = java.lang.Long.parseLong(quantityValueQNT)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_QUANTITY)
        }

        if (quantityQNT <= 0 || quantityQNT > Constants.MAX_ASSET_QUANTITY_QNT) {
            throw ParameterException(INCORRECT_ASSET_QUANTITY)
        }
        return quantityQNT
    }

    fun getOrderId(request: HttpServletRequest): Long {
        val orderValue = request.getParameter(ORDER_PARAMETER).emptyToNull()
                ?: throw ParameterException(MISSING_ORDER)
        try {
            return orderValue.parseUnsignedLong()
        } catch (e: RuntimeException) {
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
            } catch (e: RuntimeException) {
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
        } catch (e: RuntimeException) {
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
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_RECIPIENT)
        }

    }

    fun getBuyerId(request: HttpServletRequest): Long {
        val buyerIdValue = request.getParameter(BUYER_PARAMETER).emptyToNull()
        try {
            return buyerIdValue!!.parseAccountId()
        } catch (e: RuntimeException) {
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
        val lastIndex: Int
        try {
            lastIndex = Integer.parseInt(request.getParameter(LAST_INDEX_PARAMETER))
            if (lastIndex < 0) {
                return Integer.MAX_VALUE
            }
        } catch (e: NumberFormatException) {
            return Integer.MAX_VALUE
        }

        return lastIndex
    }

    fun getCreationBytes(request: HttpServletRequest): ByteArray? {
        try {
            return request.getParameter(CREATION_BYTES_PARAMETER).parseHexString()
        } catch (e: RuntimeException) {
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

    fun getAmountNQT(request: HttpServletRequest): Long {
        val amountValueNQT = request.getParameter(AMOUNT_NQT_PARAMETER).emptyToNull()
                ?: throw ParameterException(MISSING_AMOUNT)
        val amountNQT: Long
        try {
            amountNQT = java.lang.Long.parseLong(amountValueNQT)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_AMOUNT)
        }

        if (amountNQT <= 0 || amountNQT >= Constants.MAX_BALANCE_NQT) {
            throw ParameterException(INCORRECT_AMOUNT)
        }
        return amountNQT
    }
}
