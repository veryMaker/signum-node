package brs.grpc.handlers

import brs.Account
import brs.Blockchain
import brs.Generator
import brs.crypto.Crypto
import brs.grpc.GrpcApiHandler
import brs.grpc.proto.ApiException
import brs.grpc.proto.BrsApi
import brs.props.PropertyService
import brs.props.Props
import brs.services.AccountService
import burst.kit.crypto.BurstCrypto

class SubmitNonceHandler(propertyService: PropertyService, private val blockchain: Blockchain, private val accountService: AccountService, private val generator: Generator) : GrpcApiHandler<BrsApi.SubmitNonceRequest, BrsApi.SubmitNonceResponse> {
    private val passphrases: Map<Long, String>
    private val allowOtherSoloMiners: Boolean

    init {

        this.passphrases = propertyService.get(Props.SOLO_MINING_PASSPHRASES).associateBy { passphrase -> BurstCrypto.getInstance().getBurstAddressFromPassphrase(passphrase).burstID.signedLongId }
        this.allowOtherSoloMiners = propertyService.get(Props.ALLOW_OTHER_SOLO_MINERS)
    }

    override fun handleRequest(request: BrsApi.SubmitNonceRequest): BrsApi.SubmitNonceResponse {
        var secret: String = request.secretPhrase
        val nonce = request.nonce
        val accountId = request.account
        val submissionHeight = request.blockHeight

        if (submissionHeight != 0 && submissionHeight != blockchain.height + 1) {
            throw ApiException("Given block height does not match current blockchain height")
        }

        if (secret.isEmpty()) {
            if (passphrases.containsKey(accountId)) {
                secret = passphrases[accountId]!!
            } else {
                throw ApiException("Missing Passphrase and account passphrase not in solo mining config")
            }
        }

        if (!allowOtherSoloMiners && !passphrases.containsValue(secret)) {
            throw ApiException("This account is not allowed to mine on this node as the whitelist is enabled and it is not whitelisted.")
        }

        val secretPublicKey = Crypto.getPublicKey(secret)
        val secretAccount = accountService.getAccount(secretPublicKey)
        if (secretAccount != null) {
            verifySecretAccount(accountService, blockchain, secretAccount, accountId)
        }

        val generatorState: Generator.GeneratorState?
        generatorState = if (accountId == 0L || secretAccount == null) {
            generator.addNonce(secret, nonce)
        } else {
            val genAccount = accountService.getAccount(accountId)
            if (genAccount?.publicKey == null) {
                throw ApiException("Passthrough mining requires public key in blockchain")
            } else {
                val publicKey = genAccount.publicKey
                generator.addNonce(secret, nonce, publicKey!!)
            }
        }

        if (generatorState == null) {
            throw ApiException("Failed to create generator")
        }

        return BrsApi.SubmitNonceResponse.newBuilder().setDeadline(generatorState.deadline.longValueExact()).build()
    }

    companion object {

        fun verifySecretAccount(accountService: AccountService, blockchain: Blockchain, secretAccount: Account, accountId: Long) {
            val genAccount = if (accountId != 0L) {
                accountService.getAccount(accountId)
            } else {
                secretAccount
            }

            if (genAccount != null) {
                val assignment = accountService.getRewardRecipientAssignment(genAccount)
                val rewardId = when {
                    assignment == null -> genAccount.id
                    assignment.fromHeight > blockchain.lastBlock.height + 1 -> assignment.prevRecipientId
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
