package brs.http

import brs.Account
import brs.Attachment
import com.google.gson.JsonElement
import javax.servlet.http.HttpServletRequest

interface APITransactionManager {
    fun createTransaction(request: HttpServletRequest, senderAccount: Account, recipientId: Long?, amountPlanck: Long, attachment: Attachment, minimumFeePlanck: Long): JsonElement
}
