package brs.api.http

import brs.api.http.JSONResponses.ALREADY_DELIVERED
import brs.api.http.JSONResponses.INCORRECT_ACCOUNT
import brs.api.http.JSONResponses.INCORRECT_DGS_DISCOUNT
import brs.api.http.JSONResponses.INCORRECT_DGS_GOODS
import brs.api.http.JSONResponses.INCORRECT_PURCHASE
import brs.api.http.common.Parameters.DISCOUNT_PLANCK_PARAMETER
import brs.api.http.common.Parameters.GOODS_DATA_PARAMETER
import brs.api.http.common.Parameters.GOODS_IS_TEXT_PARAMETER
import brs.api.http.common.Parameters.GOODS_NONCE_PARAMETER
import brs.api.http.common.Parameters.GOODS_TO_ENCRYPT_PARAMETER
import brs.api.http.common.Parameters.PURCHASE_PARAMETER
import brs.api.http.common.Parameters.isFalse
import brs.entity.DependencyProvider
import brs.objects.Constants
import brs.transaction.appendix.Attachment
import brs.util.convert.emptyToNull
import brs.util.convert.parseHexString
import brs.util.convert.safeMultiply
import brs.util.convert.toBytes
import brs.util.jetty.get
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class DGSDelivery internal constructor(private val dp: DependencyProvider) : CreateTransaction(
    dp,
    arrayOf(APITag.DGS, APITag.CREATE_TRANSACTION),
    PURCHASE_PARAMETER,
    DISCOUNT_PLANCK_PARAMETER,
    GOODS_TO_ENCRYPT_PARAMETER,
    GOODS_IS_TEXT_PARAMETER,
    GOODS_DATA_PARAMETER,
    GOODS_NONCE_PARAMETER
) {

    override fun processRequest(request: HttpServletRequest): JsonElement {

        val sellerAccount = dp.parameterService.getSenderAccount(request)
        val purchase = dp.parameterService.getPurchase(request)
        if (sellerAccount.id != purchase.sellerId) {
            return INCORRECT_PURCHASE
        }
        if (!purchase.isPending) {
            return ALREADY_DELIVERED
        }

        val discountValuePlanck = request[DISCOUNT_PLANCK_PARAMETER].emptyToNull()
        var discountPlanck: Long = 0
        try {
            if (discountValuePlanck != null) {
                discountPlanck = discountValuePlanck.toLong()
            }
        } catch (e: Exception) {
            return INCORRECT_DGS_DISCOUNT
        }

        if (discountPlanck < 0
            || discountPlanck > Constants.MAX_BALANCE_PLANCK
            || discountPlanck > purchase.pricePlanck.safeMultiply(purchase.quantity.toLong())
        ) {
            return INCORRECT_DGS_DISCOUNT
        }

        val buyerAccount = dp.accountService.getAccount(purchase.buyerId) ?: return INCORRECT_ACCOUNT
        val goodsIsText = !isFalse(request[GOODS_IS_TEXT_PARAMETER])
        var encryptedGoods = ParameterParser.getEncryptedGoods(request, goodsIsText)

        if (encryptedGoods == null) {
            val secretPhrase = ParameterParser.getSecretPhrase(request)
            val goodsBytes: ByteArray?
            try {
                val plainGoods = request[GOODS_TO_ENCRYPT_PARAMETER].orEmpty()
                if (plainGoods.isEmpty()) {
                    return INCORRECT_DGS_GOODS
                }
                goodsBytes = if (goodsIsText) plainGoods.toBytes() else plainGoods.parseHexString()
            } catch (e: Exception) {
                return INCORRECT_DGS_GOODS
            }

            encryptedGoods = buyerAccount.encryptTo(goodsBytes, secretPhrase, goodsIsText)
        }

        val attachment = Attachment.DigitalGoodsDelivery(
            dp,
            purchase.id,
            encryptedGoods,
            discountPlanck,
            dp.blockchainService.height
        )
        return createTransaction(request, sellerAccount, buyerAccount.id, 0, attachment)

    }

}
