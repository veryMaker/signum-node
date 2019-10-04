package brs.http

import brs.Account
import brs.Attachment
import brs.Constants.FEE_QUANT
import brs.Constants.ONE_BURST
import brs.DependencyProvider
import brs.fluxcapacitor.FluxValues
import brs.http.common.Parameters.BROADCAST_PARAMETER
import brs.http.common.Parameters.DEADLINE_PARAMETER
import brs.http.common.Parameters.ENCRYPTED_MESSAGE_DATA_PARAMETER
import brs.http.common.Parameters.ENCRYPTED_MESSAGE_NONCE_PARAMETER
import brs.http.common.Parameters.ENCRYPT_TO_SELF_MESSAGE_DATA
import brs.http.common.Parameters.ENCRYPT_TO_SELF_MESSAGE_NONCE
import brs.http.common.Parameters.FEE_NQT_PARAMETER
import brs.http.common.Parameters.MESSAGE_IS_TEXT_PARAMETER
import brs.http.common.Parameters.MESSAGE_PARAMETER
import brs.http.common.Parameters.MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER
import brs.http.common.Parameters.MESSAGE_TO_ENCRYPT_PARAMETER
import brs.http.common.Parameters.MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER
import brs.http.common.Parameters.MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER
import brs.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.http.common.Parameters.RECIPIENT_PUBLIC_KEY_PARAMETER
import brs.http.common.Parameters.REFERENCED_TRANSACTION_FULL_HASH_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

internal abstract class CreateTransaction : APIServlet.JsonRequestHandler {
    private val dp: DependencyProvider

    constructor(dp: DependencyProvider, apiTags: Array<APITag>, replaceParameters: Boolean, vararg parameters: String) : super(apiTags, *if (replaceParameters) parameters else addCommonParameters(*parameters)) {
        this.dp = dp
    }

    constructor(dp: DependencyProvider, apiTags: Array<APITag>, vararg parameters: String) : super(apiTags, *addCommonParameters(*parameters)) {
        this.dp = dp
    }

    suspend fun createTransaction(request: HttpServletRequest, senderAccount: Account, attachment: Attachment): JsonElement {
        return createTransaction(request, senderAccount, null, 0, attachment)
    }

    suspend fun createTransaction(request: HttpServletRequest, senderAccount: Account, recipientId: Long?, amountNQT: Long, attachment: Attachment = Attachment.OrdinaryPayment(dp)): JsonElement {
        return dp.apiTransactionManager.createTransaction(request, senderAccount, recipientId, amountNQT, attachment, minimumFeeNQT())
    }

    override fun requirePost(): Boolean {
        return true
    }

    private fun minimumFeeNQT(): Long {
        return if (dp.fluxCapacitor.getValue(FluxValues.PRE_DYMAXION)) FEE_QUANT else ONE_BURST
    }

    companion object {
        private val commonParameters = arrayOf(SECRET_PHRASE_PARAMETER, PUBLIC_KEY_PARAMETER, FEE_NQT_PARAMETER, DEADLINE_PARAMETER, REFERENCED_TRANSACTION_FULL_HASH_PARAMETER, BROADCAST_PARAMETER, MESSAGE_PARAMETER, MESSAGE_IS_TEXT_PARAMETER, MESSAGE_TO_ENCRYPT_PARAMETER, MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, ENCRYPTED_MESSAGE_DATA_PARAMETER, ENCRYPTED_MESSAGE_NONCE_PARAMETER, MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER, MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER, ENCRYPT_TO_SELF_MESSAGE_DATA, ENCRYPT_TO_SELF_MESSAGE_NONCE, RECIPIENT_PUBLIC_KEY_PARAMETER)

        private fun addCommonParameters(vararg parameters: String): Array<String> {
            return commonParameters + parameters
        }
    }
}
