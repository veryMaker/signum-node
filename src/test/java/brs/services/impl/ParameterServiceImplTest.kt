package brs.services.impl

import brs.*
import brs.BurstException.ValidationException
import brs.assetexchange.AssetExchange
import brs.at.AT
import brs.common.QuickMocker
import brs.common.QuickMocker.MockParam
import brs.crypto.Crypto
import brs.crypto.EncryptedData
import brs.http.ParameterException
import brs.services.ATService
import brs.services.AccountService
import brs.services.AliasService
import brs.services.DGSGoodsStoreService
import brs.util.Convert
import com.google.gson.JsonObject
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import brs.common.TestConstants.TEST_SECRET_PHRASE
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*

class ParameterServiceImplTest {

    private var t: ParameterServiceImpl? = null

    private var accountServiceMock: AccountService? = null
    private var aliasServiceMock: AliasService? = null
    private var assetExchangeMock: AssetExchange? = null
    private var dgsGoodsStoreServiceMock: DGSGoodsStoreService? = null
    private var blockchainMock: Blockchain? = null
    private var blockchainProcessorMock: BlockchainProcessor? = null
    private var transactionProcessorMock: TransactionProcessor? = null
    private var atServiceMock: ATService? = null

    @Before
    fun setUp() {
        accountServiceMock = mock()
        aliasServiceMock = mock()
        assetExchangeMock = mock()
        dgsGoodsStoreServiceMock = mock()
        blockchainMock = mock()
        blockchainProcessorMock = mock()
        transactionProcessorMock = mock()
        atServiceMock = mock()

        t = ParameterServiceImpl(accountServiceMock!!, aliasServiceMock!!, assetExchangeMock!!, dgsGoodsStoreServiceMock!!, blockchainMock!!, blockchainProcessorMock!!, transactionProcessorMock!!, atServiceMock!!)
    }

    @Test
    @Throws(BurstException::class)
    fun getAccount() {
        val accountId = "123"
        val mockAccount = mock<Account>()

        val req = QuickMocker.httpServletRequest(MockParam(ACCOUNT_PARAMETER, accountId))

        whenever(accountServiceMock!!.getAccount(eq(123L))).doReturn(mockAccount)

        assertEquals(mockAccount, t!!.getAccount(req))
    }

    @Test(expected = ParameterException::class)
    @Throws(BurstException::class)
    fun getAccount_MissingAccountWhenNoAccountParameterGiven() {
        val req = QuickMocker.httpServletRequest()
        t!!.getAccount(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(BurstException::class)
    fun getAccount_UnknownAccountWhenIdNotFound() {
        val accountId = "123"
        val req = QuickMocker.httpServletRequest(MockParam(ACCOUNT_PARAMETER, accountId))

        whenever(accountServiceMock!!.getAccount(eq(123L))).doReturn(null)

        t!!.getAccount(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(BurstException::class)
    fun getAccount_IncorrectAccountWhenRuntimeExceptionOccurs() {
        val accountId = "123"
        val req = QuickMocker.httpServletRequest(MockParam(ACCOUNT_PARAMETER, accountId))

        whenever(accountServiceMock!!.getAccount(eq(123L))).thenThrow(RuntimeException())

        t!!.getAccount(req)
    }

    @Test
    @Throws(ParameterException::class)
    fun getAccounts() {
        val req = QuickMocker.httpServletRequest()
        val accountID1 = "123"
        val accountID2 = "321"
        val accountIds = arrayOf(accountID1, accountID2)

        whenever(req.getParameterValues(eq(ACCOUNT_PARAMETER))).doReturn(accountIds)

        val mockAccount1 = mock<Account>()
        val mockAccount2 = mock<Account>()

        whenever(accountServiceMock!!.getAccount(eq(123L))).doReturn(mockAccount1)
        whenever(accountServiceMock!!.getAccount(eq(321L))).doReturn(mockAccount2)

        val result = t!!.getAccounts(req)

        assertNotNull(result)
        assertEquals(2, result.size.toLong())
        assertEquals(mockAccount1, result[0])
        assertEquals(mockAccount2, result[1])
    }

    @Test
    @Throws(ParameterException::class)
    fun getAccounts_emptyResultWhenEmptyAccountValueGiven() {
        val req = QuickMocker.httpServletRequest()
        val accountIds = arrayOf("")

        whenever(req.getParameterValues(eq(ACCOUNT_PARAMETER))).doReturn(accountIds)

        val result = t!!.getAccounts(req)

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

    @Test
    @Throws(ParameterException::class)
    fun getAccounts_emptyResultWhenNullAccountValueGiven() {
        val req = QuickMocker.httpServletRequest()
        val accountIds = arrayOfNulls<String>(1)

        whenever(req.getParameterValues(eq(ACCOUNT_PARAMETER))).doReturn(accountIds)

        val result = t!!.getAccounts(req)

        assertNotNull(result)
        assertTrue(result.isEmpty())
    }


    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAccounts_missingAccountWhenNoParametersNull() {
        val req = QuickMocker.httpServletRequest()
        whenever(req.getParameterValues(eq(ACCOUNT_PARAMETER))).doReturn(null)

        t!!.getAccounts(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAccounts_missingAccountWhenNoParametersGiven() {
        val req = QuickMocker.httpServletRequest()
        val accountIds = arrayOfNulls<String>(0)

        whenever(req.getParameterValues(eq(ACCOUNT_PARAMETER))).doReturn(accountIds)

        t!!.getAccounts(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAccounts_unknownAccountWhenNotFound() {
        val req = QuickMocker.httpServletRequest()
        val accountID1 = "123"
        val accountIds = arrayOf(accountID1)

        whenever(req.getParameterValues(eq(ACCOUNT_PARAMETER))).doReturn(accountIds)

        whenever(accountServiceMock!!.getAccount(eq(123L))).doReturn(null)

        t!!.getAccounts(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAccounts_incorrectAccountWhenRuntimeExceptionOccurs() {
        val req = QuickMocker.httpServletRequest()
        val accountID1 = "123"
        val accountIds = arrayOf(accountID1)

        whenever(req.getParameterValues(eq(ACCOUNT_PARAMETER))).doReturn(accountIds)

        whenever(accountServiceMock!!.getAccount(eq(123L))).thenThrow(RuntimeException())

        t!!.getAccounts(req)
    }

    @Test
    @Throws(ParameterException::class)
    fun getSenderAccount_withSecretPhrase() {
        val secretPhrase = TEST_SECRET_PHRASE
        val req = QuickMocker.httpServletRequest(MockParam(SECRET_PHRASE_PARAMETER, secretPhrase))

        val mockAccount = mock<Account>()

        whenever(accountServiceMock!!.getAccount(eq(Crypto.getPublicKey(secretPhrase)))).doReturn(mockAccount)

        assertEquals(mockAccount, t!!.getSenderAccount(req))
    }

    @Test
    @Throws(ParameterException::class)
    fun getSenderAccount_withPublicKey() {
        val publicKey = "123"
        val req = QuickMocker.httpServletRequest(MockParam(PUBLIC_KEY_PARAMETER, publicKey))

        val mockAccount = mock<Account>()

        whenever(accountServiceMock!!.getAccount(eq(Convert.parseHexString(publicKey)))).doReturn(mockAccount)

        assertEquals(mockAccount, t!!.getSenderAccount(req))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getSenderAccount_withPublicKey_runtimeExceptionGivesParameterException() {
        val publicKey = "123"
        val req = QuickMocker.httpServletRequest(MockParam(PUBLIC_KEY_PARAMETER, publicKey))

        whenever(accountServiceMock!!.getAccount(eq(Convert.parseHexString(publicKey)))).thenThrow(RuntimeException())

        t!!.getSenderAccount(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getSenderAccount_missingSecretPhraseAndPublicKey() {
        val req = QuickMocker.httpServletRequest()
        t!!.getSenderAccount(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getSenderAccount_noAccountFoundResultsInUnknownAccount() {
        val publicKey = "123"
        val req = QuickMocker.httpServletRequest(MockParam(PUBLIC_KEY_PARAMETER, publicKey))

        whenever(accountServiceMock!!.getAccount(eq(Convert.parseHexString(publicKey)))).doReturn(null)

        t!!.getSenderAccount(req)
    }

    @Test
    @Throws(ParameterException::class)
    fun getAliasByAliasId() {
        val mockAlias = mock<Alias>()

        val req = QuickMocker.httpServletRequest(MockParam(ALIAS_PARAMETER, "123"))

        whenever(aliasServiceMock!!.getAlias(eq(123L))).doReturn(mockAlias)

        assertEquals(mockAlias, t!!.getAlias(req))
    }

    @Test
    @Throws(ParameterException::class)
    fun getAliasByAliasName() {
        val mockAlias = mock<Alias>()

        val req = QuickMocker.httpServletRequest(MockParam(ALIAS_NAME_PARAMETER, "aliasName"))

        whenever(aliasServiceMock!!.getAlias(eq("aliasName"))).doReturn(mockAlias)

        assertEquals(mockAlias, t!!.getAlias(req))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAlias_wrongAliasFormatIsIncorrectAlias() {
        t!!.getAlias(QuickMocker.httpServletRequest(MockParam(ALIAS_PARAMETER, "Five")))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAlias_noAliasOrAliasNameGivenIsMissingAliasOrAliasName() {
        val req = QuickMocker.httpServletRequest()
        t!!.getAlias(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun noAliasFoundIsUnknownAlias() {
        val mockAlias = mock<Alias>()

        val req = QuickMocker.httpServletRequest(MockParam(ALIAS_PARAMETER, "123"))

        whenever(aliasServiceMock!!.getAlias(eq(123L))).doReturn(null)

        assertEquals(mockAlias, t!!.getAlias(req))
    }

    @Test
    @Throws(ParameterException::class)
    fun getAsset() {
        val req = QuickMocker.httpServletRequest(MockParam(ASSET_PARAMETER, "123"))

        val mockAsset = mock<Asset>()

        whenever(assetExchangeMock!!.getAsset(eq(123L))).doReturn(mockAsset)

        assertEquals(mockAsset, t!!.getAsset(req))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAsset_missingIdIsMissingAsset() {
        t!!.getAsset(QuickMocker.httpServletRequest())
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAsset_wrongIdFormatIsIncorrectAsset() {
        t!!.getAsset(QuickMocker.httpServletRequest(MockParam(ASSET_PARAMETER, "twenty")))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAsset_assetNotFoundIsUnknownAsset() {
        whenever(assetExchangeMock!!.getAsset(eq(123L))).doReturn(null)

        t!!.getAsset(QuickMocker.httpServletRequest(MockParam(ASSET_PARAMETER, "123")))
    }

    @Test
    @Throws(ParameterException::class)
    fun getGoods() {
        val req = QuickMocker.httpServletRequest(
                MockParam(GOODS_PARAMETER, "1")
        )

        val mockGoods = mock<DigitalGoodsStore.Goods>()

        whenever(dgsGoodsStoreServiceMock!!.getGoods(eq(1L))).doReturn(mockGoods)

        assertEquals(mockGoods, t!!.getGoods(req))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getGoods_missingGoods() {
        t!!.getGoods(QuickMocker.httpServletRequest())
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getGoods_unknownGoods() {
        val req = QuickMocker.httpServletRequest(
                MockParam(GOODS_PARAMETER, "1")
        )

        whenever(dgsGoodsStoreServiceMock!!.getGoods(eq(1L))).doReturn(null)

        t!!.getGoods(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getGoods_incorrectGoods() {
        val req = QuickMocker.httpServletRequest(
                MockParam(GOODS_PARAMETER, "notANumber")
        )

        t!!.getGoods(req)
    }

    @Test
    @Throws(ParameterException::class)
    fun getPurchase() {
        val req = QuickMocker.httpServletRequest(
                MockParam(PURCHASE_PARAMETER, "1")
        )

        val mockPurchase = mock<DigitalGoodsStore.Purchase>()

        whenever(dgsGoodsStoreServiceMock!!.getPurchase(eq(1L))).doReturn(mockPurchase)

        assertEquals(mockPurchase, t!!.getPurchase(req))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getPurchase_missingPurchase() {
        t!!.getPurchase(QuickMocker.httpServletRequest())
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getPurchase_unknownPurchase() {
        val req = QuickMocker.httpServletRequest(
                MockParam(PURCHASE_PARAMETER, "1")
        )

        whenever(dgsGoodsStoreServiceMock!!.getPurchase(eq(1L))).doReturn(null)

        t!!.getPurchase(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getPurchase_incorrectPurchase() {
        val req = QuickMocker.httpServletRequest(
                MockParam(PURCHASE_PARAMETER, "notANumber")
        )

        t!!.getPurchase(req)
    }

    @Test
    @Throws(ParameterException::class)
    fun getEncryptMessage_isNotText() {
        val req = QuickMocker.httpServletRequest(
                MockParam(MESSAGE_TO_ENCRYPT_PARAMETER, "beef123"),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, "false"))

        val mockRecipientAccount = mock<Account>()
        whenever(mockRecipientAccount.publicKey).doReturn(ByteArray(0))

        val encryptedDataMock = mock<EncryptedData>()

        whenever(mockRecipientAccount.encryptTo(eq(Convert.parseHexString("beef123")), eq<String>(TEST_SECRET_PHRASE))).doReturn(encryptedDataMock)

        assertEquals(encryptedDataMock, t!!.getEncryptedMessage(req, mockRecipientAccount, null))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getEncryptMessage_missingRecipientParameterException() {
        val req = QuickMocker.httpServletRequest(
                MockParam(MESSAGE_TO_ENCRYPT_PARAMETER, "beef123"),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, "false"))

        val encryptedDataMock = mock<EncryptedData>()

        assertEquals(encryptedDataMock, t!!.getEncryptedMessage(req, null, null))
    }

    @Test
    @Throws(ParameterException::class)
    fun getEncryptMessage_isText() {
        val req = QuickMocker.httpServletRequest(
                MockParam(MESSAGE_TO_ENCRYPT_PARAMETER, "message"),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, "true"))

        val mockRecipientAccount = mock<Account>()
        whenever(mockRecipientAccount.publicKey).doReturn(ByteArray(0))

        val encryptedDataMock = mock<EncryptedData>()

        whenever(mockRecipientAccount.encryptTo(eq(Convert.toBytes("message")), eq<String>(TEST_SECRET_PHRASE))).doReturn(encryptedDataMock)

        assertEquals(encryptedDataMock, t!!.getEncryptedMessage(req, mockRecipientAccount, null))
    }

    @Test
    @Throws(ParameterException::class)
    fun getEncryptMessage_encryptMessageAndNonce() {
        val req = QuickMocker.httpServletRequest(
                MockParam(ENCRYPTED_MESSAGE_DATA_PARAMETER, "abc"),
                MockParam(ENCRYPTED_MESSAGE_NONCE_PARAMETER, "123"))

        val result = t!!.getEncryptedMessage(req, null, null)

        assertEquals((-85).toByte().toLong(), result!!.data[0].toLong())
        assertEquals(18.toByte().toLong(), result.nonce[0].toLong())
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getEncryptMessage_encryptMessageAndNonce_runtimeExceptionIncorrectEncryptedMessage() {
        val req = QuickMocker.httpServletRequest(
                MockParam(ENCRYPTED_MESSAGE_DATA_PARAMETER, "zz"),
                MockParam(ENCRYPTED_MESSAGE_NONCE_PARAMETER, "123"))

        t!!.getEncryptedMessage(req, null, null)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getEncryptMessage_encryptionRuntimeExceptionParameterException() {
        val req = QuickMocker.httpServletRequest(
                MockParam(MESSAGE_TO_ENCRYPT_PARAMETER, "invalidHexNumber"),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(MESSAGE_TO_ENCRYPT_IS_TEXT_PARAMETER, "false"))

        val mockAccount = mock<Account>()
        whenever(accountServiceMock!!.getAccount(eq(Crypto.getPublicKey(TEST_SECRET_PHRASE)))).doReturn(mockAccount)

        t!!.getEncryptedMessage(req, mockAccount, null)
    }

    @Test
    @Throws(ParameterException::class)
    fun getEncryptMessage_messageToSelf_messageNullReturnsNull() {
        assertNull(t!!.getEncryptedMessage(QuickMocker.httpServletRequest(), null, null))
    }

    @Test
    @Throws(ParameterException::class)
    fun getEncryptToSelfMessage_isNotText() {
        val req = QuickMocker.httpServletRequest(
                MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER, "beef123"),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER, "false"))

        val mockAccount = mock<Account>()
        whenever(accountServiceMock!!.getAccount(eq(Crypto.getPublicKey(TEST_SECRET_PHRASE)))).doReturn(mockAccount)

        val encryptedDataMock = mock<EncryptedData>()

        whenever(mockAccount.encryptTo(eq(Convert.parseHexString("beef123")), eq<String>(TEST_SECRET_PHRASE))).doReturn(encryptedDataMock)

        assertEquals(encryptedDataMock, t!!.getEncryptToSelfMessage(req))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getEncryptToSelfMessage_isNotText_notHexParameterException() {
        val req = QuickMocker.httpServletRequest(
                MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER, "zzz"),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER, "false"))

        val mockAccount = mock<Account>()
        whenever(accountServiceMock!!.getAccount(eq(Crypto.getPublicKey(TEST_SECRET_PHRASE)))).doReturn(mockAccount)

        val encryptedDataMock = mock<EncryptedData>()

        whenever(mockAccount.encryptTo(eq(Convert.parseHexString("beef123")), eq<String>(TEST_SECRET_PHRASE))).doReturn(encryptedDataMock)

        assertEquals(encryptedDataMock, t!!.getEncryptToSelfMessage(req))
    }

    @Test
    @Throws(ParameterException::class)
    fun getEncryptToSelfMessage_encryptMessageToSelf_isText() {
        val req = QuickMocker.httpServletRequest(
                MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER, "message"),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER, "true"))

        val mockAccount = mock<Account>()
        whenever(accountServiceMock!!.getAccount(eq(Crypto.getPublicKey(TEST_SECRET_PHRASE)))).doReturn(mockAccount)

        val encryptedDataMock = mock<EncryptedData>()

        whenever(mockAccount.encryptTo(eq(Convert.toBytes("message")), eq<String>(TEST_SECRET_PHRASE))).doReturn(encryptedDataMock)

        assertEquals(encryptedDataMock, t!!.getEncryptToSelfMessage(req))
    }

    @Test
    @Throws(ParameterException::class)
    fun getEncryptToSelfMessage_encryptMessageAndNonce() {
        val req = QuickMocker.httpServletRequest(
                MockParam(ENCRYPT_TO_SELF_MESSAGE_DATA, "abc"),
                MockParam(ENCRYPT_TO_SELF_MESSAGE_NONCE, "123"))

        val result = t!!.getEncryptToSelfMessage(req)

        assertEquals((-85).toByte().toLong(), result!!.data[0].toLong())
        assertEquals(18.toByte().toLong(), result.nonce[0].toLong())
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getEncryptToSelfMessage_encryptMessageAndNonce_runtimeExceptionIncorrectEncryptedMessage() {
        val req = QuickMocker.httpServletRequest(
                MockParam(ENCRYPT_TO_SELF_MESSAGE_DATA, "zz"),
                MockParam(ENCRYPT_TO_SELF_MESSAGE_NONCE, "123"))

        t!!.getEncryptToSelfMessage(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getEncryptToSelfMessage_encryptionRuntimeExceptionParameterException() {
        val req = QuickMocker.httpServletRequest(
                MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_PARAMETER, "invalidHexNumber"),
                MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE),
                MockParam(MESSAGE_TO_ENCRYPT_TO_SELF_IS_TEXT_PARAMETER, "false"))

        val mockAccount = mock<Account>()
        whenever(accountServiceMock!!.getAccount(eq(Crypto.getPublicKey(TEST_SECRET_PHRASE)))).doReturn(mockAccount)

        t!!.getEncryptToSelfMessage(req)
    }

    @Test
    @Throws(ParameterException::class)
    fun getEncryptToSelfMessage_messageToSelf_messageNullReturnsNull() {
        assertNull(t!!.getEncryptToSelfMessage(QuickMocker.httpServletRequest()))
    }

    @Test
    @Throws(ParameterException::class)
    fun getSecretPhrase() {
        val req = QuickMocker.httpServletRequest(MockParam(SECRET_PHRASE_PARAMETER, TEST_SECRET_PHRASE))

        assertEquals(TEST_SECRET_PHRASE, t!!.getSecretPhrase(req))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getSecretPhrase_phraseMissingParameterException() {
        t!!.getSecretPhrase(QuickMocker.httpServletRequest())
    }

    @Test
    @Throws(ParameterException::class)
    fun getNumberOfConfirmations() {
        whenever(blockchainMock!!.height).doReturn(6)
        assertEquals(5, t!!.getNumberOfConfirmations(QuickMocker.httpServletRequest(MockParam(NUMBER_OF_CONFIRMATIONS_PARAMETER, "5"))).toLong())
    }

    @Test
    @Throws(ParameterException::class)
    fun getNumberOfConfirmations_emptyNumberOfConfirmationsIs0() {
        assertEquals(0, t!!.getNumberOfConfirmations(QuickMocker.httpServletRequest()).toLong())
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getNumberOfConfirmations_wrongFormatNumberOfConfirmationsParameterException() {
        t!!.getNumberOfConfirmations(QuickMocker.httpServletRequest(MockParam(NUMBER_OF_CONFIRMATIONS_PARAMETER, "noNumber")))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getNumberOfConfirmations_numberOfConfirmationsBiggerThanBlockchainHeightParameterException() {
        whenever(blockchainMock!!.height).doReturn(4)
        assertEquals(5, t!!.getNumberOfConfirmations(QuickMocker.httpServletRequest(MockParam(NUMBER_OF_CONFIRMATIONS_PARAMETER, "5"))).toLong())
    }

    @Test
    @Throws(ParameterException::class)
    fun getHeight() {
        whenever(blockchainMock!!.height).doReturn(6)
        whenever(blockchainProcessorMock!!.minRollbackHeight).doReturn(4)
        assertEquals(5, t!!.getHeight(QuickMocker.httpServletRequest(MockParam(HEIGHT_PARAMETER, "5"))).toLong())
    }

    @Test
    @Throws(ParameterException::class)
    fun getHeight_missingHeightParameterIsMinus1() {
        assertEquals(-1, t!!.getHeight(QuickMocker.httpServletRequest()).toLong())
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getHeight_wrongFormatHeightParameterException() {
        assertEquals(-1, t!!.getHeight(QuickMocker.httpServletRequest(MockParam(HEIGHT_PARAMETER, "five"))).toLong())
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getHeight_negativeHeightParameterException() {
        t!!.getHeight(QuickMocker.httpServletRequest(MockParam(HEIGHT_PARAMETER, "-1")))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getHeight_heightGreaterThanBlockchainHeightParameterException() {
        whenever(blockchainMock!!.height).doReturn(5)
        t!!.getHeight(QuickMocker.httpServletRequest(MockParam(HEIGHT_PARAMETER, "6")))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getHeight_heightUnderMinRollbackHeightParameterException() {
        whenever(blockchainMock!!.height).doReturn(10)
        whenever(blockchainProcessorMock!!.minRollbackHeight).doReturn(12)
        t!!.getHeight(QuickMocker.httpServletRequest(MockParam(HEIGHT_PARAMETER, "10")))
    }

    @Test
    @Throws(ValidationException::class, ParameterException::class)
    fun parseTransaction_transactionBytes() {
        val mockTransaction = mock<Transaction>()

        whenever(transactionProcessorMock!!.parseTransaction(any<ByteArray>())).doReturn(mockTransaction)

        assertEquals(mockTransaction, t!!.parseTransaction("123", null))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun parseTransaction_transactionBytes_validationExceptionParseHexStringOccurs() {
        t!!.parseTransaction("ZZZ", null)
    }

    @Test(expected = ParameterException::class)
    @Throws(ValidationException::class, ParameterException::class)
    fun parseTransaction_transactionBytes_runTimeExceptionOccurs() {
        whenever(transactionProcessorMock!!.parseTransaction(any<ByteArray>())).thenThrow(RuntimeException())

        t!!.parseTransaction("123", null)
    }

    @Test
    @Throws(ValidationException::class, ParameterException::class)
    fun parseTransaction_transactionJSON() {
        val mockTransaction = mock<Transaction>()

        whenever(transactionProcessorMock!!.parseTransaction(any<JsonObject>())).doReturn(mockTransaction)

        assertEquals(mockTransaction, t!!.parseTransaction(null, "{}"))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class, ValidationException::class)
    fun parseTransaction_transactionJSON_validationExceptionOccurs() {
        whenever(transactionProcessorMock!!.parseTransaction(any<JsonObject>())).thenThrow(BurstException.NotValidException(""))

        t!!.parseTransaction(null, "{}")
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class, ValidationException::class)
    fun parseTransaction_transactionJSON_runTimeExceptionOccurs() {
        whenever(transactionProcessorMock!!.parseTransaction(any<JsonObject>())).thenThrow(RuntimeException())

        t!!.parseTransaction(null, "{}")
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun parseTransaction_transactionJSON_parseExceptionTransactionProcessorOccurs() {
        t!!.parseTransaction(null, "badJson")
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun parseTransaction_missingRequiredTransactionBytesOrJson() {
        t!!.parseTransaction(null, null)
    }

    @Test
    @Throws(ParameterException::class)
    fun getAT() {
        val atId = 123L

        val req = QuickMocker.httpServletRequest(
                MockParam(AT_PARAMETER, atId)
        )

        val mockAT = mock<AT>()

        whenever(atServiceMock!!.getAT(eq(atId))).doReturn(mockAT)

        assertEquals(mockAT, t!!.getAT(req))
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAT_missingAT() {
        val req = QuickMocker.httpServletRequest()

        t!!.getAT(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAT_incorrectAT() {
        val req = QuickMocker.httpServletRequest(
                MockParam(AT_PARAMETER, "notLongId")
        )

        t!!.getAT(req)
    }

    @Test(expected = ParameterException::class)
    @Throws(ParameterException::class)
    fun getAT_unknownAT() {
        val atId = 123L

        val req = QuickMocker.httpServletRequest(
                MockParam(AT_PARAMETER, atId)
        )

        whenever(atServiceMock!!.getAT(eq(atId))).doReturn(null)

        t!!.getAT(req)
    }

}
