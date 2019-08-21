package it.common

import com.google.gson.JsonArray
import com.google.gson.JsonObject

class BlockMessageBuilder {
    private var payloadLength: Long = 0
    private var totalAmountNQT: Long = 0
    private var version: Long = 0
    private var nonce: String? = null
    private var totalFeeNQT: Long = 0
    private var blockATs: String? = null
    private var previousBlock: String? = null
    private var generationSignature: String? = null
    private var generatorPublicKey: String? = null
    private var payloadHash: String? = null
    private var blockSignature: String? = null
    private var transactions = JsonArray()
    private var timestamp: Long = 0
    private var previousBlockHash: String? = null

    fun payloadLength(payloadLength: Long): BlockMessageBuilder {
        this.payloadLength = payloadLength
        return this
    }

    fun totalAmountNQT(totalAmountNQT: Long): BlockMessageBuilder {
        this.totalAmountNQT = totalAmountNQT
        return this
    }

    fun version(version: Long): BlockMessageBuilder {
        this.version = version
        return this
    }

    fun nonce(nonce: String): BlockMessageBuilder {
        this.nonce = nonce
        return this
    }

    fun totalFeeNQT(totalFeeNQT: Long): BlockMessageBuilder {
        this.totalFeeNQT = totalFeeNQT
        return this
    }

    fun blockATs(blockATs: String?): BlockMessageBuilder {
        this.blockATs = blockATs
        return this
    }

    fun previousBlock(previousBlock: String): BlockMessageBuilder {
        this.previousBlock = previousBlock
        return this
    }

    fun generationSignature(generationSignature: String): BlockMessageBuilder {
        this.generationSignature = generationSignature
        return this
    }

    fun generatorPublicKey(generatorPublicKey: String): BlockMessageBuilder {
        this.generatorPublicKey = generatorPublicKey
        return this
    }

    fun payloadHash(payloadHash: String): BlockMessageBuilder {
        this.payloadHash = payloadHash
        return this
    }

    fun blockSignature(blockSignature: String): BlockMessageBuilder {
        this.blockSignature = blockSignature
        return this
    }

    fun transactions(transactions: JsonArray?): BlockMessageBuilder {
        if (transactions == null) {
            this.transactions = JsonArray()
        } else {
            this.transactions = transactions
        }
        return this
    }

    fun timestamp(timestamp: Long): BlockMessageBuilder {
        this.timestamp = timestamp
        return this
    }

    fun previousBlockHash(previousBlockHash: String): BlockMessageBuilder {
        this.previousBlockHash = previousBlockHash
        return this
    }

    fun toJson(): JsonObject {
        val overview = JsonObject()

        overview.addProperty("payloadLength", payloadLength)
        overview.addProperty("totalAmountNQT", totalAmountNQT)
        overview.addProperty("version", version)
        overview.addProperty("nonce", nonce)
        overview.addProperty("totalFeeNQT", totalFeeNQT)
        overview.addProperty("blockATs", blockATs)
        overview.addProperty("previousBlock", previousBlock)
        overview.addProperty("generationSignature", generationSignature)
        overview.addProperty("generatorPublicKey", generatorPublicKey)
        overview.addProperty("payloadHash", payloadHash)
        overview.addProperty("blockSignature", blockSignature)
        overview.add("transactions", transactions)
        overview.addProperty("timestamp", timestamp)
        overview.addProperty("previousBlockHash", previousBlockHash)

        return overview
    }
}
