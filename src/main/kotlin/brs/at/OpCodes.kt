/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.
*/

package brs.at

internal object OpCodes {
    const val E_OP_CODE_NOP: Byte = 0x7f
    const val E_OP_CODE_SET_VAL: Byte = 0x01
    const val E_OP_CODE_SET_DAT: Byte = 0x02
    const val E_OP_CODE_CLR_DAT: Byte = 0x03
    const val E_OP_CODE_INC_DAT: Byte = 0x04
    const val E_OP_CODE_DEC_DAT: Byte = 0x05
    const val E_OP_CODE_ADD_DAT: Byte = 0x06
    const val E_OP_CODE_SUB_DAT: Byte = 0x07
    const val E_OP_CODE_MUL_DAT: Byte = 0x08
    const val E_OP_CODE_DIV_DAT: Byte = 0x09
    const val E_OP_CODE_BOR_DAT: Byte = 0x0a
    const val E_OP_CODE_AND_DAT: Byte = 0x0b
    const val E_OP_CODE_XOR_DAT: Byte = 0x0c
    const val E_OP_CODE_NOT_DAT: Byte = 0x0d
    const val E_OP_CODE_SET_IND: Byte = 0x0e
    const val E_OP_CODE_SET_IDX: Byte = 0x0f
    const val E_OP_CODE_PSH_DAT: Byte = 0x10
    const val E_OP_CODE_POP_DAT: Byte = 0x11
    const val E_OP_CODE_JMP_SUB: Byte = 0x12
    const val E_OP_CODE_RET_SUB: Byte = 0x13
    const val E_OP_CODE_IND_DAT: Byte = 0x14
    const val E_OP_CODE_IDX_DAT: Byte = 0x15
    const val E_OP_CODE_MOD_DAT: Byte = 0x16
    const val E_OP_CODE_SHL_DAT: Byte = 0x17
    const val E_OP_CODE_SHR_DAT: Byte = 0x18
    const val E_OP_CODE_JMP_ADR: Byte = 0x1a
    const val E_OP_CODE_BZR_DAT: Byte = 0x1b
    const val E_OP_CODE_BNZ_DAT: Byte = 0x1e
    const val E_OP_CODE_BGT_DAT: Byte = 0x1f
    const val E_OP_CODE_BLT_DAT: Byte = 0x20
    const val E_OP_CODE_BGE_DAT: Byte = 0x21
    const val E_OP_CODE_BLE_DAT: Byte = 0x22
    const val E_OP_CODE_BEQ_DAT: Byte = 0x23
    const val E_OP_CODE_BNE_DAT: Byte = 0x24
    const val E_OP_CODE_SLP_DAT: Byte = 0x25
    const val E_OP_CODE_FIZ_DAT: Byte = 0x26
    const val E_OP_CODE_STZ_DAT: Byte = 0x27
    const val E_OP_CODE_FIN_IMD: Byte = 0x28
    const val E_OP_CODE_STP_IMD: Byte = 0x29
    const val E_OP_CODE_SLP_IMD: Byte = 0x2a
    const val E_OP_CODE_ERR_ADR: Byte = 0x2b
    const val E_OP_CODE_SET_PCS: Byte = 0x30
    const val E_OP_CODE_EXT_FUN: Byte = 0x32
    const val E_OP_CODE_EXT_FUN_DAT: Byte = 0x33
    const val E_OP_CODE_EXT_FUN_DAT_2: Byte = 0x34
    const val E_OP_CODE_EXT_FUN_RET: Byte = 0x35
    const val E_OP_CODE_EXT_FUN_RET_DAT: Byte = 0x36
    const val E_OP_CODE_EXT_FUN_RET_DAT_2: Byte = 0x37
}
