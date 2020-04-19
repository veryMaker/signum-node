package brs.at

import brs.common.QuickMocker
import brs.common.TestConstants
import brs.db.ATStore
import brs.db.AccountStore
import brs.db.BurstKey
import brs.db.MutableBatchEntityTable
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.objects.Constants.EMPTY_BYTE_ARRAY
import brs.objects.Props
import brs.services.AccountService
import brs.services.BlockchainService
import brs.services.PropertyService
import brs.util.convert.parseHexString
import io.mockk.every
import io.mockk.mockk
import org.jooq.SortField
import org.junit.Assert.assertEquals
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AtTestHelper {
    private val addedAts = mutableListOf<AT>()
    private var onAtAdded: ((AT) -> Unit)? = null

    internal fun setupMocks(): DependencyProvider {
        val mockAtStore = mockk<ATStore>(relaxed = true)
        val mockFluxCapacitor = QuickMocker.latestValueFluxCapacitor()

        val atLongKeyFactory = mockk<BurstKey.LongKeyFactory<AT>>()
        every { atLongKeyFactory.newKey(any<Long>()) } returns mockk()

        val atStateLongKeyFactory = mockk<BurstKey.LongKeyFactory<AT.ATState>>()
        every { atStateLongKeyFactory.newKey(any<Long>()) } returns mockk()
        val mockBlockchain = mockk<BlockchainService>(relaxed = true)
        val mockPropertyService = mockk<PropertyService>(relaxed = true)

        val mockAtTable = mockk<MutableBatchEntityTable<AT>>(relaxUnitFun = true)
        every { mockAtTable[any()] } returns null

        val mockAccountTable = mockk<MutableBatchEntityTable<Account>>(relaxUnitFun = true)

        val mockAtStateTable = mockk<MutableBatchEntityTable<AT.ATState>>(relaxUnitFun = true)
        every { mockAtStateTable[any()] } returns null
        val mockAccountStore = mockk<AccountStore>(relaxed = true)
        val mockAccountService = mockk<AccountService>(relaxed = true)

        val mockAccountKeyFactory = mockk<BurstKey.LongKeyFactory<Account>>()
        every { mockAccountKeyFactory.newKey(any<Long>()) } returns mockk()
        val mockAccount = mockk<Account>(relaxed = true)

        every { mockAtTable.insert(any()) } answers {
            val at = args[0] as AT
            addedAts.add(at)
            if (onAtAdded != null) {
                onAtAdded!!(at)
            }
        }
        every { mockAccount.balancePlanck } returns TestConstants.TEN_BURST
        every { mockAccountStore.accountTable } returns mockAccountTable
        every { mockAccountStore.setOrVerify(any(), any(), any()) } returns true
        every { mockAtStore.getOrderedATs() } answers { addedAts.map { it.id } }
        every { mockAtStore.getAT(any()) } answers {
            val atId = args[0] as Long
            addedAts.forEach { addedAt ->
                if (addedAt.id == atId) {
                    return@answers addedAt
                }
            }
            return@answers null
        }
        every { mockAtTable.getAll(any(), any(), any<Collection<SortField<*>>>()) } returns addedAts
        every { mockAccountService.getOrAddAccount(any()) } returns mockAccount
        every { mockAccountService.getAccount(any<Long>()) } returns mockAccount
        every { mockAccountTable[any()] } returns mockAccount
        every { mockAccountStore.accountKeyFactory } returns mockAccountKeyFactory
        every { mockAtStore.atStateTable } returns mockAtStateTable
        every { mockPropertyService.get(eq(Props.ENABLE_AT_DEBUG_LOG)) } returns true
        every { mockAtStore.atTable } returns mockAtTable
        every { mockBlockchain.height } returns Integer.MAX_VALUE
        every { mockAtStore.atDbKeyFactory } returns atLongKeyFactory
        every { mockAtStore.atStateDbKeyFactory } returns atStateLongKeyFactory
        every { atStateLongKeyFactory.newKey(any<Long>()) } returns mockk(relaxed = true)
        val dp = QuickMocker.dependencyProvider(
            QuickMocker.mockDb(mockAccountStore, mockAtStore),
            mockAccountService,
            mockBlockchain,
            mockFluxCapacitor,
            mockPropertyService
        )
        dp.atConstants = AtConstants(dp)
        dp.atApiController = AtApiController(dp)
        dp.atApi = AtApiPlatformImpl(dp)
        dp.atController = AtController(dp)
        return dp
    }

    internal fun clearAddedAts(dp: DependencyProvider) {
        addedAts.clear()
        assertEquals(0, dp.db.atStore.getOrderedATs().size.toLong())
    }

    internal fun setOnAtAdded(onAtAdded: (AT) -> Unit) {
        this.onAtAdded = onAtAdded
    }

    fun addHelloWorldAT(dp: DependencyProvider): AT {
        AT.addAT(
            dp,
            1L,
            TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED,
            "HelloWorld",
            "Hello World AT",
            HELLO_WORLD_CREATION_BYTES,
            Integer.MAX_VALUE
        )
        return dp.db.atStore.getAT(1L)!!
    }

    fun addEchoAT(dp: DependencyProvider): AT {
        AT.addAT(
            dp,
            2L,
            TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED,
            "Echo",
            "Message Echo AT",
            ECHO_CREATION_BYTES,
            Integer.MAX_VALUE
        )
        return dp.db.atStore.getAT(2L)!!
    }

    fun addTipThanksAT(dp: DependencyProvider): AT {
        AT.addAT(
            dp,
            3L,
            TestConstants.TEST_ACCOUNT_NUMERIC_ID_PARSED,
            "TipThanks",
            "Tip Thanks AT",
            TIP_THANKS_CREATION_BYTES,
            Integer.MAX_VALUE
        )
        return dp.db.atStore.getAT(3L)!!
    }

    companion object {
        // Hello World example compiled with BlockTalk v0.0.0
        internal val HELLO_WORLD_CREATION_BYTES = getCreationBytes(
            1,
            "3033040300000000350001010000001e0100000007283507030000000012270000001a0100000033100101000000320a03350401020000001002000000110200000033160102000000010200000048656c6c6f2c20573310010200000001020000006f726c6400000000331101020000000102000000000000000000000033120102000000010200000000000000000000003313010200000032050413".parseHexString()
        )

        // Echo example compiled with BlockTalk v0.0.0
        internal val ECHO_CREATION_BYTES = getCreationBytes(
            1,
            "3033040300000000350001010000001e0100000007283507030000000012270000001a010000003310010100000032090335040102000000100200000035050102000000100200000035060102000000100200000035070102000000100200000033100101000000320a0335040102000000100200000011020000003316010200000011020000003313010200000011020000003312010200000011020000003311010200000011020000003310010200000032050413".parseHexString()
        )

        // Tip Thanks example compiled with BlockTalk v0.0.0
        internal val TIP_THANKS_CREATION_BYTES = getCreationBytes(
            2,
            "12fb0000003033040301000000350001020000001e02000000072835070301000000122c0000001a0600000033100102000000320a0335040103000000100300000011030000003316010300000001030000005468616e6b20796f33100103000000010300000075210000000000003311010300000001030000000000000000000000331201030000000103000000000000000000000033130103000000320504350004030000001003000000010300000000e87648170000001003000000110400000011030000000703000000040000001003000000110300000003040000001f03000000040000000f1afa00000033160100000000320304130103000000d70faeecffc5c4e41003000000110000000013".parseHexString()
        )

        private fun getCreationBytes(codePages: Int, code: ByteArray): ByteArray {
            val cpages = codePages.toShort()
            val dpages: Short = 1
            val cspages: Short = 1
            val uspages: Short = 1
            val minActivationAmount = TestConstants.TEN_BURST
            val data = EMPTY_BYTE_ARRAY
            var creationLength = 4 // version + reserved
            creationLength += 8 // pages
            creationLength += 8 // minActivationAmount
            creationLength += if (cpages <= 1) 1 else if (cpages < 128) 2 else 4 // code size
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
