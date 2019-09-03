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
import brs.util.Convert

import javax.servlet.http.HttpServletRequest
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal object ParameterParser {

    @Throws(ParameterException::class)
    fun getFeeNQT(req: HttpServletRequest): Long {
        val feeValueNQT = Convert.emptyToNull(req.getParameter(FEE_NQT_PARAMETER))
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

    @Throws(ParameterException::class)
    fun getPriceNQT(req: HttpServletRequest): Long {
        val priceValueNQT = Convert.emptyToNull(req.getParameter(PRICE_NQT_PARAMETER))
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

    @Throws(ParameterException::class)
    fun getQuantityQNT(req: HttpServletRequest): Long {
        val quantityValueQNT = Convert.emptyToNull(req.getParameter(QUANTITY_QNT_PARAMETER))
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

    @Throws(ParameterException::class)
    fun getOrderId(req: HttpServletRequest): Long {
        val orderValue = Convert.emptyToNull(req.getParameter(ORDER_PARAMETER))
                ?: throw ParameterException(MISSING_ORDER)
        try {
            return Convert.parseUnsignedLong(orderValue)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_ORDER)
        }

    }

    @Throws(ParameterException::class)
    fun getGoodsQuantity(req: HttpServletRequest): Int {
        val quantityString = Convert.emptyToNull(req.getParameter(QUANTITY_PARAMETER))
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

    @Throws(ParameterException::class)
    fun getEncryptedGoods(req: HttpServletRequest): EncryptedData? {
        val data = Convert.emptyToNull(req.getParameter(GOODS_DATA_PARAMETER))
        val nonce = Convert.emptyToNull(req.getParameter(GOODS_NONCE_PARAMETER))
        if (data != null && nonce != null) {
            try {
                return EncryptedData(Convert.parseHexString(data), Convert.parseHexString(nonce))
            } catch (e: RuntimeException) {
                throw ParameterException(INCORRECT_DGS_ENCRYPTED_GOODS)
            }

        }
        return null
    }

    @Throws(ParameterException::class)
    fun getSecretPhrase(req: HttpServletRequest): String {
        return Convert.emptyToNull(req.getParameter(SECRET_PHRASE_PARAMETER))
                ?: throw ParameterException(MISSING_SECRET_PHRASE)
    }

    @Throws(ParameterException::class)
    fun getTimestamp(req: HttpServletRequest): Int {
        val timestampValue = Convert.emptyToNull(req.getParameter(TIMESTAMP_PARAMETER)) ?: return 0
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

    @Throws(ParameterException::class)
    fun getRecipientId(req: HttpServletRequest): Long {
        val recipientValue = Convert.emptyToNull(req.getParameter(RECIPIENT_PARAMETER))
        if (recipientValue == null || Parameters.isZero(recipientValue)) {
            throw ParameterException(MISSING_RECIPIENT)
        }
        val recipientId: Long
        try {
            recipientId = Convert.parseAccountId(recipientValue)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_RECIPIENT)
        }

        if (recipientId == 0L) {
            throw ParameterException(INCORRECT_RECIPIENT)
        }
        return recipientId
    }

    @Throws(ParameterException::class)
    fun getSellerId(req: HttpServletRequest): Long {
        val sellerIdValue = Convert.emptyToNull(req.getParameter(SELLER_PARAMETER))
        try {
            return Convert.parseAccountId(sellerIdValue!!)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_RECIPIENT)
        }

    }

    @Throws(ParameterException::class)
    fun getBuyerId(req: HttpServletRequest): Long {
        val buyerIdValue = Convert.emptyToNull(req.getParameter(BUYER_PARAMETER))
        try {
            return Convert.parseAccountId(buyerIdValue!!)
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_RECIPIENT)
        }

    }

    fun getFirstIndex(req: HttpServletRequest): Int {
        val firstIndex: Int
        try {
            firstIndex = Integer.parseInt(req.getParameter(FIRST_INDEX_PARAMETER))
            if (firstIndex < 0) {
                return 0
            }
        } catch (e: NumberFormatException) {
            return 0
        }

        return firstIndex
    }

    fun getLastIndex(req: HttpServletRequest): Int {
        val lastIndex: Int
        try {
            lastIndex = Integer.parseInt(req.getParameter(LAST_INDEX_PARAMETER))
            if (lastIndex < 0) {
                return Integer.MAX_VALUE
            }
        } catch (e: NumberFormatException) {
            return Integer.MAX_VALUE
        }

        return lastIndex
    }

    @Throws(ParameterException::class)
    fun getCreationBytes(req: HttpServletRequest): ByteArray? {
        try {
            return Convert.parseHexString(req.getParameter(CREATION_BYTES_PARAMETER))
        } catch (e: RuntimeException) {
            throw ParameterException(INCORRECT_CREATION_BYTES)
        }


    }

    fun getATLong(req: HttpServletRequest): String {
        val hex = req.getParameter(HEX_STRING_PARAMETER)
        val bf = ByteBuffer.allocate(8)
        bf.order(ByteOrder.LITTLE_ENDIAN)
        bf.put(Convert.parseHexString(hex)!!)

        return Convert.toUnsignedLong(bf.getLong(0))
    }

    @Throws(ParameterException::class)
    fun getAmountNQT(req: HttpServletRequest): Long {
        val amountValueNQT = Convert.emptyToNull(req.getParameter(AMOUNT_NQT_PARAMETER))
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
}// never
