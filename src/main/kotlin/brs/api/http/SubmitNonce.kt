package brs.api.http

import brs.api.grpc.handlers.SubmitNonceHandler
import brs.api.grpc.service.ApiException
import brs.api.http.common.Parameters.ACCOUNT_ID_PARAMETER
import brs.api.http.common.Parameters.BLOCK_HEIGHT_PARAMETER
import brs.api.http.common.Parameters.NONCE_PARAMETER
import brs.api.http.common.Parameters.SECRET_PHRASE_PARAMETER
import brs.objects.Props
import brs.services.AccountService
import brs.services.BlockchainService
import brs.services.GeneratorService
import brs.services.PropertyService
import brs.util.convert.emptyToNull
import brs.util.convert.parseUnsignedLong
import brs.util.crypto.Crypto
import brs.util.jetty.get
import burst.kit.crypto.BurstCrypto
import burst.kit.entity.BurstAddress
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest


/**
 * TODO
 */
internal class SubmitNonce(
    propertyService: PropertyService,
    private val accountService: AccountService,
    private val blockchainService: BlockchainService,
    private val generatorService: GeneratorService
) : APIServlet.JsonRequestHandler(
    arrayOf(APITag.MINING),
    SECRET_PHRASE_PARAMETER,
    NONCE_PARAMETER,
    ACCOUNT_ID_PARAMETER,
    BLOCK_HEIGHT_PARAMETER
) {
    private val passphrases: Map<Long, String>
    private val allowOtherSoloMiners: Boolean

    init {
        val burstCrypto = BurstCrypto.getInstance()
        this.passphrases = propertyService.get(Props.SOLO_MINING_PASSPHRASES)
            .asSequence()
            .map { burstCrypto.getBurstAddressFromPassphrase(it).burstID.signedLongId to it }
            .toMap()
        this.allowOtherSoloMiners = propertyService.get(Props.ALLOW_OTHER_SOLO_MINERS)
    }

    override fun processRequest(request: HttpServletRequest): JsonElement {
        var secret: String? = request[SECRET_PHRASE_PARAMETER]
        val nonce = request[NONCE_PARAMETER].parseUnsignedLong()

        val accountId = request[ACCOUNT_ID_PARAMETER]

        val submissionHeight = request[BLOCK_HEIGHT_PARAMETER].emptyToNull()

        val response = JsonObject()

        if (submissionHeight != null) {
            try {
                val height = Integer.parseInt(submissionHeight)
                if (height != blockchainService.height + 1) {
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

            secret = passphrases[accountIdLong] ?: run {
                response.addProperty("result", "Missing Passphrase and account passphrase not in solo mining config")
                return response
            }
        }

        if (!allowOtherSoloMiners && !passphrases.containsValue(secret)) {
            response.addProperty(
                "result",
                "This account is not allowed to mine on this node as the whitelist is enabled and it is not whitelisted."
            )
            return response
        }

        val secretPublicKey = Crypto.getPublicKey(secret)
        val secretAccount = accountService.getAccount(secretPublicKey)
        if (secretAccount != null) {
            try {
                SubmitNonceHandler.verifySecretAccount(
                    accountService,
                    blockchainService,
                    secretAccount,
                    accountId.parseUnsignedLong()
                )
            } catch (e: ApiException) {
                response.addProperty("result", e.message)
                return response
            }
        }

        val generatorState = if (accountId == null || secretAccount == null) {
            generatorService.addNonce(secret, nonce)
        } else {
            val genAccount = accountService.getAccount(accountId.parseUnsignedLong())
            if (genAccount?.publicKey == null) {
                response.addProperty("result", "Passthrough mining requires public key in blockchain")
                return response
            } else {
                val publicKey = genAccount.publicKey!!
                generatorService.addNonce(secret, nonce, publicKey)
            }
        }

        response.addProperty("result", "success")
        response.addProperty("deadline", generatorState.deadline)

        return response
    }

    override fun requirePost(): Boolean {
        return true
    }
}
