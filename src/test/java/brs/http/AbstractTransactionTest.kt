package brs.http

import brs.Attachment
import brs.common.AbstractUnitTest
import com.google.gson.JsonPrimitive
import com.nhaarman.mockitokotlin2.*

abstract class AbstractTransactionTest : AbstractUnitTest() {
    protected suspend inline fun attachmentCreatedTransaction(r: () -> Any, apiTransactionManagerMock: APITransactionManager): Attachment? {
        val ac = argumentCaptor<Attachment>()

        whenever(apiTransactionManagerMock.createTransaction(any(), any(), anyOrNull(), any(), ac.capture(), any())).doReturn(JsonPrimitive("hi"))

        r()

        return ac.firstValue
    }
}
