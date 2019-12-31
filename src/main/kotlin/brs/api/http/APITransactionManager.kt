package brs.api.http

import brs.entity.Account
import brs.transaction.appendix.Attachment
import brs.util.jetty.get
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

interface APITransactionManager {
    fun createTransaction(
        request: HttpServletRequest,
        senderAccount: Account,
        recipientId: Long?,
        amountPlanck: Long,
        attachment: Attachment,
        minimumFeePlanck: Long
    ): JsonElement
}
