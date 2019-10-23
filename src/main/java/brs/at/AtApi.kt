package brs.at

internal interface AtApi {
    // range 0x0100..0x01ff

    /**
     * sets @addr to A1 (0x0100)
     *
     * @param state AT machine state
     * @return A1 register
     */
    fun getA1(state: AtMachineState): Long

    /**
     * sets @addr to A2 (0x0101)
     *
     * @param state AT machine state
     * @return A2 register
     */
    fun getA2(state: AtMachineState): Long

    /**
     * sets @addr to A3 (0x0102)
     *
     * @param state AT machine state
     * @return A3 register
     */
    fun getA3(state: AtMachineState): Long

    /**
     * sets @addr to A4 (0x0103)
     *
     * @param state AT machine state
     * @return A4 register
     */
    fun getA4(state: AtMachineState): Long

    /**
     * sets @addr to B1 (0x0104)
     *
     * @param state AT machine state
     * @return B1 register
     */
    fun getB1(state: AtMachineState): Long

    /**
     * sets @addr to B2 (0x0105)
     *
     * @param state AT machine state
     * @return B2 register
     */
    fun getB2(state: AtMachineState): Long

    /**
     * sets @addr to B3 (0x0106)
     *
     * @param state AT machine state
     * @return B3 register
     */
    fun getB3(state: AtMachineState): Long

    /**
     * sets @addr to B4 (0x0107)
     *
     * @param state AT machine state
     * @return B4 register
     */
    fun getB4(state: AtMachineState): Long

    /**
     * sets A1 to @addr (0x0110)
     *
     * @param val   new A1 register value
     * @param state AT machine state
     */
    fun setA1(`val`: Long, state: AtMachineState)

    /**
     * sets A2 to @addr (0x0111)
     *
     * @param val   new A2 register value
     * @param state AT machine state
     */
    fun setA2(`val`: Long, state: AtMachineState)

    /**
     * sets A3 to @addr (0x0112)
     *
     * @param val   new A3 register value
     * @param state AT machine state
     */
    fun setA3(`val`: Long, state: AtMachineState)

    /**
     * sets A4 to @addr (0x0113)
     *
     * @param val   new A4 register value
     * @param state AT machine state
     */
    fun setA4(`val`: Long, state: AtMachineState)

    /**
     * sets A1 from @addr1 and A2 from @addr2 (0x0114)
     *
     * @param val1  new A1 register value
     * @param val2  new A2 register value
     * @param state AT machine state
     */
    fun setA1A2(val1: Long, val2: Long, state: AtMachineState)

    /**
     * sets A3 from @addr1 and A4 from @addr2 ((0x0115)
     *
     * @param val1  new A3 register value
     * @param val2  new A4 register value
     * @param state AT machine state
     */
    fun setA3A4(val1: Long, val2: Long, state: AtMachineState)

    /**
     * sets B1 from @addr (0x0116)
     *
     * @param val   new B1 register value
     * @param state AT machine state
     */
    fun setB1(`val`: Long, state: AtMachineState)

    /**
     * sets B2 from @addr (0x0117)
     *
     * @param val   new B2 register value
     * @param state AT machine state
     */
    fun setB2(`val`: Long, state: AtMachineState)

    /**
     * sets B3 from @addr (0x0118)
     *
     * @param val   new B3 register value
     * @param state AT machine state
     */
    fun setB3(`val`: Long, state: AtMachineState)

    /**
     * sets B4 @addr (0x0119)
     *
     * @param val   new B4 register value
     * @param state AT machine state
     */
    fun setB4(`val`: Long, state: AtMachineState)

    /**
     * sets B1 from @addr1 and B2 from @addr2 (0x011a)
     *
     * @param val1  new B1 register value
     * @param val2  new B2 register value
     * @param state AT machine state
     */
    fun setB1B2(val1: Long, val2: Long, state: AtMachineState)

    /**
     * sets B3 from @addr3 and @addr4 to B4 (0x011b)
     *
     * @param val3  new B3 register value
     * @param val4  new B4 register value
     * @param state AT machine state
     */
    fun setB3B4(val3: Long, val4: Long, state: AtMachineState)

    /**
     * sets A to zero (A being A1...4)
     *
     * @param state AT machine state
     */
    fun clearA(state: AtMachineState)

    /**
     * sets B to zero (B being B1...4)
     *
     * @param state AT machine state
     */
    fun clearB(state: AtMachineState)

    /**
     * gets A from B
     *
     * @param state AT machine state
     */
    fun copyAFromB(state: AtMachineState)

    /**
     * gets B from A
     *
     * @param state AT machine state
     */
    fun copyBFromA(state: AtMachineState)

    /**
     * bool is A is zero
     *
     * @param state AT machine state
     * @return boolean value (in a long?) if A is zero
     */
    fun checkAIsZero(state: AtMachineState): Long

    /**
     * bool is B is zero
     *
     * @param state AT machine state
     * @return boolean value (in a long?) if B is zero
     */
    fun checkBIsZero(state: AtMachineState): Long


    /**
     * bool does A equal B
     *
     * @param state AT machine state
     * @return boolean value (in a long?) if B is zero
     */
    fun checkAEqualsB(state: AtMachineState): Long

    /**
     * swap the values of A and B
     *
     * @param state AT machine state
     */
    fun swapAAndB(state: AtMachineState)

    // note: these 8 math ops are intended for a future implementaion so no need to support them

    /**
     * adds A to B (result in B)
     *
     * @param state AT machine state
     */
    fun addAToB(state: AtMachineState)

    /**
     * add B to A (result in A)
     *
     * @param state AT machine state
     */
    fun addBToA(state: AtMachineState)

    /**
     * subs A from B (result in B)
     *
     * @param state AT machine state
     */
    fun subAFromB(state: AtMachineState)

    /**
     * subs B from A (result in A)
     *
     * @param state AT machine state
     */
    fun subBFromA(state: AtMachineState)

    /**
     * multiplies A by B (result in B)
     *
     * @param state AT machine state
     */
    fun mulAByB(state: AtMachineState)

    /**
     * multiplies B by A (result in A)
     *
     * @param state AT machine state
     */
    fun mulBByA(state: AtMachineState)

    /**
     * divides A by B (result in B) *can cause a divide by zero error which would stop the machine
     *
     * @param state AT machine state
     */
    fun divAByB(state: AtMachineState)

    /**
     * divides B by A (result in A) *can cause a divide by zero error which would stop the machine
     *
     * @param state AT machine state
     */
    fun divBByA(state: AtMachineState)

    /**
     * ors A by B (result in A)
     *
     * @param state AT machine state
     */
    fun orAWithB(state: AtMachineState)

    /**
     * ors B by A (result in B)
     *
     * @param state AT machine state
     */
    fun orBWithA(state: AtMachineState)

    /**
     * ands A by B (result in A)
     *
     * @param state AT machine state
     */
    fun andAWithB(state: AtMachineState)

    /**
     * ands B by A (result in B)
     *
     * @param state AT machine state
     */
    fun andBWithA(state: AtMachineState)

    /**
     * xors A by B (result in A)
     *
     * @param state AT machine state
     */
    fun xorAWithB(state: AtMachineState)

    /**
     * xors B by A (result in B)
     *
     * @param state AT machine state
     */
    fun xorBWithA(state: AtMachineState)

    // end range 0x0100..0x01ff

    // range 0x0200..0x02ff

    /**
     * sets @addr1 and @addr2 to the MD5 hash of A1..4
     *
     * @param state AT machine state
     */
    fun md5Atob(state: AtMachineState)

    /**
     * bool if @addr1 and @addr2 matches the MD5 hash of A1..4
     *
     * @param state AT machine state
     * @return bool if @addr1 and @addr2 matches the MD5 hash of A1..4
     */
    fun checkMd5AWithB(state: AtMachineState): Long

    /**
     * take a RIPEMD160 hash of A1..4 and put this in B1..4
     *
     * @param state AT machine state
     */
    fun hash160AToB(state: AtMachineState)

    /**
     * bool if RIPEMD160 hash of A1..4 matches B1..4
     *
     * @param state AT machine state
     * @return bool if RIPEMD160 hash of A1..4 matches B1..4
     */
    fun checkHash160AWithB(state: AtMachineState): Long

    /**
     * take a SHA256 hash of A1..4 abd out this in B1..4
     *
     * @param state AT machine state
     */
    fun sha256AToB(state: AtMachineState)

    /**
     * bool if SHA256 of A1..4 matches B1..4
     *
     * @param state AT machine state
     * @return bool if SHA256 of A1..4 matches B1..4
     */
    fun checkSha256AWithB(state: AtMachineState): Long

    // end of range 0x02..0x02ff

    // range 0x03..0x03ff

    /**
     * sets @addr to the timestamp of the current block
     *
     * @param state AT machine state
     */
    fun getBlockTimestamp(state: AtMachineState): Long

    /**
     * sets @addr to the timestamp of the AT creation block
     *
     * @param state AT machine state
     */
    fun getCreationTimestamp(state: AtMachineState): Long


    /**
     * sets @addr to the timestamp of the previous block
     *
     * @param state AT machine state
     * @return timestamp of the previous block
     */
    fun getLastBlockTimestamp(state: AtMachineState): Long

    /**
     * puts the block hash of the previous block in A
     *
     * @param state AT machine state
     */
    suspend fun putLastBlockHashInA(state: AtMachineState)

    /**
     * sets A to zero/tx hash of the first tx after
     *
     * @param state AT machine state
     */
    suspend fun aToTxAfterTimestamp(`val`: Long, state: AtMachineState)

    /**
     * @param state AT machine state
     * @return bool if A is a valid tx with @addr to tx type
     * 0: normal tx
     * 1: message tx
     */
    suspend fun getTypeForTxInA(state: AtMachineState): Long

    /**
     * @param state AT machine state
     * @return bool if A is a valid tx with @addr to tx amount
     */
    suspend fun getAmountForTxInA(state: AtMachineState): Long

    /**
     * @param state AT machine state
     * @return bool if A is a valid tx with @addr to the tx timestamp
     */
    suspend fun getTimestampForTxInA(state: AtMachineState): Long

    /**
     * @param state AT machine state
     * @return bool if A is a valid tx with @addr to the tx random id
     * random id is a 64bit signed value (always positive) and this is a blocking function
     */
    suspend fun getRandomIdForTxInA(state: AtMachineState): Long

    /**
     * bool if A is a valid tx with B to the tx message
     * if a tx is not a message tx then this will zero out the B value
     *
     * @param state AT machine state
     */
    suspend fun messageFromTxInAToB(state: AtMachineState)

    /**
     * bool if A is a valid tx with B set to the tx address
     *
     * @param state AT machine state
     */
    suspend fun bToAddressOfTxInA(state: AtMachineState)

    /**
     * set B to the address of the AT's creator
     *
     * @param state AT machine state
     */
    fun bToAddressOfCreator(state: AtMachineState)

    // end range 0x0300..0x03ff
    // ------------------------

    // ------------------------
    // range 0x0400..0x04ff

    /**
     * sets @addr to current balance of the AT
     *
     * @param state AT machine state
     */
    fun getCurrentBalance(state: AtMachineState): Long

    /**
     * sets @addr to the balance it had last had when running
     * this amount does not include any additional amounts sent to the
     * AT between "execution events"
     *
     * @param state AT machine state
     */
    fun getPreviousBalance(state: AtMachineState): Long

    /**
     * bool if B is a valid address then send it $addr amount
     * if this amount is greater than the AT's balance then it will also
     * return false
     *
     * @param state AT machine state
     */
    fun sendToAddressInB(`val`: Long, state: AtMachineState)

    /**
     * bool if B is a valid address then send it entire balance
     *
     * @param state AT machine state
     */
    fun sendAllToAddressInB(state: AtMachineState)

    /**
     * bool if B is a valid address then send it the old balance
     *
     * @param state AT machine state
     */
    fun sendOldToAddressInB(state: AtMachineState)

    /**
     * bool if B is valid address then send it A as a message
     *
     * @param state AT machine state
     */
    fun sendAToAddressInB(state: AtMachineState)

    /**
     * $addr1 is timestamp calculated from $addr2
     *
     * @param state AT machine state
     * @return time+minutes
     */
    fun addMinutesToTimestamp(val1: Long, val2: Long, state: AtMachineState): Long

    /**
     * set min amount of balance increase needed to unfreeze
     *
     * @param state AT machine state
     */
    fun setMinActivationAmount(`val`: Long, state: AtMachineState)

    // end range 0x0400.0x04ff
    // -----------------------

    /**
     * puts the gensig of the previous block in A
     *
     * @param state AT machine state
     */
    suspend fun putLastBlockGenerationSignatureInA(state: AtMachineState)

    /**
     * take a SHA256 hash of val2 bytes starting at val1. out this in B1..4
     *
     * @param state AT machine state
     */
    fun sha256ToB(val1: Long, val2: Long, state: AtMachineState)
}
