package brs.http

import brs.Account
import brs.Blockchain
import brs.Generator
import brs.crypto.Crypto
import brs.grpc.handlers.SubmitNonceHandler
import brs.grpc.proto.ApiException
import brs.props.PropertyService
import brs.props.Props
import brs.services.AccountService
import brs.util.Convert
import burst.kit.crypto.BurstCrypto
import burst.kit.entity.BurstAddress
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import javax.servlet.http.HttpServletRequest
import java.util.Objects
import java.util.function.Function
import java.util.stream.Collectors

import brs.http.common.Parameters.*
import java.util.stream.Collector


internal class SubmitNonce(propertyService: PropertyService, private val accountService: AccountService, private val blockchain: Blockchain, private val generator: Generator) : APIServlet.JsonRequestHandler(arrayOf(APITag.MINING), SECRET_PHRASE_PARAMETER, NONCE_PARAMETER, ACCOUNT_ID_PARAMETER, BLOCK_HEIGHT_PARAMETER) {

    private val passphrases: Map<Long, String>
    private val allowOtherSoloMiners: Boolean

    init {
        val burstCrypto = BurstCrypto.getInstance()
        this.passphrases = propertyService.get(Props.SOLO_MINING_PASSPHRASES)
                .map { burstCrypto.getBurstAddressFromPassphrase(it).burstID.signedLongId to it}
                .toMap()
        this.allowOtherSoloMiners = propertyService.get(Props.ALLOW_OTHER_SOLO_MINERS)!!
    }

    internal override fun processRequest(req: HttpServletRequest): JsonElement {
        var secret: String? = req.getParameter(SECRET_PHRASE_PARAMETER)
        val nonce = Convert.parseUnsignedLong(req.getParameter(NONCE_PARAMETER))

        val accountId = req.getParameter(ACCOUNT_ID_PARAMETER)

        val submissionHeight = Convert.emptyToNull(req.getParameter(BLOCK_HEIGHT_PARAMETER))

        val response = JsonObject()

        if (submissionHeight != null) {
            try {
                val height = Integer.parseInt(submissionHeight)
                if (height != blockchain.height + 1) {
                    response.addProperty("result", "Given block height does not match current blockchain height")
                    return response
                }
            } catch (e: NumberFormatException) {
                response.addProperty("result", "Given block height is not a number")
                return response
            }

        }

        if (secret == null || secret == "") {
            val accountIdLong: Long
            try {
                accountIdLong = BurstAddress.fromEither(accountId).burstID.signedLongId
            } catch (e: Exception) {
                response.addProperty("result", "Missing Passphrase and Account ID is malformed")
                return response
            }

            if (passphrases.containsKey(accountIdLong)) {
                secret = passphrases[accountIdLong]
            } else {
                response.addProperty("result", "Missing Passphrase and account passphrase not in solo mining config")
                return response
            }
        }

        if (!allowOtherSoloMiners && !passphrases.containsValue(secret)) {
            response.addProperty("result", "This account is not allowed to mine on this node as the whitelist is enabled and it is not whitelisted.")
            return response
        }

        val secretPublicKey = Crypto.getPublicKey(secret)
        val secretAccount = accountService.getAccount(secretPublicKey)
        if (secretAccount != null) {
            try {
                SubmitNonceHandler.verifySecretAccount(accountService, blockchain, secretAccount, Convert.parseUnsignedLong(accountId))
            } catch (e: ApiException) {
                response.addProperty("result", e.message)
                return response
            }

        }

        var generatorState: Generator.GeneratorState? = null
        if (accountId == null || secretAccount == null) {
            generatorState = generator.addNonce(secret!!, nonce)
        } else {
            val genAccount = accountService.getAccount(Convert.parseUnsignedLong(accountId))
            if (genAccount == null || genAccount.publicKey == null) {
                response.addProperty("result", "Passthrough mining requires public key in blockchain")
            } else {
                val publicKey = genAccount.publicKey
                generatorState = generator.addNonce(secret!!, nonce, publicKey)
            }
        }

        if (generatorState == null) {
            response.addProperty("result", "failed to create generator")
            return response
        }

        response.addProperty("result", "success")
        response.addProperty("deadline", generatorState.deadline)

        return response
    }

    internal override fun requirePost(): Boolean {
        return true
    }
}
