package brs.http

import brs.Attachment
import brs.BurstException
import brs.common.AbstractUnitTest
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.nhaarman.mockitokotlin2.*

abstract class AbstractTransactionTest : AbstractUnitTest() {
    @Throws(BurstException::class)
    protected fun attachmentCreatedTransaction(r: () -> Any, apiTransactionManagerMock: APITransactionManager): Attachment? {
        val ac = argumentCaptor<Attachment>()

        whenever(apiTransactionManagerMock.createTransaction(any(), any(), anyOrNull(), any(), ac.capture(), any())).thenReturn(JsonPrimitive("hi"))

        r()

        return ac.firstValue
    }

}
