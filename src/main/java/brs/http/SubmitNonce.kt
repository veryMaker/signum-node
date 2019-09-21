package brs.http

import brs.Blockchain
import brs.Generator
import brs.crypto.Crypto
import brs.grpc.handlers.SubmitNonceHandler
import brs.grpc.proto.ApiException
import brs.http.common.Parameters.ACCOUNT_ID_PARAMETER
import brs.http.common.Parameters.BLOCK_HEIGHT_PARAMETER
import brs.http.common.Parameters.NONCE_PARAMETER
import brs.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.props.PropertyService
import brs.props.Props
import brs.services.AccountService
import brs.util.Convert
import brs.util.parseUnsignedLong
import burst.kit.crypto.BurstCrypto
import burst.kit.entity.BurstAddress
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest


internal class SubmitNonce(propertyService: PropertyService, private val accountService: AccountService, private val blockchain: Blockchain, private val generator: Generator) : APIServlet.JsonRequestHandler(arrayOf(APITag.MINING), SECRET_PHRASE_PARAMETER, NONCE_PARAMETER, ACCOUNT_ID_PARAMETER, BLOCK_HEIGHT_PARAMETER) {

    private val passphrases: Map<Long, String>
    private val allowOtherSoloMiners: Boolean

    init {
        val burstCrypto = BurstCrypto.getInstance()
        this.passphrases = propertyService.get(Props.SOLO_MINING_PASSPHRASES)
                .map { burstCrypto.getBurstAddressFromPassphrase(it).burstID.signedLongId to it}
                .toMap()
        this.allowOtherSoloMiners = propertyService.get(Props.ALLOW_OTHER_SOLO_MINERS)
    }

    internal override fun processRequest(request: HttpServletRequest): JsonElement {
        var secret: String? = request.getParameter(SECRET_PHRASE_PARAMETER)
        val nonce = request.getParameter(NONCE_PARAMETER).parseUnsignedLong()

        val accountId = request.getParameter(ACCOUNT_ID_PARAMETER)

        val submissionHeight = Convert.emptyToNull(request.getParameter(BLOCK_HEIGHT_PARAMETER))

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

        if (secret == null || secret.isEmpty()) {
            val accountIdLong: Long
            try {
                accountIdLong = BurstAddress.fromEither(accountId).burstID.signedLongId
            } catch (e: Exception) {
                response.addProperty("result", "Missing Passphrase and Account ID is malformed")
                return response
            }

            if (passphrases.containsKey(accountIdLong)) {
                secret = passphrases[accountIdLong]!!
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
                SubmitNonceHandler.verifySecretAccount(accountService, blockchain, secretAccount, accountId.parseUnsignedLong())
            } catch (e: ApiException) {
                response.addProperty("result", e.message)
                return response
            }

        }

        val generatorState = if (accountId == null || secretAccount == null) {
            generator.addNonce(secret!!, nonce)
        } else {
            val genAccount = accountService.getAccount(accountId.parseUnsignedLong())
            if (genAccount?.publicKey == null) {
                response.addProperty("result", "Passthrough mining requires public key in blockchain")
                return response
            } else {
                val publicKey = genAccount.publicKey!!
                generator.addNonce(secret!!, nonce, publicKey)
            }
        }

        response.addProperty("result", "success")
        response.addProperty("deadline", generatorState.deadline)

        return response
    }

    internal override fun requirePost(): Boolean {
        return true
    }
}
