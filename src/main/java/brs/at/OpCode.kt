/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.
*/

package brs.at

internal object OpCode {

    val E_OP_CODE_NOP = java.lang.Byte.parseByte("7f", 16)
    val E_OP_CODE_SET_VAL = java.lang.Byte.parseByte("01", 16)
    val E_OP_CODE_SET_DAT = java.lang.Byte.parseByte("02", 16)
    val E_OP_CODE_CLR_DAT = java.lang.Byte.parseByte("03", 16)
    val E_OP_CODE_INC_DAT = java.lang.Byte.parseByte("04", 16)
    val E_OP_CODE_DEC_DAT = java.lang.Byte.parseByte("05", 16)
    val E_OP_CODE_ADD_DAT = java.lang.Byte.parseByte("06", 16)
    val E_OP_CODE_SUB_DAT = java.lang.Byte.parseByte("07", 16)
    val E_OP_CODE_MUL_DAT = java.lang.Byte.parseByte("08", 16)
    val E_OP_CODE_DIV_DAT = java.lang.Byte.parseByte("09", 16)
    val E_OP_CODE_BOR_DAT = java.lang.Byte.parseByte("0a", 16)
    val E_OP_CODE_AND_DAT = java.lang.Byte.parseByte("0b", 16)
    val E_OP_CODE_XOR_DAT = java.lang.Byte.parseByte("0c", 16)
    val E_OP_CODE_NOT_DAT = java.lang.Byte.parseByte("0d", 16)
    val E_OP_CODE_SET_IND = java.lang.Byte.parseByte("0e", 16)
    val E_OP_CODE_SET_IDX = java.lang.Byte.parseByte("0f", 16)
    val E_OP_CODE_PSH_DAT = java.lang.Byte.parseByte("10", 16)
    val E_OP_CODE_POP_DAT = java.lang.Byte.parseByte("11", 16)
    val E_OP_CODE_JMP_SUB = java.lang.Byte.parseByte("12", 16)
    val E_OP_CODE_RET_SUB = java.lang.Byte.parseByte("13", 16)
    val E_OP_CODE_IND_DAT = java.lang.Byte.parseByte("14", 16)
    val E_OP_CODE_IDX_DAT = java.lang.Byte.parseByte("15", 16)
    val E_OP_CODE_MOD_DAT = java.lang.Byte.parseByte("16", 16)
    val E_OP_CODE_SHL_DAT = java.lang.Byte.parseByte("17", 16)
    val E_OP_CODE_SHR_DAT = java.lang.Byte.parseByte("18", 16)
    val E_OP_CODE_JMP_ADR = java.lang.Byte.parseByte("1a", 16)
    val E_OP_CODE_BZR_DAT = java.lang.Byte.parseByte("1b", 16)
    val E_OP_CODE_BNZ_DAT = java.lang.Byte.parseByte("1e", 16)
    val E_OP_CODE_BGT_DAT = java.lang.Byte.parseByte("1f", 16)
    val E_OP_CODE_BLT_DAT = java.lang.Byte.parseByte("20", 16)
    val E_OP_CODE_BGE_DAT = java.lang.Byte.parseByte("21", 16)
    val E_OP_CODE_BLE_DAT = java.lang.Byte.parseByte("22", 16)
    val E_OP_CODE_BEQ_DAT = java.lang.Byte.parseByte("23", 16)
    val E_OP_CODE_BNE_DAT = java.lang.Byte.parseByte("24", 16)
    val E_OP_CODE_SLP_DAT = java.lang.Byte.parseByte("25", 16)
    val E_OP_CODE_FIZ_DAT = java.lang.Byte.parseByte("26", 16)
    val E_OP_CODE_STZ_DAT = java.lang.Byte.parseByte("27", 16)
    val E_OP_CODE_FIN_IMD = java.lang.Byte.parseByte("28", 16)
    val E_OP_CODE_STP_IMD = java.lang.Byte.parseByte("29", 16)
    val E_OP_CODE_SLP_IMD = java.lang.Byte.parseByte("2a", 16)
    val E_OP_CODE_ERR_ADR = java.lang.Byte.parseByte("2b", 16)
    val E_OP_CODE_SET_PCS = java.lang.Byte.parseByte("30", 16)
    val E_OP_CODE_EXT_FUN = java.lang.Byte.parseByte("32", 16)
    val E_OP_CODE_EXT_FUN_DAT = java.lang.Byte.parseByte("33", 16)
    val E_OP_CODE_EXT_FUN_DAT_2 = java.lang.Byte.parseByte("34", 16)
    val E_OP_CODE_EXT_FUN_RET = java.lang.Byte.parseByte("35", 16)
    val E_OP_CODE_EXT_FUN_RET_DAT = java.lang.Byte.parseByte("36", 16)
    val E_OP_CODE_EXT_FUN_RET_DAT_2 = java.lang.Byte.parseByte("37", 16)
}
