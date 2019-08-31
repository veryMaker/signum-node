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

    protected constructor(dp: DependencyProvider, id: Long?, senderId: Long?, recipientId: Long?, dbKey: BurstKey, amountNQT: Long?,
                          requiredSigners: Int, deadline: Int, deadlineAction: DecisionType) {
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
            when (decision) {
                Escrow.DecisionType.UNDECIDED -> return "undecided"
                Escrow.DecisionType.RELEASE -> return "release"
                Escrow.DecisionType.REFUND -> return "refund"
                Escrow.DecisionType.SPLIT -> return "split"
            }

            return null
        }

        fun stringToDecision(decision: String): DecisionType? {
            when (decision) {
                "undecided" -> return DecisionType.UNDECIDED
                "release" -> return DecisionType.RELEASE
                "refund" -> return DecisionType.REFUND
                "split" -> return DecisionType.SPLIT
                else -> return null
            }
        }

        fun decisionToByte(decision: DecisionType): Byte? {
            when (decision) {
                Escrow.DecisionType.UNDECIDED -> return 0
                Escrow.DecisionType.RELEASE -> return 1
                Escrow.DecisionType.REFUND -> return 2
                Escrow.DecisionType.SPLIT -> return 3
                else -> return null
            }
        }

        fun byteToDecision(decision: Byte): DecisionType? {
            when (decision) {
                0 -> return DecisionType.UNDECIDED
                1 -> return DecisionType.RELEASE
                2 -> return DecisionType.REFUND
                3 -> return DecisionType.SPLIT
                else -> return null
            }
        }

        fun decisionToProtobuf(decision: DecisionType): BrsApi.EscrowDecisionType {
            when (decision) {
                Escrow.DecisionType.UNDECIDED -> return BrsApi.EscrowDecisionType.UNDECIDED
                Escrow.DecisionType.RELEASE -> return BrsApi.EscrowDecisionType.RELEASE
                Escrow.DecisionType.REFUND -> return BrsApi.EscrowDecisionType.REFUND
                Escrow.DecisionType.SPLIT -> return BrsApi.EscrowDecisionType.SPLIT
                else -> return BrsApi.EscrowDecisionType.EscrowDecisionType_UNSET
            }
        }

        fun protoBufToDecision(decision: BrsApi.EscrowDecisionType): DecisionType? {
            when (decision) {
                BrsApi.EscrowDecisionType.UNDECIDED -> return DecisionType.UNDECIDED
                BrsApi.EscrowDecisionType.RELEASE -> return DecisionType.RELEASE
                BrsApi.EscrowDecisionType.REFUND -> return DecisionType.REFUND
                BrsApi.EscrowDecisionType.SPLIT -> return DecisionType.SPLIT
                else -> return null
            }
        }
    }
}
