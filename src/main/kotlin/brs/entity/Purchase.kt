package brs.entity

import brs.db.BurstKey
import brs.db.VersionedValuesTable
import brs.transaction.appendix.Attachment
import brs.util.delegates.AtomicLazy

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
    val note: EncryptedData?
    val timestamp: Int
    var isPending: Boolean = false
    var encryptedGoods: EncryptedData? = null
        private set
    private var goodsIsText: Boolean = false
    var refundNote: EncryptedData? = null
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

    private fun feedbackTable(dp: DependencyProvider): VersionedValuesTable<Purchase, EncryptedData> {
        return dp.digitalGoodsStoreStore.feedbackTable
    }

    private fun publicFeedbackDbKeyFactory(dp: DependencyProvider): BurstKey.LongKeyFactory<Purchase> {
        return dp.digitalGoodsStoreStore.publicFeedbackDbKeyFactory
    }

    private fun publicFeedbackTable(dp: DependencyProvider): VersionedValuesTable<Purchase, String> {
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
        pricePlanck: Long, deadline: Int, note: EncryptedData?, timestamp: Int, isPending: Boolean,
        encryptedGoods: EncryptedData?, refundNote: EncryptedData?,
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

    fun goodsIsText(): Boolean {
        return goodsIsText
    }

    fun setEncryptedGoods(encryptedGoods: EncryptedData, goodsIsText: Boolean) {
        this.encryptedGoods = encryptedGoods
        this.goodsIsText = goodsIsText
    }

    fun addFeedbackNote(feedbackNote: EncryptedData) {
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
            return Goods.goodsTable(dp)[Goods.goodsDbKeyFactory(dp).newKey(goodsId)]
        }
    }
}
