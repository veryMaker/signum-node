package brs.services.impl

import brs.api.http.ParameterException
import brs.api.http.common.Parameters.ACCOUNT_PARAMETER
import brs.api.http.common.Parameters.ALIAS_NAME_PARAMETER
import brs.api.http.common.Parameters.ALIAS_PARAMETER
import brs.api.http.common.Parameters.ASSET_PARAMETER
import brs.api.http.common.Parameters.AT_PARAMETER
import brs.api.http.common.Parameters.ENCRYPTED_MESSAGE_DATA_PARAMETER
import brs.api.http.common.Parameters.ENCRYPTED_MESSAGE_NONCE_PARAMETER
import brs.api.http.common.Parameters.ENCRYPT_TO_SELF_MESSAGE_DATA
import brs.api.http.common.Parameters.ENCRYPT_TO_SELF_MESSAGE_NONCE
import brs.api.http.common.Parameters.GOODS_PARAMETER
import brs.api.http.common.Parameters.HEIGHT_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_TO_ENCRYPT_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER
import brs.api.http.common.Parameters.MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER
import brs.api.http.common.Parameters.NUMBER_OF_CONFIRMATIONS_PARAMETER
import brs.api.http.common.Parameters.PUBLIC_KEY_PARAMETER
import brs.api.http.common.Parameters.PURCHASE_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.at.AT
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.common.TestConstants.TEST_SECRET_PHRASE
import brs.entity.*
import brs.objects.Constants.EMPTY_BYTE_ARRAY
import brs.services.*
import brs.util.BurstException
import brs.util.convert.parseHexString
import brs.util.convert.toBytes
import brs.util.crypto.Crypto
import burst.kit.entity.BurstEncryptedMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ParameterServiceImplTest {
    private lateinit var t: ParameterServiceImpl

    private lateinit var accountServiceMock: AccountService
    private lateinit var aliasServiceMock: AliasService
    private lateinit var assetExchangeServiceMock: AssetExchangeService
    private lateinit var digitalGoodsStoreServiceMock: DigitalGoodsStoreService
    private lateinit var blockchainServiceMock: BlockchainService
    private lateinit var blockchainProcessorServiceMock: BlockchainProcessorService
    private lateinit var transactionProcessorServiceMock: TransactionProcessorService
    private lateinit var atServiceMock: ATService

    @Before
    fun setUp() {
        accountServiceMock = mockk(relaxed = true)
        aliasServiceMock = mockk(relaxed = true)
        assetExchangeServiceMock = mockk(relaxed = true)
        digitalGoodsStoreServiceMock = mockk(relaxed = true)
        blockchainServiceMock = mockk(relaxed = true)
        blockchainProcessorServiceMock = mockk(relaxed = true)
        transactionProcessorServiceMock = mockk(relaxed = true)
        atServiceMock = mockk(relaxed = true)

        t = ParameterServiceImpl(
            QuickMocker.dependencyProvider(
                accountServiceMock,
                aliasServiceMock,
                assetExchangeServiceMock,
                digitalGoodsStoreServiceMock,
                blockchainServiceMock,
                blockchainProcessorServiceMock,
                transactionProcessorServiceMock,
                atServiceMock
            )
        )
    }

    @After
    fun tearDown() {
        unmockkObject(Transaction)
    }

    @Test
    fun getAccount() {
        val accountId = "123"
        val mockAccount = mockk<Account>(relaxed = true)

        val request = QuickMocker.httpServletRequest(MockParam(ACCOUNT_PARAMETER, accountId))

        every { accountServiceMock.getAccount(eq(123L)) } returns mockAccount

        assertEquals(mockAccount, t.getAccount(request))
    }

    @Test(expected = ParameterException::class)
    fun getAccount_MissingAccountWhenNoAccountParameterGiven() {
        val request = QuickMocker.httpServletRequest()
        t.getAccount(request)
    }

    @Test(expected = ParameterException::class)
    fun getAccount_UnknownAccountWhenIdNotFound() {
        val accountId = "123"
        val request = QuickMocker.httpServletRequest(MockParam(ACCOUNT_PARAMETER, accountId))

        every { accountServiceMock.getAccount(eq(123L)) } returns null

        t.getAccount(request)
    }

    @Test(expected = ParameterException::class)
    fun getAccount_IncorrectAccountWhenRuntimeExceptionOccurs() {
        val accountId = "123"
        val request = QuickMocker.httpServletRequest(MockParam(ACCOUNT_PARAMETER, accountId))

        every { accountServiceMock.getAccount(eq(123L)) } throws Exception()

        t.getAccount(request)
    }

    @Test
    fun getAccounts() {
        val request = QuickMocker.httpServletRequest()
        val accountID1 = "123"
        val accountID2 = "321"
        val accountIds = arrayOf(accountID1, accountID2)

        every { request.getParameterValues(eq(ACCOUNT_PARAMETER)) } returns accountIds

        val mockAccount1 = mockk<Account>(relaxed = true)
        val mockAccount2 = mockk<Account>(relaxed = true)

        every { accountServiceMock.getAccount(eq(123L)) } returns mockAccount1
        every { accountServiceMock.getAccount(eq(321L)) } returns mockAccount2

        val result = t.getAccounts(request)

        assertNotNull(result)
        assertEquals(2, result.size.toLong())
        assertEquals(mockAccount1, result[0])
        assertEquals(mockAccount2, result[1])
    }

    @Test
    fun getAccounts_emptyResultWhenEmptyAccountValueGiven() {
        val request = QuickMocker.httpServletRequest()
        val accountIds = arrayOf("")

        every { request.getParameterValues(eq(ACCOUNT_PARAMETER)) } returns accountIds

        val result = t.getAccounts(request)

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    fun getAccounts_emptyResultWhenNullAccountValueGiven() {
        val request = QuickMocker.httpServletRequest()
        val accountIds = arrayOfNulls<String>(1)

        every { request.getParameterValues(eq(ACCOUNT_PARAMETER)) } returns accountIds

        val result = t.getAccounts(request)

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }


    @Test(expected = ParameterException::class)
    fun getAccounts_missingAccountWhenNoParametersNull() {
        val request = QuickMocker.httpServletRequest()
        every { request.getParameterValues(eq(ACCOUNT_PARAMETER)) } returns null

        t.getAccounts(request)
    }

    @Test(expected = ParameterException::class)
    fun getAccounts_missingAccountWhenNoParametersGiven() {
        val request = QuickMocker.httpServletRequest()
        val accountIds = arrayOfNulls<String>(0)

        every { request.getParameterValues(eq(ACCOUNT_PARAMETER)) } returns accountIds

        t.getAccounts(request)
    }

    @Test(expected = ParameterException::class)
    fun getAccounts_unknownAccountWhenNotFound() {
        val request = QuickMocker.httpServletRequest()
        val accountID1 = "123"
        val accountIds = arrayOf(accountID1)

        every { request.getParameterValues(eq(ACCOUNT_PARAMETER)) } returns accountIds

        every { accountServiceMock.getAccount(eq(123L)) } returns null

        t.getAccounts(request)
    }

    @Test(expected = ParameterException::class)
    fun getAccounts_incorrectAccountWhenRuntimeExceptionOccurs() {
        val request = QuickMocker.httpServletRequest()
        val accountID1 = "123"
        val accountIds = arrayOf(accountID1)

        every { request.getParameterValues(eq(ACCOUNT_PARAMETER)) } returns accountIds

        every { accountServiceMock.getAccount(eq(123L)) } throws Exception()

        t.getAccounts(request)
    }

    @Test
    fun getSenderAccount_withSecretPhrase() {
        val secretPhrase = TEST_SECRET_PHRASE
        val request = QuickMocker.httpServletRequest(MockParam(SECRET_PHRASE_PARAMETER, secretPhrase))

        val mockAccount = mockk<Account>(relaxed = true)

        every { accountServiceMock.getAccount(eq(Crypto.getPublicKey(secretPhrase))) } returns mockAccount

        assertEquals(mockAccount, t.getSenderAccount(request))
    }

    @Test
    fun getSenderAccount_withPublicKey() {
        val publicKey = "123"
        val request = QuickMocker.httpServletRequest(MockParam(PUBLIC_KEY_PARAMETER, publicKey))

        val mockAccount = mockk<Account>(relaxed = true)

        every { accountServiceMock.getAccount(eq(publicKey.parseHexString())) } returns mockAccount

        assertEquals(mockAccount, t.getSenderAccount(request))
    }

    @Test(expected = ParameterException::class)
    fun getSenderAccount_withPublicKey_runtimeExceptionGivesParameterException() {
        val publicKey = "123"
        val request = QuickMocker.httpServletRequest(MockParam(PUBLIC_KEY_PARAMETER, publicKey))

        every { accountServiceMock.getAccount(eq(publicKey.parseHexString())) } throws Exception()

        t.getSenderAccount(request)
    }

    @Test(expected = ParameterException::class)
    fun getSenderAccount_missingSecretPhraseAndPublicKey() {
        val request = QuickMocker.httpServletRequest()
        t.getSenderAccount(request)
    }

    @Test(expected = ParameterException::class)
    fun getSenderAccount_noAccountFoundResultsInUnknownAccount() {
        val publicKey = "123"
        val request = QuickMocker.httpServletRequest(MockParam(PUBLIC_KEY_PARAMETER, publicKey))

        every { accountServiceMock.getAccount(eq(publicKey.parseHexString())) } returns null

        t.getSenderAccount(request)
    }

    @Test
    fun getAliasByAliasId() {
        val mockAlias = mockk<Alias>(relaxed = true)

        val request = QuickMocker.httpServletRequest(MockParam(ALIAS_PARAMETER, "123"))

        every { aliasServiceMock.getAlias(eq(123L)) } returns mockAlias

        assertEquals(mockAlias, t.getAlias(request))
    }

    @Test
    fun getAliasByAliasName() {
        val mockAlias = mockk<Alias>(relaxed = true)

        val request = QuickMocker.httpServletRequest(MockParam(ALIAS_NAME_PARAMETER, "aliasName"))

        every { aliasServiceMock.getAlias(eq("aliasName")) } returns mockAlias

        assertEquals(mockAlias, t.getAlias(request))
    }

    @Test(expected = ParameterException::class)
    fun getAlias_wrongAliasFormatIsIncorrectAlias() {
        t.getAlias(QuickMocker.httpServletRequest(MockParam(ALIAS_PARAMETER, "Five")))
    }

    @Test(expected = ParameterException::class)
    fun getAlias_noAliasOrAliasNameGivenIsMissingAliasOrAliasName() {
        val request = QuickMocker.httpServletRequest()
        t.getAlias(request)
    }

    @Test(expected = ParameterException::class)
    fun noAliasFoundIsUnknownAlias() {
        val mockAlias = mockk<Alias>(relaxed = true)

        val request = QuickMocker.httpServletRequest(MockParam(ALIAS_PARAMETER, "123"))

        every { aliasServiceMock.getAlias(eq(123L)) } returns null

        assertEquals(mockAlias, t.getAlias(request))
    }

    @Test
    fun getAsset() {
        val request = QuickMocker.httpServletRequest(MockParam(ASSET_PARAMETER, "123"))

        val mockAsset = mockk<Asset>(relaxed = true)

        every { assetExchangeServiceMock.getAsset(eq(123L)) } returns mockAsset

        assertEquals(mockAsset, t.getAsset(request))
    }

    @Test(expected = ParameterException::class)
    fun getAsset_missingIdIsMissingAsset() {
        t.getAsset(QuickMocker.httpServletRequest())
    }

    @Test(expected = ParameterException::class)
    fun getAsset_wrongIdFormatIsIncorrectAsset() {
        t.getAsset(QuickMocker.httpServletRequest(MockParam(ASSET_PARAMETER, "twenty")))
    }

    @Test(expected = ParameterException::class)
    fun getAsset_assetNotFoundIsUnknownAsset() {
        every { assetExchangeServiceMock.getAsset(eq(123L)) } returns null

        t.getAsset(QuickMocker.httpServletRequest(MockParam(ASSET_PARAMETER, "123")))
    }

    @Test
    fun getGoods() {
        val request = QuickMocker.httpServletRequest(
            MockParam(GOODS_PARAMETER, "1")
        )

        val mockGoods = mockk<Goods>(relaxed = true)

        every { digitalGoodsStoreServiceMock.getGoods(eq(1L)) } returns mockGoods

        assertEquals(mockGoods, t.getGoods(request))
    }

    @Test(expected = ParameterException::class)
    fun getGoods_missingGoods() {
        t.getGoods(QuickMocker.httpServletRequest())
    }

    @Test(expected = ParameterException::class)
    fun getGoods_unknownGoods() {
        val request = QuickMocker.httpServletRequest(
            MockParam(GOODS_PARAMETER, "1")
        )

        every { digitalGoodsStoreServiceMock.getGoods(eq(1L)) } returns null

        t.getGoods(request)
    }

    @Test(expected = ParameterException::class)
    fun getGoods_incorrectGoods() {
        val request = QuickMocker.httpServletRequest(
            MockParam(GOODS_PARAMETER, "notANumber")
        )

        t.getGoods(request)
    }

    @Test
    fun getPurchase() {
        val request = QuickMocker.httpServletRequest(
            MockParam(PURCHASE_PARAMETER, "1")
        )

        val mockPurchase = mockk<Purchase>(relaxed = true)

        every { digitalGoodsStoreServiceMock.getPurchase(eq(1L)) } returns mockPurchase

        assertEquals(mockPurchase, t.getPurchase(request))
    }

    @Test(expected = ParameterException::class)
    fun getPurchase_missingPurchase() {
        t.getPurchase(QuickMocker.httpServletRequest())
    }

    @Test(expected = ParameterException::class)
    fun getPurchase_unknownPurchase() {
        val request = QuickMocker.httpServletRequest(
            MockParam(PURCHASE_PARAMETER, "1")
        )

        every { digitalGoodsStoreServiceMock.getPurchase(eq(1L)) } returns null

        t.getPurchase(request)
    }

    @Test(expected = ParameterException::class)
    fun getPurchase_incorrectPurchase() {
        val request = QuickMocker.httpServletRequest(
            MockParam(PURCHASE_PARAMETER, "notANumber")
        )

        t.getPurchase(request)
    }

    @Test
    fun getEncryptMessage_isNotText() {
        val request = QuickMocker.httpServletRequest(
            MockParam(MESSAGE_TO_ENCRYPT_PARAMETER, "beef123"),
            MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
            MockParam(MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, "false")
        )

        val mockRecipientAccount = mockk<Account>(relaxed = true)
        every { mockRecipientAccount.publicKey } returns EMPTY_BYTE_ARRAY

        val encryptedDataMock = mockk<BurstEncryptedMessage>(relaxed = true)

        every { mockRecipientAccount.encryptTo(eq("beef123".parseHexString()), eq(TEST_SECRET_PHRASE), any()) } returns encryptedDataMock

        assertEquals(encryptedDataMock, t.getEncryptedMessage(request, mockRecipientAccount, null))
    }

    @Test(expected = ParameterException::class)
    fun getEncryptMessage_missingRecipientParameterException() {
        val request = QuickMocker.httpServletRequest(
            MockParam(MESSAGE_TO_ENCRYPT_PARAMETER, "beef123"),
            MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
            MockParam(MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, "false")
        )

        val encryptedDataMock = mockk<BurstEncryptedMessage>(relaxed = true)

        assertEquals(encryptedDataMock, t.getEncryptedMessage(request, null, null))
    }

    @Test
    fun getEncryptMessage_isText() {
        val request = QuickMocker.httpServletRequest(
            MockParam(MESSAGE_TO_ENCRYPT_PARAMETER, "message"),
            MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
            MockParam(MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, "true")
        )

        val mockRecipientAccount = mockk<Account>(relaxed = true)
        every { mockRecipientAccount.publicKey } returns EMPTY_BYTE_ARRAY

        val encryptedDataMock = mockk<BurstEncryptedMessage>(relaxed = true)

        every { mockRecipientAccount.encryptTo(eq("message".toBytes()), eq(TEST_SECRET_PHRASE), any()) } returns encryptedDataMock

        assertEquals(encryptedDataMock, t.getEncryptedMessage(request, mockRecipientAccount, null))
    }

    @Test
    fun getEncryptMessage_encryptMessageAndNonce() {
        val request = QuickMocker.httpServletRequest(
            MockParam(ENCRYPTED_MESSAGE_DATA_PARAMETER, "abc"),
            MockParam(ENCRYPTED_MESSAGE_NONCE_PARAMETER, "123")
        )

        val result = t.getEncryptedMessage(request, null, null)

        assertEquals((-85).toByte().toLong(), result!!.data[0].toLong())
        assertEquals(18.toByte().toLong(), result.nonce[0].toLong())
    }

    @Test(expected = ParameterException::class)
    fun getEncryptMessage_encryptMessageAndNonce_incorrectEncryptedMessage() {
        val request = QuickMocker.httpServletRequest(
            MockParam(ENCRYPTED_MESSAGE_DATA_PARAMETER, "zz"),
            MockParam(ENCRYPTED_MESSAGE_NONCE_PARAMETER, "123")
        )

        t.getEncryptedMessage(request, null, null)
    }

    @Test(expected = ParameterException::class)
    fun getEncryptMessage_encryptionParameterException() {
        val request = QuickMocker.httpServletRequest(
            MockParam(MESSAGE_TO_ENCRYPT_PARAMETER, "invalidHexNumber"),
            MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
            MockParam(MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, "false")
        )

        val mockAccount = mockk<Account>(relaxed = true)
        every { accountServiceMock.getAccount(eq(Crypto.getPublicKey(TEST_SECRET_PHRASE))) } returns mockAccount

        t.getEncryptedMessage(request, mockAccount, null)
    }

    @Test
    fun getEncryptMessage_messageToSelf_messageNullReturnsNull() {
        assertNull(t.getEncryptedMessage(QuickMocker.httpServletRequest(), null, null))
    }

    @Test
    fun getEncryptToSelfMessage_isNotText() {
        val request = QuickMocker.httpServletRequest(
            MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER, "beef123"),
            MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
            MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER, "false")
        )

        val mockAccount = mockk<Account>(relaxed = true)
        every { accountServiceMock.getAccount(eq(Crypto.getPublicKey(TEST_SECRET_PHRASE))) } returns mockAccount

        val encryptedDataMock = mockk<BurstEncryptedMessage>(relaxed = true)

        every { mockAccount.encryptTo(eq("beef123".parseHexString()), eq(TEST_SECRET_PHRASE), any()) } returns encryptedDataMock

        assertEquals(encryptedDataMock, t.getEncryptToSelfMessage(request))
    }

    @Test(expected = ParameterException::class)
    fun getEncryptToSelfMessage_isNotText_notHexParameterException() {
        val request = QuickMocker.httpServletRequest(
            MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER, "zzz"),
            MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
            MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER, "false")
        )

        val mockAccount = mockk<Account>(relaxed = true)
        every { accountServiceMock.getAccount(eq(Crypto.getPublicKey(TEST_SECRET_PHRASE))) } returns mockAccount

        val encryptedDataMock = mockk<BurstEncryptedMessage>(relaxed = true)

        every { mockAccount.encryptTo(eq("beef123".parseHexString()), eq(TEST_SECRET_PHRASE), any()) } returns encryptedDataMock

        assertEquals(encryptedDataMock, t.getEncryptToSelfMessage(request))
    }

    @Test
    fun getEncryptToSelfMessage_encryptMessageToSelf_isText() {
        val request = QuickMocker.httpServletRequest(
            MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER, "message"),
            MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
            MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER, "true")
        )

        val mockAccount = mockk<Account>(relaxed = true)
        every { accountServiceMock.getAccount(eq(Crypto.getPublicKey(TEST_SECRET_PHRASE))) } returns mockAccount

        val encryptedDataMock = mockk<BurstEncryptedMessage>(relaxed = true)

        every { mockAccount.encryptTo(eq("message".toBytes()), eq(TEST_SECRET_PHRASE), any()) } returns encryptedDataMock

        assertEquals(encryptedDataMock, t.getEncryptToSelfMessage(request))
    }

    @Test
    fun getEncryptToSelfMessage_encryptMessageAndNonce() {
        val request = QuickMocker.httpServletRequest(
            MockParam(ENCRYPT_TO_SELF_MESSAGE_DATA, "abc"),
            MockParam(ENCRYPT_TO_SELF_MESSAGE_NONCE, "123")
        )

        val result = t.getEncryptToSelfMessage(request)

        assertEquals((-85).toByte().toLong(), result!!.data[0].toLong())
        assertEquals(18.toByte().toLong(), result.nonce[0].toLong())
    }

    @Test(expected = ParameterException::class)
    fun getEncryptToSelfMessage_encryptMessageAndNonce_incorrectEncryptedMessage() {
        val request = QuickMocker.httpServletRequest(
            MockParam(ENCRYPT_TO_SELF_MESSAGE_DATA, "zz"),
            MockParam(ENCRYPT_TO_SELF_MESSAGE_NONCE, "123")
        )

        t.getEncryptToSelfMessage(request)
    }

    @Test(expected = ParameterException::class)
    fun getEncryptToSelfMessage_encryptionParameterException() {
        val request = QuickMocker.httpServletRequest(
            MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER, "invalidHexNumber"),
            MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
            MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER, "false")
        )

        val mockAccount = mockk<Account>(relaxed = true)
        every { accountServiceMock.getAccount(eq(Crypto.getPublicKey(TEST_SECRET_PHRASE))) } returns mockAccount

        t.getEncryptToSelfMessage(request)
    }

    @Test
    fun getEncryptToSelfMessage_messageToSelf_messageNullReturnsNull() {
        assertNull(t.getEncryptToSelfMessage(QuickMocker.httpServletRequest()))
    }

    @Test
    fun getSecretPhrase() {
        val request = QuickMocker.httpServletRequest(MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE))

        assertEquals(TEST_SECRET_PHRASE, t.getSecretPhrase(request))
    }

    @Test(expected = ParameterException::class)
    fun getSecretPhrase_phraseMissingParameterException() {
        t.getSecretPhrase(QuickMocker.httpServletRequest())
    }

    @Test
    fun getNumberOfConfirmations() {
        every { blockchainServiceMock.height } returns 6
        assertEquals(
            5,
            t.getNumberOfConfirmations(
                QuickMocker.httpServletRequest(
                    MockParam(
                        NUMBER_OF_CONFIRMATIONS_PARAMETER,
                        "5"
                    )
                )
            ).toLong()
        )
    }

    @Test
    fun getNumberOfConfirmations_emptyNumberOfConfirmationsIs0() {
        assertEquals(0, t.getNumberOfConfirmations(QuickMocker.httpServletRequest()).toLong())
    }

    @Test(expected = ParameterException::class)
    fun getNumberOfConfirmations_wrongFormatNumberOfConfirmationsParameterException() {
        t.getNumberOfConfirmations(
            QuickMocker.httpServletRequest(
                MockParam(
                    NUMBER_OF_CONFIRMATIONS_PARAMETER,
                    "noNumber"
                )
            )
        )
    }

    @Test(expected = ParameterException::class)
    fun getNumberOfConfirmations_numberOfConfirmationsBiggerThanBlockchainHeightParameterException() {
        every { blockchainServiceMock.height } returns 4
        assertEquals(
            5,
            t.getNumberOfConfirmations(
                QuickMocker.httpServletRequest(
                    MockParam(
                        NUMBER_OF_CONFIRMATIONS_PARAMETER,
                        "5"
                    )
                )
            ).toLong()
        )
    }

    @Test
    fun getHeight() {
        every { blockchainServiceMock.height } returns 6
        every { blockchainProcessorServiceMock.minRollbackHeight } returns 4
        assertEquals(5, t.getHeight(QuickMocker.httpServletRequest(MockParam(HEIGHT_PARAMETER, "5"))).toLong())
    }

    @Test
    fun getHeight_missingHeightParameterIsMinus1() {
        assertEquals(-1, t.getHeight(QuickMocker.httpServletRequest()).toLong())
    }

    @Test(expected = ParameterException::class)
    fun getHeight_wrongFormatHeightParameterException() {
        assertEquals(-1, t.getHeight(QuickMocker.httpServletRequest(MockParam(HEIGHT_PARAMETER, "five"))).toLong())
    }

    @Test(expected = ParameterException::class)
    fun getHeight_negativeHeightParameterException() {
        t.getHeight(QuickMocker.httpServletRequest(MockParam(HEIGHT_PARAMETER, "-1")))
    }

    @Test(expected = ParameterException::class)
    fun getHeight_heightGreaterThanBlockchainHeightParameterException() {
        every { blockchainServiceMock.height } returns 5
        t.getHeight(QuickMocker.httpServletRequest(MockParam(HEIGHT_PARAMETER, "6")))
    }

    @Test(expected = ParameterException::class)
    fun getHeight_heightUnderMinRollbackHeightParameterException() {
        every { blockchainServiceMock.height } returns 10
        every { blockchainProcessorServiceMock.minRollbackHeight } returns 12
        t.getHeight(QuickMocker.httpServletRequest(MockParam(HEIGHT_PARAMETER, "10")))
    }

    @Test
    fun parseTransaction_transactionBytes() {
        val mockTransaction = mockk<Transaction>(relaxed = true)

        mockkObject(Transaction.Companion)
        every { Transaction.parseTransaction(any(), any<ByteArray>()) } returns mockTransaction

        assertEquals(mockTransaction, t.parseTransaction("123", null))
    }

    @Test(expected = ParameterException::class)
    fun parseTransaction_transactionBytes_validationExceptionParseHexStringOccurs() {
        t.parseTransaction("ZZZ", null)
    }

    @Test(expected = ParameterException::class)
    fun parseTransaction_transactionBytes_runTimeExceptionOccurs() {
        mockkObject(Transaction.Companion)
        every { Transaction.parseTransaction(any(), any<ByteArray>()) } throws RuntimeException()

        t.parseTransaction("123", null)
    }

    @Test
    fun parseTransaction_transactionJSON() {
        val mockTransaction = mockk<Transaction>(relaxed = true)

        mockkObject(Transaction.Companion)
        every { Transaction.parseTransaction(any(), any(), any()) } returns mockTransaction

        assertEquals(mockTransaction, t.parseTransaction(null, "{}"))
    }

    @Test(expected = ParameterException::class)
    fun parseTransaction_transactionJSON_validationExceptionOccurs() {
        mockkObject(Transaction.Companion)
        every { Transaction.parseTransaction(any(), any(), any()) } throws BurstException.NotValidException("")

        t.parseTransaction(null, "{}")
    }

    @Test(expected = ParameterException::class)
    fun parseTransaction_transactionJSON_runTimeExceptionOccurs() {
        mockkObject(Transaction.Companion)
        every { Transaction.parseTransaction(any(), any(), any()) } throws RuntimeException()

        t.parseTransaction(null, "{}")
    }

    @Test(expected = ParameterException::class)
    fun parseTransaction_transactionJSON_parseExceptionTransactionProcessorOccurs() {
        t.parseTransaction(null, "badJson")
    }

    @Test(expected = ParameterException::class)
    fun parseTransaction_missingRequiredTransactionBytesOrJson() {
        t.parseTransaction(null, null)
    }

    @Test
    fun getAT() {
        val atId = 123L

        val request = QuickMocker.httpServletRequest(MockParam(AT_PARAMETER, atId))

        val mockAT = mockk<AT>(relaxed = true)

        every { atServiceMock.getAT(eq(atId)) } returns mockAT

        assertEquals(mockAT, t.getAT(request))
    }

    @Test(expected = ParameterException::class)
    fun getAT_missingAT() {
        val request = QuickMocker.httpServletRequest()

        t.getAT(request)
    }

    @Test(expected = ParameterException::class)
    fun getAT_incorrectAT() {
        val request = QuickMocker.httpServletRequest(
            MockParam(AT_PARAMETER, "notLongId")
        )

        t.getAT(request)
    }

    @Test(expected = ParameterException::class)
    fun getAT_unknownAT() {
        val atId = 123L

        val request = QuickMocker.httpServletRequest(
            MockParam(AT_PARAMETER, atId)
        )

        every { atServiceMock.getAT(eq(atId)) } returns null

        t.getAT(request)
    }

}
