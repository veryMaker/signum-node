package brs.http

import brs.Attachment
import brs.BurstException
import brs.Constants
import brs.DependencyProvider
import brs.http.JSONResponses.ALREADY_DELIVERED
import brs.http.JSONResponses.INCORRECT_ACCOUNT
import brs.http.JSONResponses.INCORRECT_DGS_DISCOUNT
import brs.http.JSONResponses.INCORRECT_DGS_GOODS
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.http.common.Parameters.DISCOUNT_NQT_PARAMETER
import brs.http.common.Parameters.GOODS_DATA_PARAMETER
import brs.http.common.Parameters.GOODS_IS_TEXT_PARAMETER
import brs.http.common.Parameters.GOODS_NONCE_PARAMETER
import brs.http.common.Parameters.GOODS_TO_ENCRYPT_PARAMETER
import brs.http.common.Parameters.PURCHASE_PARAMETER
import brs.http.common.Parameters.isFalse
import brs.util.Convert
import brs.util.parseHexString
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal class DGSDelivery internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), PURCHASE_PARAMETER, DISCOUNT_NQT_PARAMETER, GOODS_TO_ENCRYPT_PARAMETER, GOODS_IS_TEXT_PARAMETER, GOODS_DATA_PARAMETER, GOODS_NONCE_PARAMETER) {

    internal override fun processRequest(request: HttpServletRequest): JsonElement {

        val sellerAccount = dp.parameterService.getSenderAccount(request)
        val purchase = dp.parameterService.getPurchase(request)
        if (sellerAccount.id != purchase.sellerId) {
            return INCORRECT_PURCHASE
        }
        if (!purchase.isPending) {
            return ALREADY_DELIVERED
        }

        val discountValueNQT = Convert.emptyToNull(request.getParameter(DISCOUNT_NQT_PARAMETER))
        var discountNQT: Long = 0
        try {
            if (discountValueNQT != null) {
                discountNQT = java.lang.Long.parseLong(discountValueNQT)
            }
        } catch (e: RuntimeException) {
            return INCORRECT_DGS_DISCOUNT
        }

        if (discountNQT < 0
                || discountNQT > Constants.MAX_BALANCE_NQT
                || discountNQT > Convert.safeMultiply(purchase.priceNQT, purchase.quantity.toLong())) {
            return INCORRECT_DGS_DISCOUNT
        }

        val buyerAccount = dp.accountService.getAccount(purchase.buyerId) ?: return INCORRECT_ACCOUNT
        val goodsIsText = !isFalse(request.getParameter(GOODS_IS_TEXT_PARAMETER))
        var encryptedGoods = ParameterParser.getEncryptedGoods(request)

        if (encryptedGoods == null) {
            val secretPhrase = ParameterParser.getSecretPhrase(request)
            val goodsBytes: ByteArray?
            try {
                val plainGoods = Convert.nullToEmpty(request.getParameter(GOODS_TO_ENCRYPT_PARAMETER))
                if (plainGoods.isEmpty()) {
                    return INCORRECT_DGS_GOODS
                }
                goodsBytes = if (goodsIsText) Convert.toBytes(plainGoods) else plainGoods.parseHexString()
            } catch (e: RuntimeException) {
                return INCORRECT_DGS_GOODS
            }

            encryptedGoods = buyerAccount.encryptTo(goodsBytes, secretPhrase)
        }

        val attachment = Attachment.DigitalGoodsDelivery(dp, purchase.id, encryptedGoods!!, goodsIsText, discountNQT, dp.blockchain.height)
        return createTransaction(request, sellerAccount, buyerAccount.id, 0, attachment)

    }

}
