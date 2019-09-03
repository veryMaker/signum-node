package brs.http

import brs.*
import brs.crypto.EncryptedData
import brs.http.JSONResponses.ALREADY_DELIVERED
import brs.http.JSONResponses.INCORRECT_DGS_DISCOUNT
import brs.http.JSONResponses.INCORRECT_DGS_GOODS
import brs.http.JSONResponses.INCORRECT_PURCHASE
import brs.http.common.Parameters
import brs.services.AccountService
import brs.services.ParameterService
import brs.util.Convert
import com.google.gson.JsonElement

import javax.servlet.http.HttpServletRequest

internal class DGSDelivery internal constructor(private val dp: DependencyProvider) : CreateTransaction(dp, arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION), PURCHASE_PARAMETER, DISCOUNT_NQT_PARAMETER, GOODS_TO_ENCRYPT_PARAMETER, GOODS_IS_TEXT_PARAMETER, GOODS_DATA_PARAMETER, GOODS_NONCE_PARAMETER) {

    @Throws(BurstException::class)
    internal override fun processRequest(req: HttpServletRequest): JsonElement {

        val sellerAccount = dp.parameterService.getSenderAccount(req)
        val purchase = dp.parameterService.getPurchase(req)
        if (sellerAccount.id != purchase.sellerId) {
            return INCORRECT_PURCHASE
        }
        if (!purchase.isPending) {
            return ALREADY_DELIVERED
        }

        val discountValueNQT = Convert.emptyToNull(req.getParameter(DISCOUNT_NQT_PARAMETER))
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

        val buyerAccount = dp.accountService.getAccount(purchase.buyerId)
        val goodsIsText = !isFalse(req.getParameter(GOODS_IS_TEXT_PARAMETER))
        var encryptedGoods = ParameterParser.getEncryptedGoods(req)

        if (encryptedGoods == null) {
            val secretPhrase = ParameterParser.getSecretPhrase(req)
            val goodsBytes: ByteArray?
            try {
                val plainGoods = Convert.nullToEmpty(req.getParameter(GOODS_TO_ENCRYPT_PARAMETER))
                if (plainGoods.isEmpty()) {
                    return INCORRECT_DGS_GOODS
                }
                goodsBytes = if (goodsIsText) Convert.toBytes(plainGoods) else Convert.parseHexString(plainGoods)
            } catch (e: RuntimeException) {
                return INCORRECT_DGS_GOODS
            }

            encryptedGoods = buyerAccount.encryptTo(goodsBytes, secretPhrase)
        }

        val attachment = Attachment.DigitalGoodsDelivery(purchase.id, encryptedGoods!!, goodsIsText, discountNQT, dp.blockchain.height)
        return createTransaction(req, sellerAccount, buyerAccount.id, 0, attachment)

    }

}
