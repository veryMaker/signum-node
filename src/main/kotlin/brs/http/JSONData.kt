package brs.http

import brs.*
import brs.Alias.Offer
import brs.at.AT
import brs.at.AtApiHelper
import brs.crypto.Crypto
import brs.crypto.EncryptedData
import brs.http.common.Parameters.FULL_HASH_RESPONSE
import brs.http.common.ResultFields.ACCOUNT_RESPONSE
import brs.http.common.ResultFields.ALIAS_NAME_RESPONSE
import brs.http.common.ResultFields.ALIAS_RESPONSE
import brs.http.common.ResultFields.ALIAS_URI_RESPONSE
import brs.http.common.ResultFields.AMOUNT_PLANCK_RESPONSE
import brs.http.common.ResultFields.ASK_ORDER_HEIGHT_RESPONSE
import brs.http.common.ResultFields.ASK_ORDER_RESPONSE
import brs.http.common.ResultFields.ASSET_RESPONSE
import brs.http.common.ResultFields.ASSET_TRANSFER_RESPONSE
import brs.http.common.ResultFields.ATTACHMENT_RESPONSE
import brs.http.common.ResultFields.BALANCE_PLANCK_RESPONSE
import brs.http.common.ResultFields.BASE_TARGET_RESPONSE
import brs.http.common.ResultFields.BID_ORDER_HEIGHT_RESPONSE
import brs.http.common.ResultFields.BID_ORDER_RESPONSE
import brs.http.common.ResultFields.BLOCK_RESPONSE
import brs.http.common.ResultFields.BLOCK_REWARD_RESPONSE
import brs.http.common.ResultFields.BLOCK_SIGNATURE_RESPONSE
import brs.http.common.ResultFields.BLOCK_TIMESTAMP_RESPONSE
import brs.http.common.ResultFields.BUYER_RESPONSE
import brs.http.common.ResultFields.CONFIRMATIONS_RESPONSE
import brs.http.common.ResultFields.DATA_RESPONSE
import brs.http.common.ResultFields.DEADLINE_ACTION_RESPONSE
import brs.http.common.ResultFields.DEADLINE_RESPONSE
import brs.http.common.ResultFields.DECIMALS_RESPONSE
import brs.http.common.ResultFields.DECISION_RESPONSE
import brs.http.common.ResultFields.DELISTED_RESPONSE
import brs.http.common.ResultFields.DELIVERY_DEADLINE_TIMESTAMP_RESPONSE
import brs.http.common.ResultFields.DESCRIPTION_RESPONSE
import brs.http.common.ResultFields.DISCOUNT_PLANCK_RESPONSE
import brs.http.common.ResultFields.EC_BLOCK_HEIGHT_RESPONSE
import brs.http.common.ResultFields.EC_BLOCK_ID_RESPONSE
import brs.http.common.ResultFields.EFFECTIVE_BALANCE_PLANCK_RESPONSE
import brs.http.common.ResultFields.FEEDBACK_NOTES_RESPONSE
import brs.http.common.ResultFields.FEE_PLANCK_RESPONSE
import brs.http.common.ResultFields.FORGED_BALANCE_PLANCK_RESPONSE
import brs.http.common.ResultFields.FREQUENCY_RESPONSE
import brs.http.common.ResultFields.GENERATION_SIGNATURE_RESPONSE
import brs.http.common.ResultFields.GENERATOR_PUBLIC_KEY_RESPONSE
import brs.http.common.ResultFields.GENERATOR_RESPONSE
import brs.http.common.ResultFields.GOODS_DATA_RESPONSE
import brs.http.common.ResultFields.GOODS_IS_TEXT_RESPONSE
import brs.http.common.ResultFields.GOODS_RESPONSE
import brs.http.common.ResultFields.GUARANTEED_BALANCE_PLANCK_RESPONSE
import brs.http.common.ResultFields.HEIGHT_RESPONSE
import brs.http.common.ResultFields.ID_RESPONSE
import brs.http.common.ResultFields.ID_RS_RESPONSE
import brs.http.common.ResultFields.NAME_RESPONSE
import brs.http.common.ResultFields.NEXT_BLOCK_RESPONSE
import brs.http.common.ResultFields.NONCE_RESPONSE
import brs.http.common.ResultFields.NOTE_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_ACCOUNTS_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_TRADES_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_TRANSACTIONS_RESPONSE
import brs.http.common.ResultFields.NUMBER_OF_TRANSFERS_RESPONSE
import brs.http.common.ResultFields.ORDER_RESPONSE
import brs.http.common.ResultFields.PAYLOAD_HASH_RESPONSE
import brs.http.common.ResultFields.PAYLOAD_LENGTH_RESPONSE
import brs.http.common.ResultFields.PENDING_RESPONSE
import brs.http.common.ResultFields.PREVIOUS_BLOCK_HASH_RESPONSE
import brs.http.common.ResultFields.PREVIOUS_BLOCK_RESPONSE
import brs.http.common.ResultFields.PRICE_PLANCK_RESPONSE
import brs.http.common.ResultFields.PUBLIC_FEEDBACKS_RESPONSE
import brs.http.common.ResultFields.PURCHASE_RESPONSE
import brs.http.common.ResultFields.QUANTITY_QNT_RESPONSE
import brs.http.common.ResultFields.QUANTITY_RESPONSE
import brs.http.common.ResultFields.RECIPIENT_RESPONSE
import brs.http.common.ResultFields.RECIPIENT_RS_RESPONSE
import brs.http.common.ResultFields.REFERENCED_TRANSACTION_FULL_HASH_RESPONSE
import brs.http.common.ResultFields.REFUND_NOTE_RESPONSE
import brs.http.common.ResultFields.REFUND_PLANCK_RESPONSE
import brs.http.common.ResultFields.REQUIRED_SIGNERS_RESPONSE
import brs.http.common.ResultFields.SCOOP_NUM_RESPONSE
import brs.http.common.ResultFields.SELLER_RESPONSE
import brs.http.common.ResultFields.SENDER_PUBLIC_KEY_RESPONSE
import brs.http.common.ResultFields.SENDER_RESPONSE
import brs.http.common.ResultFields.SENDER_RS_RESPONSE
import brs.http.common.ResultFields.SIGNATURE_HASH_RESPONSE
import brs.http.common.ResultFields.SIGNATURE_RESPONSE
import brs.http.common.ResultFields.SIGNERS_RESPONSE
import brs.http.common.ResultFields.SUBTYPE_RESPONSE
import brs.http.common.ResultFields.TAGS_RESPONSE
import brs.http.common.ResultFields.TIMESTAMP_RESPONSE
import brs.http.common.ResultFields.TIME_NEXT_RESPONSE
import brs.http.common.ResultFields.TOTAL_AMOUNT_PLANCK_RESPONSE
import brs.http.common.ResultFields.TOTAL_FEE_PLANCK_RESPONSE
import brs.http.common.ResultFields.TRADE_TYPE_RESPONSE
import brs.http.common.ResultFields.TRANSACTIONS_RESPONSE
import brs.http.common.ResultFields.TRANSACTION_RESPONSE
import brs.http.common.ResultFields.TYPE_RESPONSE
import brs.http.common.ResultFields.UNCONFIRMED_BALANCE_PLANCK_RESPONSE
import brs.http.common.ResultFields.UNCONFIRMED_QUANTITY_RESPONSE
import brs.http.common.ResultFields.VERSION_RESPONSE
import brs.peer.Peer
import brs.services.AccountService
import brs.util.addAll
import brs.util.convert.emptyToNull
import brs.util.convert.rsAccount
import brs.util.convert.toHexString
import brs.util.convert.toUnsignedString
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.nio.ByteBuffer
import java.nio.ByteOrder

object JSONData {

    internal fun alias(alias: Alias, offer: Offer?): JsonObject {
        val json = JsonObject()
        putAccount(json, ACCOUNT_RESPONSE, alias.accountId)
        json.addProperty(ALIAS_NAME_RESPONSE, alias.aliasName)
        json.addProperty(ALIAS_URI_RESPONSE, alias.aliasURI)
        json.addProperty(TIMESTAMP_RESPONSE, alias.timestamp)
        json.addProperty(ALIAS_RESPONSE, alias.id.toUnsignedString())

        if (offer != null) {
            json.addProperty(PRICE_PLANCK_RESPONSE, offer.pricePlanck.toString())
            if (offer.buyerId != 0L) {
                json.addProperty(BUYER_RESPONSE, offer.buyerId.toUnsignedString())
            }
        }
        return json
    }

    internal fun accountBalance(account: Account?): JsonObject {
        val json = JsonObject()
        if (account == null) {
            json.addProperty(BALANCE_PLANCK_RESPONSE, "0")
            json.addProperty(UNCONFIRMED_BALANCE_PLANCK_RESPONSE, "0")
            json.addProperty(EFFECTIVE_BALANCE_PLANCK_RESPONSE, "0")
            json.addProperty(FORGED_BALANCE_PLANCK_RESPONSE, "0")
            json.addProperty(GUARANTEED_BALANCE_PLANCK_RESPONSE, "0")
        } else {
            json.addProperty(BALANCE_PLANCK_RESPONSE, account.balancePlanck.toString())
            json.addProperty(UNCONFIRMED_BALANCE_PLANCK_RESPONSE, account.unconfirmedBalancePlanck.toString())
            json.addProperty(EFFECTIVE_BALANCE_PLANCK_RESPONSE, account.balancePlanck.toString())
            json.addProperty(FORGED_BALANCE_PLANCK_RESPONSE, account.forgedBalancePlanck.toString())
            json.addProperty(GUARANTEED_BALANCE_PLANCK_RESPONSE, account.balancePlanck.toString())
        }
        return json
    }

    internal fun asset(asset: Asset, tradeCount: Int, transferCount: Int, assetAccountsCount: Int): JsonObject {
        val json = JsonObject()
        putAccount(json, ACCOUNT_RESPONSE, asset.accountId)
        json.addProperty(NAME_RESPONSE, asset.name)
        json.addProperty(DESCRIPTION_RESPONSE, asset.description)
        json.addProperty(DECIMALS_RESPONSE, asset.decimals)
        json.addProperty(QUANTITY_QNT_RESPONSE, asset.quantity.toString())
        json.addProperty(ASSET_RESPONSE, asset.id.toUnsignedString())
        json.addProperty(NUMBER_OF_TRADES_RESPONSE, tradeCount)
        json.addProperty(NUMBER_OF_TRANSFERS_RESPONSE, transferCount)
        json.addProperty(NUMBER_OF_ACCOUNTS_RESPONSE, assetAccountsCount)
        return json
    }

    internal fun accountAsset(accountAsset: Account.AccountAsset): JsonObject {
        val json = JsonObject()
        putAccount(json, ACCOUNT_RESPONSE, accountAsset.accountId)
        json.addProperty(ASSET_RESPONSE, accountAsset.assetId.toUnsignedString())
        json.addProperty(QUANTITY_QNT_RESPONSE, accountAsset.quantity.toString())
        json.addProperty(UNCONFIRMED_QUANTITY_RESPONSE, accountAsset.unconfirmedQuantity.toString())
        return json
    }

    internal fun askOrder(order: Order.Ask): JsonObject {
        val json = order(order)
        json.addProperty(TYPE_RESPONSE, "ask")
        return json
    }

    internal fun bidOrder(order: Order.Bid): JsonObject {
        val json = order(order)
        json.addProperty(TYPE_RESPONSE, "bid")
        return json
    }

    private fun order(order: Order): JsonObject {
        val json = JsonObject()
        json.addProperty(ORDER_RESPONSE, order.id.toUnsignedString())
        json.addProperty(ASSET_RESPONSE, order.assetId.toUnsignedString())
        putAccount(json, ACCOUNT_RESPONSE, order.accountId)
        json.addProperty(QUANTITY_QNT_RESPONSE, order.quantity.toString())
        json.addProperty(PRICE_PLANCK_RESPONSE, order.pricePlanck.toString())
        json.addProperty(HEIGHT_RESPONSE, order.height)
        return json
    }

    internal fun block(block: Block, includeTransactions: Boolean, currentBlockchainHeight: Int, blockReward: Long, scoopNum: Int): JsonObject {
        val json = JsonObject()
        json.addProperty(BLOCK_RESPONSE, block.stringId)
        json.addProperty(HEIGHT_RESPONSE, block.height)
        putAccount(json, GENERATOR_RESPONSE, block.generatorId)
        json.addProperty(GENERATOR_PUBLIC_KEY_RESPONSE, block.generatorPublicKey.toHexString())
        json.addProperty(NONCE_RESPONSE, block.nonce.toUnsignedString())
        json.addProperty(SCOOP_NUM_RESPONSE, scoopNum)
        json.addProperty(TIMESTAMP_RESPONSE, block.timestamp)
        json.addProperty(NUMBER_OF_TRANSACTIONS_RESPONSE, block.transactions.size)
        json.addProperty(TOTAL_AMOUNT_PLANCK_RESPONSE, block.totalAmountPlanck.toString())
        json.addProperty(TOTAL_FEE_PLANCK_RESPONSE, block.totalFeePlanck.toString())
        json.addProperty(BLOCK_REWARD_RESPONSE, (blockReward / Constants.ONE_BURST).toUnsignedString())
        json.addProperty(PAYLOAD_LENGTH_RESPONSE, block.payloadLength)
        json.addProperty(VERSION_RESPONSE, block.version)
        json.addProperty(BASE_TARGET_RESPONSE, block.baseTarget.toUnsignedString())

        if (block.previousBlockId != 0L) {
            json.addProperty(PREVIOUS_BLOCK_RESPONSE, block.previousBlockId.toUnsignedString())
        }

        if (block.nextBlockId != 0L) {
            json.addProperty(NEXT_BLOCK_RESPONSE, block.nextBlockId.toUnsignedString())
        }

        json.addProperty(PAYLOAD_HASH_RESPONSE, block.payloadHash.toHexString())
        json.addProperty(GENERATION_SIGNATURE_RESPONSE, block.generationSignature.toHexString())

        if (block.version > 1) {
            json.addProperty(PREVIOUS_BLOCK_HASH_RESPONSE, block.previousBlockHash?.toHexString() ?: "")
        }

        json.addProperty(BLOCK_SIGNATURE_RESPONSE, block.blockSignature.toHexString())

        val transactions = JsonArray()
        for (transaction in block.transactions) {
            if (includeTransactions) {
                transactions.add(transaction(transaction, currentBlockchainHeight))
            } else {
                transactions.add(transaction.id.toUnsignedString())
            }
        }
        json.add(TRANSACTIONS_RESPONSE, transactions)
        return json
    }

    internal fun encryptedData(encryptedData: EncryptedData): JsonObject {
        val json = JsonObject()
        json.addProperty(DATA_RESPONSE, encryptedData.data.toHexString())
        json.addProperty(NONCE_RESPONSE, encryptedData.nonce.toHexString())
        return json
    }

    internal fun escrowTransaction(escrow: Escrow): JsonObject {
        val json = JsonObject()
        json.addProperty(ID_RESPONSE, escrow.id.toUnsignedString())
        json.addProperty(SENDER_RESPONSE, escrow.senderId.toUnsignedString())
        json.addProperty(SENDER_RS_RESPONSE, escrow.senderId.rsAccount())
        json.addProperty(RECIPIENT_RESPONSE, escrow.recipientId.toUnsignedString())
        json.addProperty(RECIPIENT_RS_RESPONSE, escrow.recipientId.rsAccount())
        json.addProperty(AMOUNT_PLANCK_RESPONSE, escrow.amountPlanck.toUnsignedString())
        json.addProperty(REQUIRED_SIGNERS_RESPONSE, escrow.requiredSigners)
        json.addProperty(DEADLINE_RESPONSE, escrow.deadline)
        json.addProperty(DEADLINE_ACTION_RESPONSE, Escrow.decisionToString(escrow.deadlineAction))

        val signers = JsonArray()
        for (decision in escrow.decisions) {
            if (decision.accountId == escrow.senderId || decision.accountId == escrow.recipientId) {
                continue
            }
            val signerDetails = JsonObject()
            signerDetails.addProperty(ID_RESPONSE, decision.accountId!!.toUnsignedString())
            signerDetails.addProperty(ID_RS_RESPONSE, decision.accountId.rsAccount())
            signerDetails.addProperty(DECISION_RESPONSE, Escrow.decisionToString(decision.decision!!))
            signers.add(signerDetails)
        }
        json.add(SIGNERS_RESPONSE, signers)
        return json
    }

    internal fun goods(goods: DigitalGoodsStore.Goods): JsonObject {
        val json = JsonObject()
        json.addProperty(GOODS_RESPONSE, goods.id.toUnsignedString())
        json.addProperty(NAME_RESPONSE, goods.name)
        json.addProperty(DESCRIPTION_RESPONSE, goods.description)
        json.addProperty(QUANTITY_RESPONSE, goods.quantity)
        json.addProperty(PRICE_PLANCK_RESPONSE, goods.pricePlanck.toString())
        putAccount(json, SELLER_RESPONSE, goods.sellerId)
        json.addProperty(TAGS_RESPONSE, goods.tags)
        json.addProperty(DELISTED_RESPONSE, goods.isDelisted)
        json.addProperty(TIMESTAMP_RESPONSE, goods.timestamp)
        return json
    }

    internal fun peer(peer: Peer): JsonObject {
        val json = JsonObject()
        json.addProperty("state", peer.state.ordinal)
        json.addProperty("announcedAddress", peer.announcedAddress)
        json.addProperty("shareAddress", peer.shareAddress)
        json.addProperty("downloadedVolume", peer.downloadedVolume)
        json.addProperty("uploadedVolume", peer.uploadedVolume)
        json.addProperty("application", peer.application)
        json.addProperty("version", peer.version.toStringIfNotEmpty())
        json.addProperty("platform", peer.platform)
        json.addProperty("blacklisted", peer.isBlacklisted)
        json.addProperty("lastUpdated", peer.lastUpdated)
        return json
    }

    internal fun purchase(purchase: DigitalGoodsStore.Purchase): JsonObject {
        val json = JsonObject()
        json.addProperty(PURCHASE_RESPONSE, purchase.id.toUnsignedString())
        json.addProperty(GOODS_RESPONSE, purchase.goodsId.toUnsignedString())
        json.addProperty(NAME_RESPONSE, purchase.getName())
        putAccount(json, SELLER_RESPONSE, purchase.sellerId)
        json.addProperty(PRICE_PLANCK_RESPONSE, purchase.pricePlanck.toString())
        json.addProperty(QUANTITY_RESPONSE, purchase.quantity)
        putAccount(json, BUYER_RESPONSE, purchase.buyerId)
        json.addProperty(TIMESTAMP_RESPONSE, purchase.timestamp)
        json.addProperty(DELIVERY_DEADLINE_TIMESTAMP_RESPONSE, purchase.deliveryDeadlineTimestamp)
        if (purchase.note != null) {
            json.add(NOTE_RESPONSE, encryptedData(purchase.note))
        }
        json.addProperty(PENDING_RESPONSE, purchase.isPending)
        if (purchase.encryptedGoods != null) {
            json.add(GOODS_DATA_RESPONSE, encryptedData(purchase.encryptedGoods!!))
            json.addProperty(GOODS_IS_TEXT_RESPONSE, purchase.goodsIsText())
        }
        if (purchase.feedbackNotes != null) {
            val feedbacks = JsonArray()
            for (encryptedData in purchase.feedbackNotes!!) {
                feedbacks.add(encryptedData(encryptedData))
            }
            json.add(FEEDBACK_NOTES_RESPONSE, feedbacks)
        }
        val publicFeedback = purchase.getPublicFeedback()
        if (publicFeedback != null && publicFeedback.isNotEmpty()) {
            val publicFeedbacks = JsonArray()
            for (string in publicFeedback) {
                publicFeedbacks.add(string)
            }
            json.add(PUBLIC_FEEDBACKS_RESPONSE, publicFeedbacks)
        }
        if (purchase.refundNote != null) {
            json.add(REFUND_NOTE_RESPONSE, encryptedData(purchase.refundNote!!))
        }
        if (purchase.discountPlanck > 0) {
            json.addProperty(DISCOUNT_PLANCK_RESPONSE, purchase.discountPlanck.toString())
        }
        if (purchase.refundPlanck > 0) {
            json.addProperty(REFUND_PLANCK_RESPONSE, purchase.refundPlanck.toString())
        }
        return json
    }

    internal fun subscription(subscription: Subscription): JsonObject {
        val json = JsonObject()
        json.addProperty(ID_RESPONSE, subscription.id.toUnsignedString())
        putAccount(json, SENDER_RESPONSE, subscription.senderId)
        putAccount(json, RECIPIENT_RESPONSE, subscription.recipientId)
        json.addProperty(AMOUNT_PLANCK_RESPONSE, subscription.amountPlanck.toUnsignedString())
        json.addProperty(FREQUENCY_RESPONSE, subscription.frequency)
        json.addProperty(TIME_NEXT_RESPONSE, subscription.timeNext)
        return json
    }

    internal fun trade(trade: Trade, asset: Asset?): JsonObject {
        val json = JsonObject()
        json.addProperty(TIMESTAMP_RESPONSE, trade.timestamp)
        json.addProperty(QUANTITY_QNT_RESPONSE, trade.quantity.toString())
        json.addProperty(PRICE_PLANCK_RESPONSE, trade.pricePlanck.toString())
        json.addProperty(ASSET_RESPONSE, trade.assetId.toUnsignedString())
        json.addProperty(ASK_ORDER_RESPONSE, trade.askOrderId.toUnsignedString())
        json.addProperty(BID_ORDER_RESPONSE, trade.bidOrderId.toUnsignedString())
        json.addProperty(ASK_ORDER_HEIGHT_RESPONSE, trade.askOrderHeight)
        json.addProperty(BID_ORDER_HEIGHT_RESPONSE, trade.bidOrderHeight)
        putAccount(json, SELLER_RESPONSE, trade.sellerId)
        putAccount(json, BUYER_RESPONSE, trade.buyerId)
        json.addProperty(BLOCK_RESPONSE, trade.blockId.toUnsignedString())
        json.addProperty(HEIGHT_RESPONSE, trade.height)
        json.addProperty(TRADE_TYPE_RESPONSE, if (trade.isBuy) "buy" else "sell")
        if (asset != null) {
            json.addProperty(NAME_RESPONSE, asset.name)
            json.addProperty(DECIMALS_RESPONSE, asset.decimals)
        }
        return json
    }

    internal fun assetTransfer(assetTransfer: AssetTransfer, asset: Asset?): JsonObject {
        val json = JsonObject()
        json.addProperty(ASSET_TRANSFER_RESPONSE, assetTransfer.id.toUnsignedString())
        json.addProperty(ASSET_RESPONSE, assetTransfer.assetId.toUnsignedString())
        putAccount(json, SENDER_RESPONSE, assetTransfer.senderId)
        putAccount(json, RECIPIENT_RESPONSE, assetTransfer.recipientId)
        json.addProperty(QUANTITY_QNT_RESPONSE, assetTransfer.quantity.toString())
        json.addProperty(HEIGHT_RESPONSE, assetTransfer.height)
        json.addProperty(TIMESTAMP_RESPONSE, assetTransfer.timestamp)
        if (asset != null) {
            json.addProperty(NAME_RESPONSE, asset.name)
            json.addProperty(DECIMALS_RESPONSE, asset.decimals)
        }

        return json
    }

    internal fun unconfirmedTransaction(transaction: Transaction): JsonObject {
        val json = JsonObject()
        json.addProperty(TYPE_RESPONSE, transaction.type.type)
        json.addProperty(SUBTYPE_RESPONSE, transaction.type.subtype)
        json.addProperty(TIMESTAMP_RESPONSE, transaction.timestamp)
        json.addProperty(DEADLINE_RESPONSE, transaction.deadline)
        json.addProperty(SENDER_PUBLIC_KEY_RESPONSE, transaction.senderPublicKey.toHexString())
        if (transaction.recipientId != 0L) {
            putAccount(json, RECIPIENT_RESPONSE, transaction.recipientId)
        }
        json.addProperty(AMOUNT_PLANCK_RESPONSE, transaction.amountPlanck.toString())
        json.addProperty(FEE_PLANCK_RESPONSE, transaction.feePlanck.toString())
        if (transaction.referencedTransactionFullHash != null) {
            json.addProperty(REFERENCED_TRANSACTION_FULL_HASH_RESPONSE, transaction.referencedTransactionFullHash.toHexString())
        }
        val signature = transaction.signature.emptyToNull()
        if (signature != null) {
            json.addProperty(SIGNATURE_RESPONSE, signature.toHexString())
            json.addProperty(SIGNATURE_HASH_RESPONSE, Crypto.sha256().digest(signature).toHexString())
            json.addProperty(FULL_HASH_RESPONSE, transaction.fullHash.toHexString())
            json.addProperty(TRANSACTION_RESPONSE, transaction.stringId)
        } else if (!transaction.type.isSigned) {
            json.addProperty(FULL_HASH_RESPONSE, transaction.fullHash.toHexString())
            json.addProperty(TRANSACTION_RESPONSE, transaction.stringId)
        }
        val attachmentJSON = JsonObject()
        for (appendage in transaction.appendages) {
            attachmentJSON.addAll(appendage.jsonObject)
        }
        if (attachmentJSON.size() > 0) {
            modifyAttachmentJSON(attachmentJSON)
            json.add(ATTACHMENT_RESPONSE, attachmentJSON)
        }
        putAccount(json, SENDER_RESPONSE, transaction.senderId)
        json.addProperty(HEIGHT_RESPONSE, transaction.height)
        json.addProperty(VERSION_RESPONSE, transaction.version)
        if (transaction.version > 0) {
            json.addProperty(EC_BLOCK_ID_RESPONSE, transaction.ecBlockId.toUnsignedString())
            json.addProperty(EC_BLOCK_HEIGHT_RESPONSE, transaction.ecBlockHeight)
        }

        return json
    }

    fun transaction(transaction: Transaction, currentBlockchainHeight: Int): JsonObject {
        val json = unconfirmedTransaction(transaction)
        json.addProperty(BLOCK_RESPONSE, transaction.blockId.toUnsignedString())
        json.addProperty(CONFIRMATIONS_RESPONSE, currentBlockchainHeight - transaction.height)
        json.addProperty(BLOCK_TIMESTAMP_RESPONSE, transaction.blockTimestamp)
        return json
    }

    // ugly, hopefully temporary
    private fun modifyAttachmentJSON(json: JsonObject) {
        val quantity = json.remove(QUANTITY_QNT_RESPONSE)
        if (quantity != null && quantity.isJsonPrimitive) {
            json.addProperty(QUANTITY_QNT_RESPONSE, quantity.asString)
        }
        val pricePlanck = json.remove(PRICE_PLANCK_RESPONSE)
        if (pricePlanck != null && pricePlanck.isJsonPrimitive) {
            json.addProperty(PRICE_PLANCK_RESPONSE, pricePlanck.asString)
        }
        val discountPlanck = json.remove(DISCOUNT_PLANCK_RESPONSE)
        if (discountPlanck != null && discountPlanck.isJsonPrimitive) {
            json.addProperty(DISCOUNT_PLANCK_RESPONSE, discountPlanck.asString)
        }
        val refundPlanck = json.remove(REFUND_PLANCK_RESPONSE)
        if (refundPlanck != null && refundPlanck.isJsonPrimitive) {
            json.addProperty(REFUND_PLANCK_RESPONSE, refundPlanck.asString)
        }
    }

    internal fun putAccount(json: JsonObject, name: String, accountId: Long) {
        json.addProperty(name, accountId.toUnsignedString())
        json.addProperty(name + "RS", accountId.rsAccount())
    }

    //TODO refactor the accountservice out of this :-)
    internal fun at(at: AT, accountService: AccountService): JsonObject {
        val json = JsonObject()
        val bf = ByteBuffer.allocate(8)
        bf.order(ByteOrder.LITTLE_ENDIAN)

        bf.put(at.creator)
        bf.clear()
        putAccount(json, "creator", bf.long) // TODO is this redundant or does this bring LE byte order?
        bf.clear()
        bf.put(at.id, 0, 8)
        val id = bf.getLong(0)
        json.addProperty("at", id.toUnsignedString())
        json.addProperty("atRS", id.rsAccount())
        json.addProperty("atVersion", at.version)
        json.addProperty("name", at.name)
        json.addProperty("description", at.description)
        json.addProperty("creator", AtApiHelper.getLong(at.creator!!).toUnsignedString())
        json.addProperty("creatorRS", AtApiHelper.getLong(at.creator!!).rsAccount())
        json.addProperty("machineCode", at.apCodeBytes.toHexString())
        json.addProperty("machineData", at.apDataBytes.toHexString())
        json.addProperty("balanceNQT", accountService.getAccount(id)!!.balancePlanck.toUnsignedString())
        json.addProperty("prevBalanceNQT", at.getpBalance().toUnsignedString())
        json.addProperty("nextBlock", at.nextHeight())
        json.addProperty("frozen", at.freezeOnSameBalance())
        json.addProperty("running", at.machineState.running)
        json.addProperty("stopped", at.machineState.stopped)
        json.addProperty("finished", at.machineState.finished)
        json.addProperty("dead", at.machineState.dead)
        json.addProperty("minActivation", at.minActivationAmount().toUnsignedString())
        json.addProperty("creationBlock", at.creationBlockHeight)
        return json
    }

    internal fun hex2long(longString: String): JsonObject {
        val json = JsonObject()
        json.addProperty("hex2long", longString)
        return json
    }
}
