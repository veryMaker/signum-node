package brs.entity

import brs.db.BurstKey
import brs.db.ValuesTable
import brs.transaction.appendix.Attachment
import brs.util.delegates.AtomicLazy
import burst.kit.entity.BurstEncryptedMessage

open class Purchase {
    private lateinit var dp: DependencyProvider

    val id: Long
    val dbKey: BurstKey
    val buyerId: Long
    val goodsId: Long
    val sellerId: Long
    val quantity: Int
    val pricePlanck: Long
    val deliveryDeadlineTimestamp: Int
    val note: BurstEncryptedMessage?
    val timestamp: Int
    var isPending: Boolean = false
    var encryptedGoods: BurstEncryptedMessage? = null
        private set
    var refundNote: BurstEncryptedMessage? = null
    private var hasFeedbackNotes: Boolean = false
    var feedbackNotes by AtomicLazy {
        if (!hasFeedbackNotes) null else feedbackTable(dp).get(feedbackDbKeyFactory(dp).newKey(this@Purchase)).toMutableList()
    }
    private var hasPublicFeedbacks: Boolean = false
    var publicFeedbacks: MutableList<String>? = null
        private set
    var discountPlanck: Long = 0
    var refundPlanck: Long = 0

    fun getName() = getGoods(dp, goodsId)!!.name

    fun getPublicFeedback(): List<String>? {
        if (!hasPublicFeedbacks) {
            return emptyList()
        }
        publicFeedbacks = publicFeedbackTable(dp).get(publicFeedbackDbKeyFactory(dp).newKey(this)).toMutableList()
        return publicFeedbacks
    }

    private fun purchaseDbKeyFactory(dp: DependencyProvider): BurstKey.LongKeyFactory<Purchase> {
        return dp.digitalGoodsStoreStore.purchaseDbKeyFactory
    }

    private fun feedbackDbKeyFactory(dp: DependencyProvider): BurstKey.LongKeyFactory<Purchase> {
        return dp.digitalGoodsStoreStore.feedbackDbKeyFactory
    }

    private fun feedbackTable(dp: DependencyProvider): ValuesTable<Purchase, BurstEncryptedMessage> {
        return dp.digitalGoodsStoreStore.feedbackTable
    }

    private fun publicFeedbackDbKeyFactory(dp: DependencyProvider): BurstKey.LongKeyFactory<Purchase> {
        return dp.digitalGoodsStoreStore.publicFeedbackDbKeyFactory
    }

    private fun publicFeedbackTable(dp: DependencyProvider): ValuesTable<Purchase, String> {
        return dp.digitalGoodsStoreStore.publicFeedbackTable
    }

    constructor(
        dp: DependencyProvider,
        transaction: Transaction,
        attachment: Attachment.DigitalGoodsPurchase,
        sellerId: Long
    ) {
        this.dp = dp
        this.id = transaction.id
        this.dbKey = purchaseDbKeyFactory(dp).newKey(this.id)
        this.buyerId = transaction.senderId
        this.goodsId = attachment.goodsId
        this.sellerId = sellerId
        this.quantity = attachment.quantity
        this.pricePlanck = attachment.pricePlanck
        this.deliveryDeadlineTimestamp = attachment.deliveryDeadlineTimestamp
        this.note = if (transaction.encryptedMessage == null) null else transaction.encryptedMessage.encryptedData
        this.timestamp = transaction.timestamp
        this.isPending = true
    }

    protected constructor(
        dp: DependencyProvider, id: Long, dbKey: BurstKey, buyerId: Long, goodsId: Long, sellerId: Long, quantity: Int,
        pricePlanck: Long, deadline: Int, note: BurstEncryptedMessage?, timestamp: Int, isPending: Boolean,
        encryptedGoods: BurstEncryptedMessage?, refundNote: BurstEncryptedMessage?,
        hasFeedbackNotes: Boolean, hasPublicFeedbacks: Boolean,
        discountPlanck: Long, refundPlanck: Long
    ) {
        this.dp = dp
        this.id = id
        this.dbKey = dbKey
        this.buyerId = buyerId
        this.goodsId = goodsId
        this.sellerId = sellerId
        this.quantity = quantity
        this.pricePlanck = pricePlanck
        this.deliveryDeadlineTimestamp = deadline
        this.note = note
        this.timestamp = timestamp
        this.isPending = isPending
        this.encryptedGoods = encryptedGoods
        this.refundNote = refundNote
        this.hasFeedbackNotes = hasFeedbackNotes
        this.hasPublicFeedbacks = hasPublicFeedbacks
        this.discountPlanck = discountPlanck
        this.refundPlanck = refundPlanck
    }

    fun setEncryptedGoods(encryptedGoods: BurstEncryptedMessage) {
        this.encryptedGoods = encryptedGoods
    }

    fun addFeedbackNote(feedbackNote: BurstEncryptedMessage) {
        if (feedbackNotes == null) {
            feedbackNotes = mutableListOf()
        }
        feedbackNotes!!.add(feedbackNote)
        this.hasFeedbackNotes = true
    }

    fun setHasPublicFeedbacks(hasPublicFeedbacks: Boolean) {
        this.hasPublicFeedbacks = hasPublicFeedbacks
    }

    companion object {
        private fun getGoods(dp: DependencyProvider, goodsId: Long): Goods? {
            return dp.digitalGoodsStoreStore.goodsTable[dp.digitalGoodsStoreStore.goodsDbKeyFactory.newKey(goodsId)]
        }
    }
}
