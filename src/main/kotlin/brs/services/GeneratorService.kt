package brs.services

import brs.util.Observable

import java.math.BigInteger

interface GeneratorService : Observable<GeneratorService.GeneratorState, GeneratorService.Event> {
    /**
     * TODO
     */
    val allGenerators: Collection<GeneratorState>

    /**
     * TODO
     */
    enum class Event {
        NONCE_SUBMITTED
    }

    /**
     * TODO
     */
    fun addNonce(secretPhrase: String, nonce: Long?): GeneratorState

    /**
     * TODO
     */
    fun addNonce(secretPhrase: String, nonce: Long?, publicKey: ByteArray): GeneratorState

    /**
     * TODO
     */
    fun calculateGenerationSignature(lastGenSig: ByteArray, lastGenId: Long): ByteArray

    /**
     * TODO
     */
    fun calculateScoop(genSig: ByteArray, height: Long): Int

    /**
     * TODO
     */
    fun calculateHit(accountId: Long, nonce: Long, genSig: ByteArray, scoop: Int, blockHeight: Int): BigInteger

    /**
     * TODO
     */
    fun calculateHit(accountId: Long, nonce: Long, genSig: ByteArray, scoopData: ByteArray): BigInteger

    /**
     * TODO
     */
    fun calculateDeadline(accountId: Long, nonce: Long, genSig: ByteArray, scoop: Int, baseTarget: Long, blockHeight: Int): BigInteger

    /**
     * TODO
     */
    interface GeneratorState {
        /**
         * TODO
         */
        val publicKey: ByteArray

        /**
         * TODO
         */
        val accountId: Long?

        /**
         * TODO
         */
        val deadline: BigInteger

        /**
         * TODO
         */
        val block: Long
    }
}
