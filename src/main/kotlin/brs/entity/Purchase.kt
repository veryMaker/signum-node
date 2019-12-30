package brs.entity

import brs.db.BurstKey
import brs.transaction.appendix.Attachment
import brs.util.delegates.AtomicLazy
import burst.kit.entity.BurstEncryptedMessage

// TODO stop these entities from being open
open class Purchase(
    private val dp: DependencyProvider,
    val id: Long,
    val dbKey: BurstKey,
    val buyerId: Long,
    val goodsId: Long,
    val sellerId: Long,
    val quantity: Int,
    val pricePlanck: Long,
    val deliveryDeadlineTimestamp: Int,
    val note: BurstEncryptedMessage?,
    val timestamp: Int
) {
    var isPending: Boolean = false
    var encryptedGoods: BurstEncryptedMessage? = null
        private set
    var refundNote: BurstEncryptedMessage? = null
    private var hasFeedbackNotes: Boolean = false
    val feedbackNotes by AtomicLazy {
        if (!hasFeedbackNotes) mutableListOf() else dp.digitalGoodsStoreStore.feedbackTable[dp.digitalGoodsStoreStore.feedbackDbKeyFactory.newKey(this@Purchase)].toMutableList()
    }
    private var hasPublicFeedbacks: Boolean = false
    val publicFeedback by lazy {
        if (!hasFeedbackNotes) mutableListOf() else dp.digitalGoodsStoreStore.publicFeedbackTable[dp.digitalGoodsStoreStore.publicFeedbackDbKeyFactory.newKey(this@Purchase)].toMutableList()
    }
    var discountPlanck: Long = 0
    var refundPlanck: Long = 0

    val name by lazy {
        dp.digitalGoodsStoreStore.goodsTable[dp.digitalGoodsStoreStore.goodsDbKeyFactory.newKey(goodsId)]!!.name
    }

    constructor(
        dp: DependencyProvider,
        transaction: Transaction,
        attachment: Attachment.DigitalGoodsPurchase,
        sellerId: Long
    ) : this(dp,
        transaction.id,
        dp.digitalGoodsStoreStore.purchaseDbKeyFactory.newKey(transaction.id),
        transaction.senderId,
        attachment.goodsId,
        sellerId,
        attachment.quantity,
        attachment.pricePlanck,
        attachment.deliveryDeadlineTimestamp,
        if (transaction.encryptedMessage == null) null else transaction.encryptedMessage.encryptedData, transaction.timestamp) {
        this.isPending = false
    }

    protected constructor(
        dp: DependencyProvider, id: Long, dbKey: BurstKey, buyerId: Long, goodsId: Long, sellerId: Long, quantity: Int,
        pricePlanck: Long, deadline: Int, note: BurstEncryptedMessage?, timestamp: Int, isPending: Boolean,
        encryptedGoods: BurstEncryptedMessage?, refundNote: BurstEncryptedMessage?,
        hasFeedbackNotes: Boolean, hasPublicFeedbacks: Boolean,
        discountPlanck: Long, refundPlanck: Long
    ) : this(dp, id, dbKey, buyerId, goodsId, sellerId, quantity, pricePlanck, deadline, note, timestamp) {
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
        feedbackNotes.add(feedbackNote)
        this.hasFeedbackNotes = true
    }

    fun setHasPublicFeedbacks(hasPublicFeedbacks: Boolean) {
        this.hasPublicFeedbacks = hasPublicFeedbacks
    }
}
