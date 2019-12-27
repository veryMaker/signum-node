package brs.api.http

import brs.common.AbstractUnitTest
import brs.transaction.appendix.Attachment
import com.google.gson.JsonPrimitive
import io.mockk.every

abstract class AbstractTransactionTest : AbstractUnitTest() {
    protected inline fun attachmentCreatedTransaction(r: () -> Any, apiTransactionManagerMock: APITransactionManager): Attachment? {
        val ac = mutableListOf<Attachment?>()

        every { apiTransactionManagerMock.createTransaction(any(), any(), any(), any(), captureNullable(ac), any()) } returns JsonPrimitive("hi")

        r()

        return if (ac.size > 0) ac[0] else null
    }
}
