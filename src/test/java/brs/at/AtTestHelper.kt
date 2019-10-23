package brs.at

import brs.Account
import brs.Blockchain
import brs.DependencyProvider
import brs.common.QuickMocker
import brs.common.TestConstants
import brs.db.BurstKey
import brs.db.VersionedBatchEntityTable
import brs.db.VersionedEntityTable
import brs.db.store.ATStore
import brs.db.store.AccountStore
import brs.props.PropertyService
import brs.props.Props
import brs.util.convert.parseHexString
import com.nhaarman.mockitokotlin2.*
import io.mockk.coEvery
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AtTestHelper {
    private val addedAts = mutableListOf<AT>()
    private var onAtAdded: ((AT) -> Unit)? = null

    internal suspend fun setupMocks(): DependencyProvider {
        val mockAtStore = mock<ATStore>()
        val mockFluxCapacitor = QuickMocker.latestValueFluxCapacitor()

        val atLongKeyFactory = mock<BurstKey.LongKeyFactory<AT>>()

        val atStateLongKeyFactory = mock<BurstKey.LongKeyFactory<AT.ATState>>()
        val mockBlockchain = mock<Blockchain>()
        val mockPropertyService = mock<PropertyService>()

        val mockAtTable = mock<VersionedEntityTable<AT>>()

        val mockAccountTable = mock<VersionedBatchEntityTable<Account>>()

        val mockAtStateTable = mock<VersionedEntityTable<AT.ATState>>()
        val mockAccountStore = mock<AccountStore>()

        val mockAccountKeyFactory = mock<BurstKey.LongKeyFactory<Account>>()
        val mockAccount = mock<Account>()

        doAnswer { invoke ->
            val at = invoke.getArgument<AT>(0)
            addedAts.add(at)
            if (onAtAdded != null) {
                onAtAdded!!(at)
            }
            null
        }.whenever(mockAtTable).insert(any())
        whenever(mockAccount.balanceNQT).doReturn(TestConstants.TEN_BURST)
        whenever(mockAccountStore.accountTable).doReturn(mockAccountTable)
        whenever(mockAccountStore.setOrVerify(any(), any(), any()))
                .doReturn(true)
        doAnswer {
            addedAts.map { it.id }
                    .map { AtApiHelper.getLong(it!!) }
                    .toList()
        }.whenever(mockAtStore).getOrderedATs()
        doAnswer { invoke ->
            val atId = invoke.getArgument<Long>(0)
            addedAts.forEach { addedAt ->
                if (AtApiHelper.getLong(addedAt.id ?: return@forEach) == atId) {
                    return@doAnswer addedAt
                }
            }
            null
        }.whenever(mockAtStore).getAT(any())
        whenever(mockAtTable.getAll(any(), any())).doReturn(addedAts)
        mockkObject(Account.Companion)
        coEvery { Account.getOrAddAccount(any(), any()) } returns mockAccount
        coEvery { Account.getAccount(any(), any()) } returns mockAccount
        whenever(mockAccountTable.get(any())).doReturn(mockAccount)
        whenever(mockAccountStore.accountKeyFactory).doReturn(mockAccountKeyFactory)
        whenever(mockAtStore.atStateTable).doReturn(mockAtStateTable)
        whenever(mockPropertyService.get(eq(Props.ENABLE_AT_DEBUG_LOG))).doReturn(true)
        whenever(mockAtStore.atTable).doReturn(mockAtTable)
        whenever(mockBlockchain.height).doReturn(Integer.MAX_VALUE)
        whenever(mockAtStore.atDbKeyFactory).doReturn(atLongKeyFactory)
        whenever(mockAtStore.atStateDbKeyFactory).doReturn(atStateLongKeyFactory)
        val dp = QuickMocker.dependencyProvider(mockAccountStore, mockAtStore, mockBlockchain, mockFluxCapacitor, mockPropertyService)
        dp.atConstants = AtConstants(dp)
        dp.atApiController = AtApiController(dp)
        dp.atApiPlatformImpl = AtApiPlatformImpl(dp)
        dp.atController = AtController(dp)
        return dp
    }

    internal suspend fun clearAddedAts(dp: DependencyProvider) {
        addedAts.clear()
        assertEquals(0, AT.getOrderedATs(dp).size.toLong())
    }

    internal fun setOnAtAdded(onAtAdded: (AT) -> Unit) {
        this.onAtAdded = onAtAdded
    }

    suspend fun addHelloWorldAT(dp: DependencyProvider) {
        AT.addAT(dp, 1L, TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, "HelloWorld", "Hello World AT", HELLO_WORLD_CREATION_BYTES, Integer.MAX_VALUE)
    }

    suspend fun addEchoAT(dp: DependencyProvider) {
        AT.addAT(dp, 2L, TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, "Echo", "Message Echo AT", ECHO_CREATION_BYTES, Integer.MAX_VALUE)
    }

    suspend fun addTipThanksAT(dp: DependencyProvider) {
        AT.addAT(dp, 3L, TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED, "TipThanks", "Tip Thanks AT", TIP_THANKS_CREATION_BYTES, Integer.MAX_VALUE)
    }

    companion object {
        // Hello World example compiled with BlockTalk v0.0.0
        internal val HELLO_WORLD_CREATION_BYTES = getCreationBytes(1, "3033040300000000350001010000001e0100000007283507030000000012270000001a0100000033100101000000320a03350401020000001002000000110200000033160102000000010200000048656c6c6f2c20573310010200000001020000006f726c6400000000331101020000000102000000000000000000000033120102000000010200000000000000000000003313010200000032050413".parseHexString())

        // Echo example compiled with BlockTalk v0.0.0
        internal val ECHO_CREATION_BYTES = getCreationBytes(1, "3033040300000000350001010000001e0100000007283507030000000012270000001a010000003310010100000032090335040102000000100200000035050102000000100200000035060102000000100200000035070102000000100200000033100101000000320a0335040102000000100200000011020000003316010200000011020000003313010200000011020000003312010200000011020000003311010200000011020000003310010200000032050413".parseHexString())

        // Tip Thanks example compiled with BlockTalk v0.0.0
        internal val TIP_THANKS_CREATION_BYTES = getCreationBytes(2, "12fb0000003033040301000000350001020000001e02000000072835070301000000122c0000001a0600000033100102000000320a0335040103000000100300000011030000003316010300000001030000005468616e6b20796f33100103000000010300000075210000000000003311010300000001030000000000000000000000331201030000000103000000000000000000000033130103000000320504350004030000001003000000010300000000e87648170000001003000000110400000011030000000703000000040000001003000000110300000003040000001f03000000040000000f1afa00000033160100000000320304130103000000d70faeecffc5c4e41003000000110000000013".parseHexString())

        private fun getCreationBytes(codePages: Int, code: ByteArray): ByteArray {
            val cpages = codePages.toShort()
            val dpages: Short = 1
            val cspages: Short = 1
            val uspages: Short = 1
            val minActivationAmount = TestConstants.TEN_BURST
            val data = ByteArray(0)
            var creationLength = 4 // version + reserved
            creationLength += 8 // pages
            creationLength += 8 // minActivationAmount
            creationLength += if (cpages * 256 <= 256) 1 else if (cpages * 256 <= 32767) 2 else 4 // code size
            creationLength += code.size
            creationLength += 1 // data size
            creationLength += data.size

            val creation = ByteBuffer.allocate(creationLength)
            creation.order(ByteOrder.LITTLE_ENDIAN)
            creation.putShort(currentAtVersion())
            creation.putShort(0.toShort())
            creation.putShort(cpages)
            creation.putShort(dpages)
            creation.putShort(cspages)
            creation.putShort(uspages)
            creation.putLong(minActivationAmount)
            putLength(cpages.toInt(), code.size, creation)
            creation.put(code)
            putLength(dpages.toInt(), data.size, creation)
            creation.put(data)
            return creation.array()
        }

        private fun currentAtVersion(): Short {
            return 2
        }

        private fun putLength(nPages: Int, length: Int, buffer: ByteBuffer) {
            when {
                nPages * 256 <= 256 -> buffer.put(length.toByte())
                nPages * 256 <= 32767 -> buffer.putShort(length.toShort())
                else -> buffer.putInt(length)
            }
        }
    }
}
