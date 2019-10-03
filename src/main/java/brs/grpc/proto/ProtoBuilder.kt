package brs.grpc.proto

import brs.*
import brs.assetexchange.AssetExchange
import brs.at.AT
import brs.crypto.EncryptedData
import brs.services.AccountService
import brs.services.BlockService
import com.google.protobuf.ByteString
import com.google.protobuf.InvalidProtocolBufferException
import com.google.rpc.Code
import com.google.rpc.Status
import io.grpc.StatusException
import io.grpc.protobuf.StatusProto
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder

fun ByteArray?.toByteString(): ByteString {
    return if (this == null) ByteString.EMPTY else ByteString.copyFrom(this)
}

object ProtoBuilder {
    private val logger = LoggerFactory.getLogger(ProtoBuilder::class.java)

    fun buildError(t: Throwable): StatusException {
        val message = if (t.message == null) "Unknown Error: " + t.javaClass.toString() else t.message
        if (t.message == null) {
            logger.debug("Unknown message for gRPC API exception. Exception:", t)
        }
        return StatusProto.toStatusException(Status.newBuilder().setCode(Code.ABORTED_VALUE).setMessage(message).build())
    }

    fun buildAccount(account: Account, accountService: AccountService): BrsApi.Account {
        return BrsApi.Account.newBuilder()
                .setId(account.id)
                .setPublicKey(account.publicKey.toByteString())
                .setBalance(account.balanceNQT)
                .setUnconfirmedBalance(account.unconfirmedBalanceNQT)
                .setForgedBalance(account.forgedBalanceNQT)
                .setName(account.name)
                .setDescription(account.description)
                .setRewardRecipient(accountService.getRewardRecipientAssignment(account)?.accountId ?: account.id)
                .addAllAssetBalances(accountService.getAssets(account.id, 0, -1).map { buildAssetBalance(it) })
                .build()
    }

    fun buildAssetBalance(asset: Account.AccountAsset): BrsApi.AssetBalance {
        return BrsApi.AssetBalance.newBuilder()
                .setAsset(asset.assetId)
                .setAccount(asset.accountId)
                .setBalance(asset.quantityQNT)
                .setUnconfirmedBalance(asset.unconfirmedQuantityQNT)
                .build()
    }

    fun buildBlock(blockchain: Blockchain, blockService: BlockService, block: Block, includeTransactions: Boolean): BrsApi.Block {
        val builder = BrsApi.Block.newBuilder()
                .setId(block.id)
                .setHeight(block.height)
                .setNumberOfTransactions(block.transactions.size)
                .setTotalAmount(block.totalAmountNQT)
                .setTotalFee(block.totalFeeNQT)
                .setBlockReward(blockService.getBlockReward(block))
                .setPayloadLength(block.payloadLength)
                .setVersion(block.version)
                .setBaseTarget(block.baseTarget)
                .setTimestamp(block.timestamp)
                .addAllTransactionIds(block.transactions.map { it.id })
                .setGenerationSignature(block.generationSignature.toByteString())
                .setBlockSignature(block.blockSignature.toByteString())
                .setPayloadHash(block.payloadHash.toByteString())
                .setGeneratorPublicKey(block.generatorPublicKey.toByteString())
                .setNonce(block.nonce)
                .setScoop(blockService.getScoopNum(block))
                .setPreviousBlockHash(block.previousBlockHash.toByteString())
                .setNextBlockId(block.nextBlockId)

        if (includeTransactions) {
            val currentHeight = blockchain.height
            builder.addAllTransactions(block.transactions
                    .map { transaction -> buildTransaction(transaction, currentHeight) })
        }

        return builder.build()
    }

    fun buildBasicTransaction(transaction: Transaction): BrsApi.BasicTransaction {
        return BrsApi.BasicTransaction.newBuilder()
                .setSenderPublicKey(transaction.senderPublicKey.toByteString())
                .setSenderId(transaction.senderId)
                .setRecipient(transaction.recipientId)
                .setVersion(transaction.version.toInt())
                .setType(transaction.type.type.toInt())
                .setSubtype(transaction.type.subtype.toInt())
                .setAmount(transaction.amountNQT)
                .setFee(transaction.feeNQT)
                .setTimestamp(transaction.timestamp)
                .setDeadline(transaction.deadline.toInt())
                .setReferencedTransactionFullHash(transaction.referencedTransactionFullHash.toByteString())
                .setAttachment(transaction.attachment.protobufMessage)
                .addAllAppendages(transaction.appendages.map { it.protobufMessage })
                .setEcBlockId(transaction.ecBlockId)
                .setEcBlockHeight(transaction.ecBlockHeight)
                .setSignature(transaction.bytes.toByteString())
                .build()
    }

    fun buildTransaction(transaction: Transaction, currentHeight: Int): BrsApi.Transaction {
        return BrsApi.Transaction.newBuilder()
                .setTransaction(buildBasicTransaction(transaction))
                .setId(transaction.id)
                .setTransactionBytes(transaction.bytes.toByteString())
                .setBlock(transaction.blockId)
                .setBlockHeight(transaction.height)
                .setBlockTimestamp(transaction.blockTimestamp)
                .setSignature(transaction.signature.toByteString())
                .setFullHash(transaction.fullHash.toByteString())
                .setConfirmations(currentHeight - transaction.height)
                .build()
    }

    fun buildUnconfirmedTransaction(transaction: Transaction): BrsApi.Transaction {
        return BrsApi.Transaction.newBuilder()
                .setTransaction(buildBasicTransaction(transaction))
                .setId(transaction.id)
                .setTransactionBytes(transaction.bytes.toByteString())
                .setBlockHeight(transaction.height)
                .setSignature(transaction.signature.toByteString())
                .setFullHash(transaction.fullHash.toByteString())
                .build()
    }

    fun buildAT(accountService: AccountService, at: AT): BrsApi.AT {
        val bf = ByteBuffer.allocate(8)
        bf.order(ByteOrder.LITTLE_ENDIAN)
        bf.put(at.creator!!)
        bf.clear()
        val creatorId = bf.long // TODO can this be improved?
        bf.clear()
        bf.put(at.id!!, 0, 8)
        val atId = bf.getLong(0)
        return BrsApi.AT.newBuilder()
                .setId(atId)
                .setCreator(creatorId)
                .setVersion(at.version.toInt())
                .setName(at.name)
                .setDescription(at.description)
                .setMachineCode(at.apCodeBytes.toByteString())
                .setMachineData(at.apDataBytes.toByteString())
                .setBalance(accountService.getAccount(atId)!!.balanceNQT)
                .setPreviousBalance(at.getpBalance())
                .setNextBlock(at.nextHeight())
                .setFrozen(at.freezeOnSameBalance())
                .setRunning(at.machineState.running)
                .setStopped(at.machineState.stopped)
                .setFinished(at.machineState.finished)
                .setDead(at.machineState.dead)
                .setMinActivation(at.minActivationAmount())
                .setCreationBlock(at.creationBlockHeight)
                .build()
    }

    fun buildAlias(alias: Alias, offer: Alias.Offer?): BrsApi.Alias {
        val builder = BrsApi.Alias.newBuilder()
                .setId(alias.id)
                .setOwner(alias.accountId)
                .setName(alias.aliasName)
                .setUri(alias.aliasURI)
                .setTimestamp(alias.timestamp)
                .setOffered(offer != null)

        if (offer != null) {
            builder.price = offer.priceNQT
            builder.buyer = offer.buyerId
        }

        return builder.build()
    }

    fun buildEncryptedData(encryptedData: EncryptedData?): BrsApi.EncryptedData {
        return if (encryptedData == null) BrsApi.EncryptedData.getDefaultInstance() else BrsApi.EncryptedData.newBuilder()
                .setData(encryptedData.data.toByteString())
                .setNonce(encryptedData.nonce.toByteString())
                .build() // TODO is this needed for all methods?
    }

    fun parseEncryptedData(encryptedData: BrsApi.EncryptedData): EncryptedData {
        return EncryptedData(encryptedData.data.toByteArray(), encryptedData.nonce.toByteArray())
    }

    fun sanitizeIndexRange(indexRange: BrsApi.IndexRange): BrsApi.IndexRange {
        val newIndexRange = indexRange.toBuilder()
        if (newIndexRange.firstIndex == 0 && newIndexRange.lastIndex == 0) { // Unset values
            newIndexRange.lastIndex = Integer.MAX_VALUE // Signed :(
        }
        if (newIndexRange.firstIndex < 0 || newIndexRange.lastIndex < 0) {
            newIndexRange.firstIndex = 0
            newIndexRange.lastIndex = 100
        }
        if (newIndexRange.firstIndex > newIndexRange.lastIndex) {
            newIndexRange.firstIndex = newIndexRange.lastIndex
        }
        return newIndexRange.build()
    }

    fun buildAsset(assetExchange: AssetExchange, asset: Asset): BrsApi.Asset {
        return BrsApi.Asset.newBuilder()
                .setAsset(asset.id)
                .setAccount(asset.accountId)
                .setName(asset.name)
                .setDescription(asset.description)
                .setQuantity(asset.quantityQNT)
                .setDecimals(asset.decimals.toInt())
                .setNumberOfTrades(assetExchange.getTradeCount(asset.id))
                .setNumberOfTransfers(assetExchange.getTransferCount(asset.id))
                .setNumberOfAccounts(assetExchange.getAssetAccountsCount(asset.id))
                .build()
    }

    fun buildSubscription(subscription: Subscription): BrsApi.Subscription {
        return BrsApi.Subscription.newBuilder()
                .setId(subscription.id!!)
                .setSender(subscription.senderId!!)
                .setRecipient(subscription.recipientId!!)
                .setAmount(subscription.amountNQT!!)
                .setFrequency(subscription.frequency)
                .setTimeNext(subscription.timeNext)
                .build()
    }

    fun buildOrder(order: Order): BrsApi.Order {
        return BrsApi.Order.newBuilder()
                .setId(order.id)
                .setAsset(order.assetId)
                .setAccount(order.accountId)
                .setQuantity(order.quantityQNT)
                .setPrice(order.priceNQT)
                .setHeight(order.height)
                .setType(order.protobufType)
                .build()
    }

    fun buildGoods(goods: DigitalGoodsStore.Goods): BrsApi.DgsGood {
        return BrsApi.DgsGood.newBuilder()
                .setId(goods.id)
                .setSeller(goods.sellerId)
                .setPrice(goods.priceNQT)
                .setQuantity(goods.quantity.toLong())
                .setIsDelisted(goods.isDelisted)
                .setTimestamp(goods.timestamp)
                .setName(goods.name)
                .setDescription(goods.description)
                .setTags(goods.tags)
                .build()
    }

    fun buildEscrowTransaction(escrow: Escrow): BrsApi.EscrowTransaction {
        return BrsApi.EscrowTransaction.newBuilder()
                .setEscrowId(escrow.id!!)
                .setSender(escrow.senderId!!)
                .setRecipient(escrow.recipientId!!)
                .setAmount(escrow.amountNQT!!)
                .setRequiredSigners(escrow.requiredSigners)
                .setDeadline(escrow.deadline)
                .setDeadlineAction(Escrow.decisionToProtobuf(escrow.deadlineAction))
                .build()
    }

    fun buildTrade(trade: Trade, asset: Asset): BrsApi.AssetTrade {
        return BrsApi.AssetTrade.newBuilder()
                .setAsset(trade.assetId)
                .setTradeType(if (trade.isBuy) BrsApi.AssetTradeType.BUY else BrsApi.AssetTradeType.SELL)
                .setSeller(trade.sellerId)
                .setBuyer(trade.buyerId)
                .setPrice(trade.priceNQT)
                .setQuantity(trade.quantityQNT)
                .setAskOrder(trade.askOrderId)
                .setBidOrder(trade.bidOrderId)
                .setAskOrderHeight(trade.askOrderHeight)
                .setBidOrderHeight(trade.bidOrderHeight)
                .setBlock(trade.blockId)
                .setHeight(trade.height)
                .setTimestamp(trade.timestamp)
                .setAssetName(asset.name)
                .setAssetDescription(asset.description)
                .build()
    }

    fun buildTransfer(assetTransfer: AssetTransfer, asset: Asset): BrsApi.AssetTransfer {
        return BrsApi.AssetTransfer.newBuilder()
                .setId(assetTransfer.id)
                .setAsset(assetTransfer.assetId)
                .setSender(assetTransfer.senderId)
                .setRecipient(assetTransfer.recipientId)
                .setQuantity(assetTransfer.quantityQNT)
                .setHeight(assetTransfer.height)
                .setTimestamp(assetTransfer.timestamp)
                .setAssetName(asset.name)
                .setAssetDescription(asset.description)
                .build()
    }

    fun buildPurchase(purchase: DigitalGoodsStore.Purchase, goods: DigitalGoodsStore.Goods): BrsApi.DgsPurchase {
        return BrsApi.DgsPurchase.newBuilder()
                .setId(purchase.id)
                .setGood(purchase.goodsId)
                .setSeller(purchase.sellerId)
                .setBuyer(purchase.buyerId)
                .setPrice(purchase.priceNQT)
                .setQuantity(purchase.quantity.toLong())
                .setTimestamp(purchase.timestamp)
                .setDeliveryDeadlineTimestamp(purchase.deliveryDeadlineTimestamp)
                .setGoodName(goods.name)
                .setGoodDescription(goods.description)
                .setNote(buildEncryptedData(purchase.note))
                .setIsPending(purchase.isPending)
                .setDeliveredData(buildEncryptedData(purchase.encryptedGoods))
                .setDeliveredDataIsText(purchase.goodsIsText())
                .addAllFeedback(purchase.feedbackNotes?.map { buildEncryptedData(it) } ?: emptyList())
                .addAllPublicFeedback(purchase.publicFeedback)
                .setRefundNote(buildEncryptedData(purchase.refundNote))
                .setDiscount(purchase.discountNQT)
                .setRefund(purchase.refundNQT)
                .build()
    }

    fun parseBasicTransaction(dp: DependencyProvider, basicTransaction: BrsApi.BasicTransaction): Transaction {
        try {
            val transactionBuilder = Transaction.Builder(dp, basicTransaction.version.toByte(), basicTransaction.senderPublicKey.toByteArray(), basicTransaction.amount, basicTransaction.fee, basicTransaction.timestamp, basicTransaction.deadline.toShort(), Attachment.AbstractAttachment.parseProtobufMessage(dp, basicTransaction.attachment))
                    .senderId(basicTransaction.senderId)
                    .recipientId(basicTransaction.recipient)

            if (basicTransaction.referencedTransactionFullHash.size() > 0) {
                transactionBuilder.referencedTransactionFullHash(basicTransaction.referencedTransactionFullHash.toByteArray())
            }

            val blockchainHeight = dp.blockchain.height

            for (appendix in basicTransaction.appendagesList) {
                try {
                    when {
                        appendix.`is`(BrsApi.MessageAppendix::class.java) -> transactionBuilder.message(Appendix.Message(dp, appendix.unpack(BrsApi.MessageAppendix::class.java), blockchainHeight))
                        appendix.`is`(BrsApi.EncryptedMessageAppendix::class.java) -> {
                            val encryptedMessageAppendix = appendix.unpack(BrsApi.EncryptedMessageAppendix::class.java)
                            when (encryptedMessageAppendix.type) {
                                BrsApi.EncryptedMessageAppendix.Type.TO_RECIPIENT -> transactionBuilder.encryptedMessage(Appendix.EncryptedMessage(dp, encryptedMessageAppendix, blockchainHeight))
                                BrsApi.EncryptedMessageAppendix.Type.TO_SELF -> transactionBuilder.encryptToSelfMessage(Appendix.EncryptToSelfMessage(dp, encryptedMessageAppendix, blockchainHeight))
                                else -> throw ApiException("Invalid encrypted message type: " + encryptedMessageAppendix.type.name)
                            }
                        }
                        appendix.`is`(BrsApi.PublicKeyAnnouncementAppendix::class.java) -> transactionBuilder.publicKeyAnnouncement(Appendix.PublicKeyAnnouncement(dp, appendix.unpack(BrsApi.PublicKeyAnnouncementAppendix::class.java), blockchainHeight))
                    }
                } catch (e: InvalidProtocolBufferException) {
                    throw ApiException("Failed to unpack Any: " + e.message)
                }

            }
            return transactionBuilder.build()
        } catch (e: BurstException.NotValidException) {
            throw ApiException("Transaction not valid: " + e.message)
        } catch (e: InvalidProtocolBufferException) {
            throw ApiException("Could not parse an Any: " + e.message)
        }
    }
}
