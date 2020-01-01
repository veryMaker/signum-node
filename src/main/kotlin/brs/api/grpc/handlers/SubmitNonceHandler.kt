package brs.api.grpc.handlers

import brs.api.grpc.GrpcApiHandler
import brs.api.grpc.proto.BrsApi
import brs.api.grpc.service.ApiException
import brs.entity.Account
import brs.entity.DependencyProvider
import brs.objects.Props
import brs.services.AccountService
import brs.services.BlockchainService
import brs.services.GeneratorService
import brs.util.crypto.Crypto
import burst.kit.crypto.BurstCrypto

class SubmitNonceHandler(private val dp: DependencyProvider) :
    GrpcApiHandler<BrsApi.SubmitNonceRequest, BrsApi.SubmitNonceResponse> {
    private val passphrases: Map<Long, String>
    private val allowOtherSoloMiners: Boolean

    init {
        this.passphrases = dp.propertyService.get(Props.SOLO_MINING_PASSPHRASES).associateBy { passphrase ->
            BurstCrypto.getInstance().getBurstAddressFromPassphrase(passphrase).burstID.signedLongId
        }
        this.allowOtherSoloMiners = dp.propertyService.get(Props.ALLOW_OTHER_SOLO_MINERS)
    }

    override fun handleRequest(request: BrsApi.SubmitNonceRequest): BrsApi.SubmitNonceResponse {
        var secret: String = request.secretPhrase
        val nonce = request.nonce
        val accountId = request.account
        val submissionHeight = request.blockHeight

        if (submissionHeight != 0 && submissionHeight != dp.blockchainService.height + 1) {
            throw ApiException("Given block height does not match current blockchain height")
        }

        if (secret.isEmpty()) {
            secret = passphrases[accountId] ?: throw ApiException("Missing Passphrase and account passphrase not in solo mining config")
        }

        if (!allowOtherSoloMiners && !passphrases.containsValue(secret)) {
            throw ApiException("This account is not allowed to mine on this node as the whitelist is enabled and it is not whitelisted.")
        }

        val secretPublicKey = Crypto.getPublicKey(secret)
        val secretAccount = dp.accountService.getAccount(secretPublicKey)
        if (secretAccount != null) {
            verifySecretAccount(dp.accountService, dp.blockchainService, secretAccount, accountId)
        }

        val generatorState: GeneratorService.GeneratorState?
        generatorState = if (accountId == 0L || secretAccount == null) {
            dp.generatorService.addNonce(secret, nonce)
        } else {
            val genAccount = dp.accountService.getAccount(accountId)
            if (genAccount?.publicKey == null) {
                throw ApiException("Passthrough mining requires public key in blockchain")
            } else {
                val publicKey = genAccount.publicKey
                dp.generatorService.addNonce(secret, nonce, publicKey!!)
            }
        }

        return BrsApi.SubmitNonceResponse.newBuilder().setDeadline(generatorState.deadline.longValueExact()).build()
    }

    companion object {
        fun verifySecretAccount(
            accountService: AccountService,
            blockchainService: BlockchainService,
            secretAccount: Account,
            accountId: Long
        ) {
            val genAccount = if (accountId != 0L) {
                accountService.getAccount(accountId)
            } else {
                secretAccount
            }

            if (genAccount != null) {
                val assignment = accountService.getRewardRecipientAssignment(genAccount)
                val rewardId = when {
                    assignment == null -> genAccount.id
                    assignment.fromHeight > blockchainService.lastBlock.height + 1 -> assignment.prevRecipientId
                    else -> assignment.recipientId
                }
                if (rewardId != secretAccount.id) {
                    throw ApiException("Passphrase does not match reward recipient")
                }
            } else {
                throw ApiException("Passphrase is for a different account")
            }
        }
    }
}
