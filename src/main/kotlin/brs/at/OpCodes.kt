/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.
*/

package brs.at

internal object OpCodes {
    /** No-op */
    const val NOP: Byte = 0x7f
    /** Set value at address (1st argument) to value (second argument) */
    const val SET_VAL: Byte = 0x01
    /** Set value at address (1st argument) to value at address (second argument) */
    const val SET_DAT: Byte = 0x02
    /** Set value at address (1st argument) to 0 */
    const val CLR_DAT: Byte = 0x03
    /** Increment value at address (1st argument) by 1 */
    const val INC_DAT: Byte = 0x04
    /** Decrement value at address (1st argument) by 1 */
    const val DEC_DAT: Byte = 0x05
    /** Add value at address (1st argument) to value at address (second argument) */
    const val ADD_DAT: Byte = 0x06
    /** Subtract value at address (1st argument) to value at address (second argument) */
    const val SUB_DAT: Byte = 0x07
    /** Multiply value at address (1st argument) by value at address (second argument) */
    const val MUL_DAT: Byte = 0x08
    /** Divide value at address (1st argument) by value at address (second argument) */
    const val DIV_DAT: Byte = 0x09
    /** Bitwise OR value at address (1st argument) with value at address (second argument) */
    const val BOR_DAT: Byte = 0x0a
    /** Bitwise AND value at address (1st argument) with value at address (second argument) */
    const val AND_DAT: Byte = 0x0b
    /** Bitwise XOR value at address (1st argument) with value at address (second argument) */
    const val XOR_DAT: Byte = 0x0c
    /** Bitwise NOT value at address (1st argument) */
    const val NOT_DAT: Byte = 0x0d
    /** Indirect Set: Set value at address (1st argument) to the value at address (value at address (2nd argument)) */
    const val SET_IND: Byte = 0x0e
    const val SET_IDX: Byte = 0x0f
    const val PSH_DAT: Byte = 0x10
    const val POP_DAT: Byte = 0x11
    const val JMP_SUB: Byte = 0x12
    const val RET_SUB: Byte = 0x13
    const val IND_DAT: Byte = 0x14
    const val IDX_DAT: Byte = 0x15
    const val MOD_DAT: Byte = 0x16
    const val SHL_DAT: Byte = 0x17
    const val SHR_DAT: Byte = 0x18
    const val JMP_ADR: Byte = 0x1a
    const val BZR_DAT: Byte = 0x1b
    const val BNZ_DAT: Byte = 0x1e
    const val BGT_DAT: Byte = 0x1f
    const val BLT_DAT: Byte = 0x20
    const val BGE_DAT: Byte = 0x21
    const val BLE_DAT: Byte = 0x22
    const val BEQ_DAT: Byte = 0x23
    const val BNE_DAT: Byte = 0x24
    const val SLP_DAT: Byte = 0x25
    const val FIZ_DAT: Byte = 0x26
    const val STZ_DAT: Byte = 0x27
    /** Finish (pc = pcs and stop) if address (1st argument) is zero */
    const val FIN_IMD: Byte = 0x28
    const val STP_IMD: Byte = 0x29
    const val SLP_IMD: Byte = 0x2a
    const val ERR_ADR: Byte = 0x2b
    const val SET_PCS: Byte = 0x30
    const val EXT_FUN: Byte = 0x32
    const val EXT_FUN_DAT: Byte = 0x33
    const val EXT_FUN_DAT_2: Byte = 0x34
    const val EXT_FUN_RET: Byte = 0x35
    const val EXT_FUN_RET_DAT: Byte = 0x36
    const val EXT_FUN_RET_DAT_2: Byte = 0x37
}
