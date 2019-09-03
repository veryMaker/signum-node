package brs

import brs.db.BurstKey
import brs.grpc.proto.BrsApi

open class Escrow {
    private val dp: DependencyProvider
    val senderId: Long?
    val recipientId: Long?
    val id: Long?
    val dbKey: BurstKey
    val amountNQT: Long?
    val requiredSigners: Int
    val deadline: Int
    val deadlineAction: DecisionType

    val decisions: Collection<Decision>
        get() = dp.escrowStore.getDecisions(id)

    enum class DecisionType {
        UNDECIDED,
        RELEASE,
        REFUND,
        SPLIT
    }

    open class Decision(val dbKey: BurstKey, val escrowId: Long?, val accountId: Long?, var decision: DecisionType?)

    constructor(dp: DependencyProvider, dbKey: BurstKey, sender: Account,
                recipient: Account,
                id: Long?,
                amountNQT: Long?,
                requiredSigners: Int,
                deadline: Int,
                deadlineAction: DecisionType) {
        this.dp = dp
        this.dbKey = dbKey
        this.senderId = sender.id
        this.recipientId = recipient.id
        this.id = id
        this.amountNQT = amountNQT
        this.requiredSigners = requiredSigners
        this.deadline = deadline
        this.deadlineAction = deadlineAction
    }

    protected constructor(dp: DependencyProvider, id: Long?, senderId: Long?, recipientId: Long?, dbKey: BurstKey, amountNQT: Long?, requiredSigners: Int, deadline: Int, deadlineAction: DecisionType) {
        this.dp = dp
        this.senderId = senderId
        this.recipientId = recipientId
        this.id = id
        this.dbKey = dbKey
        this.amountNQT = amountNQT
        this.requiredSigners = requiredSigners
        this.deadline = deadline
        this.deadlineAction = deadlineAction
    }

    companion object {
        fun decisionToString(decision: DecisionType): String? {
            return when (decision) {
                DecisionType.UNDECIDED -> "undecided"
                DecisionType.RELEASE -> "release"
                DecisionType.REFUND -> "refund"
                DecisionType.SPLIT -> "split"
            }
        }

        fun stringToDecision(decision: String): DecisionType? {
            return when (decision) {
                "undecided" -> DecisionType.UNDECIDED
                "release" -> DecisionType.RELEASE
                "refund" -> DecisionType.REFUND
                "split" -> DecisionType.SPLIT
                else -> null
            }
        }

        fun decisionToByte(decision: DecisionType): Byte? {
            return when (decision) {
                DecisionType.UNDECIDED -> 0
                DecisionType.RELEASE -> 1
                DecisionType.REFUND -> 2
                DecisionType.SPLIT -> 3
            }
        }

        fun byteToDecision(decision: Byte): DecisionType? {
            return when (decision.toInt()) {
                0 -> DecisionType.UNDECIDED
                1 -> DecisionType.RELEASE
                2 -> DecisionType.REFUND
                3 -> DecisionType.SPLIT
                else -> null
            }
        }

        fun decisionToProtobuf(decision: DecisionType): BrsApi.EscrowDecisionType {
            return when (decision) {
                DecisionType.UNDECIDED -> BrsApi.EscrowDecisionType.UNDECIDED
                DecisionType.RELEASE -> BrsApi.EscrowDecisionType.RELEASE
                DecisionType.REFUND -> BrsApi.EscrowDecisionType.REFUND
                DecisionType.SPLIT -> BrsApi.EscrowDecisionType.SPLIT
            }
        }

        fun protoBufToDecision(decision: BrsApi.EscrowDecisionType): DecisionType? {
            return when (decision) {
                BrsApi.EscrowDecisionType.UNDECIDED -> DecisionType.UNDECIDED
                BrsApi.EscrowDecisionType.RELEASE -> DecisionType.RELEASE
                BrsApi.EscrowDecisionType.REFUND -> DecisionType.REFUND
                BrsApi.EscrowDecisionType.SPLIT -> DecisionType.SPLIT
                else -> null
            }
        }
    }
}
