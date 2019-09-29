package brs

import brs.util.Observable

import java.math.BigInteger

interface Generator : Observable<Generator.GeneratorState, Generator.Event> {
    val allGenerators: Collection<GeneratorState>

    enum class Event {
        GENERATION_DEADLINE, NONCE_SUBMITTED
    }

    fun generateForBlockchainProcessor(dp: DependencyProvider)

    fun addNonce(secretPhrase: String, nonce: Long?): GeneratorState

    fun addNonce(secretPhrase: String, nonce: Long?, publicKey: ByteArray): GeneratorState

    fun calculateGenerationSignature(lastGenSig: ByteArray, lastGenId: Long): ByteArray

    fun calculateScoop(genSig: ByteArray, height: Long): Int

    fun calculateHit(accountId: Long, nonce: Long, genSig: ByteArray, scoop: Int, blockHeight: Int): BigInteger

    fun calculateHit(accountId: Long, nonce: Long, genSig: ByteArray, scoopData: ByteArray): BigInteger

    fun calculateDeadline(accountId: Long, nonce: Long, genSig: ByteArray, scoop: Int, baseTarget: Long, blockHeight: Int): BigInteger

    interface GeneratorState {
        val publicKey: ByteArray

        val accountId: Long?

        val deadline: BigInteger

        val block: Long
    }
}
