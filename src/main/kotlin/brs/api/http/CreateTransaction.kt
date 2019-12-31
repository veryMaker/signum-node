package brs.api.http

import brs.api.http.common.Parameters.BROADCAST_PARAMETER
import brs.api.http.common.Parameters.DEADLINE_PARAMETER
import brs.api.http.common.Parameters.ENCRYPTED_MESSAGE_DATA_PARAMETER
import brs.api.http.common.Parameters.ENCRYPTED_MESSAGE_NONCE_PARAMETER
import brs.api.http.common.Parameters.ENCRYPT_TO_SELF_MESSAGE_DATA
import brs.api.http.common.Parameters.ENCRYPT_TO_SELF_MESSAGE_NONCE
import brs.api.http.common.Parameters.FEE_PLANCK_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_IS_TEXT_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_TO_ENCRYPT_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER
import brs.api.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.api.http.common.Parameters.RECIPIENT_PUBLIC_KEY_PARAMETER
import brs.api.http.common.Parameters.REFERENCED_TRANSACTION_FULL_HASH_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.objects.Constants.FEE_QUANT
import brs.objects.Constants.ONE_BURST
import brs.objects.FluxValues
import brs.transaction.appendix.Attachment
import brs.util.jetty.get
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal abstract class CreateTransaction : APIServlet.JsonRequestHandler {
    private val dp: DependencyProvider

    constructor(
        dp: DependencyProvider,
        apiTags: Array<APITag>,
        replaceParameters: Boolean,
        vararg parameters: String
    ) : super(apiTags, *if (replaceParameters) parameters else addCommonParameters(*parameters)) {
        this.dp = dp
    }

    constructor(dp: DependencyProvider, apiTags: Array<APITag>, vararg parameters: String) : super(
        apiTags,
        *addCommonParameters(*parameters)
    ) {
        this.dp = dp
    }

    fun createTransaction(request: HttpServletRequest, senderAccount: Account, attachment: Attachment): JsonElement {
        return createTransaction(request, senderAccount, null, 0, attachment)
    }

    fun createTransaction(
        request: HttpServletRequest,
        senderAccount: Account,
        recipientId: Long?,
        amountPlanck: Long,
        attachment: Attachment = Attachment.OrdinaryPayment(dp)
    ): JsonElement {
        return dp.apiTransactionManager.createTransaction(
            request,
            senderAccount,
            recipientId,
            amountPlanck,
            attachment,
            minimumFeePlanck()
        )
    }

    override fun requirePost(): Boolean {
        return true
    }

    private fun minimumFeePlanck(): Long {
        return if (dp.fluxCapacitorService.getValue(FluxValues.PRE_DYMAXION)) FEE_QUANT else ONE_BURST
    }

    companion object {
        private val commonParameters = arrayOf(
            SECRET_PHRASE_PARAMETER,
            PUBLIC_KEY_PARAMETER,
            FEE_PLANCK_PARAMETER,
            DEADLINE_PARAMETER,
            REFERENCED_TRANSACTION_FULL_HASH_PARAMETER,
            BROADCAST_PARAMETER,
            MESSAGE_PARAMETER,
            MESSAGE_IS_TEXT_PARAMETER,
            MESSAGE_TO_ENCRYPT_PARAMETER,
            MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER,
            ENCRYPTED_MESSAGE_DATA_PARAMETER,
            ENCRYPTED_MESSAGE_NONCE_PARAMETER,
            MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER,
            MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER,
            ENCRYPT_TO_SELF_MESSAGE_DATA,
            ENCRYPT_TO_SELF_MESSAGE_NONCE,
            RECIPIENT_PUBLIC_KEY_PARAMETER
        )

        private fun addCommonParameters(vararg parameters: String): Array<String> {
            return commonParameters + parameters
        }
    }
}
