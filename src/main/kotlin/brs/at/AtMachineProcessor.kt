/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.
*/

package brs.at

import brs.entity.DependencyProvider

internal class AtMachineProcessor(
    private val dp: DependencyProvider,
    private val machineData: AtMachineState
) {
    private val func = Fun()

    private val addrs: Int
        get() {
            if (machineData.machineState.pc + 4 + 4 >= machineData.cSize) {
                return -1
            }

            func.addr1 = machineData.apCode.getInt(machineData.machineState.pc + 1)
            func.addr2 = machineData.apCode.getInt(machineData.machineState.pc + 1 + 4)
            return if (!validAddr(func.addr1, false) || !validAddr(func.addr2, false)) {
                -1
            } else 0
        }

    private val addrOff: Int
        get() {
            if (machineData.machineState.pc + 5 >= machineData.cSize) {
                return -1
            }

            func.addr1 = machineData.apCode.getInt(machineData.machineState.pc + 1)
            func.off = machineData.apCode.get(machineData.machineState.pc + 5)
            return if (!validAddr(func.addr1, false) || !validAddr(machineData.machineState.pc + func.off, true)) {
                -1
            } else 0
        }

    private val addrsOff: Int
        get() {
            if (machineData.machineState.pc + 9 >= machineData.cSize) {
                return -1
            }

            func.addr1 = machineData.apCode.getInt(machineData.machineState.pc + 1)
            func.addr2 = machineData.apCode.getInt(machineData.machineState.pc + 5)
            func.off = machineData.apCode.get(machineData.machineState.pc + 9)

            return if (!validAddr(func.addr1, false) ||
                !validAddr(func.addr2, false) ||
                !validAddr(machineData.machineState.pc + func.off, true)
            ) {
                -1
            } else 0
        }

    private val funAddr: Int
        get() {
            if (machineData.machineState.pc + 4 + 4 >= machineData.cSize) {
                return -1
            }

            func.func = machineData.apCode.getShort(machineData.machineState.pc + 1)
            func.addr1 = machineData.apCode.getInt(machineData.machineState.pc + 1 + 2)
            return if (!validAddr(func.addr1, false)) {
                -1
            } else 0
        }

    private val funAddrs: Int
        get() {
            if (machineData.machineState.pc + 4 + 4 + 2 >= machineData.cSize) {
                return -1
            }

            func.func = machineData.apCode.getShort(machineData.machineState.pc + 1)
            func.addr3 = machineData.apCode.getInt(machineData.machineState.pc + 1 + 2)
            func.addr2 = machineData.apCode.getInt(machineData.machineState.pc + 1 + 2 + 4)

            return if (!validAddr(func.addr3, false) || !validAddr(func.addr2, false)) {
                -1
            } else 0
        }

    private val addressVal: Int
        get() {
            if (machineData.machineState.pc + 4 + 8 >= machineData.cSize) {
                return -1
            }

            func.addr1 = machineData.apCode.getInt(machineData.machineState.pc + 1)
            func.value = machineData.apCode.getLong(machineData.machineState.pc + 1 + 4)

            return if (!validAddr(func.addr1, false)) {
                -1
            } else 0
        }

    private fun getFun(): Int {
        if (machineData.machineState.pc + 2 >= machineData.cSize)
            return -1
        else {
            func.func = machineData.apCode.getShort(machineData.machineState.pc + 1)
        }

        return 0
    }

    private fun getAddr(isCode: Boolean): Int {
        if (machineData.machineState.pc + 4 >= machineData.cSize) {
            return -1
        }

        func.addr1 = machineData.apCode.getInt(machineData.apCode.position() + machineData.machineState.pc + 1)
        return if (!validAddr(func.addr1, isCode)) {
            -1
        } else 0
    }

    private fun validAddr(addr: Int, isCode: Boolean): Boolean {
        if (addr < 0) {
            return false
        }

        return if (!isCode && (addr.toLong() * 8 + 8 > Integer.MAX_VALUE.toLong() || addr * 8 + 8 > machineData.dSize)) {
            false
        } else !isCode || addr < machineData.cSize
    }

    fun processOp(): Int {
        var rc = 0
        if (machineData.cSize < 1 || machineData.machineState.pc >= machineData.cSize) return 0
        // This is because of a Kotlin bug (KT-36047) that causes the compiler to not produce an optimized TABLESWITCH instruction when this is inlined.
        @Suppress("MoveVariableDeclarationIntoWhen")
        val op = machineData.apCode.get(machineData.machineState.pc)
        when (op) {
            OpCodes.E_OP_CODE_SET_VAL -> {
                rc = addressVal
                if (rc == 0) {
                    rc = 13
                    machineData.machineState.pc += rc
                    machineData.apData.putLong(func.addr1 * 8, func.value)
                    machineData.apData.clear()
                }
            }
            OpCodes.E_OP_CODE_SET_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    machineData.machineState.pc += rc
                    machineData.apData.putLong(func.addr1 * 8, machineData.apData.getLong(func.addr2 * 8)) // HERE
                    machineData.apData.clear()
                }
            }
            OpCodes.E_OP_CODE_CLR_DAT -> {
                rc = getAddr(false)

                if (rc == 0) {
                    rc = 5
                    machineData.machineState.pc += rc
                    machineData.apData.putLong(func.addr1 * 8, 0.toLong())
                    machineData.apData.clear()
                }
            }
            OpCodes.E_OP_CODE_INC_DAT, OpCodes.E_OP_CODE_DEC_DAT, OpCodes.E_OP_CODE_NOT_DAT -> {
                rc = getAddr(false)
                if (rc == 0) {
                    rc = 5
                    machineData.machineState.pc += rc
                    val incData = machineData.apData.getLong(func.addr1 * 8)
                    when (op) {
                        OpCodes.E_OP_CODE_INC_DAT -> machineData.apData.putLong(func.addr1 * 8, incData + 1)
                        OpCodes.E_OP_CODE_DEC_DAT -> machineData.apData.putLong(func.addr1 * 8, incData - 1)
                        OpCodes.E_OP_CODE_NOT_DAT -> machineData.apData.putLong(func.addr1 * 8, incData.inv())
                    }
                    machineData.apData.clear()
                }
            }
            OpCodes.E_OP_CODE_ADD_DAT, OpCodes.E_OP_CODE_SUB_DAT, OpCodes.E_OP_CODE_MUL_DAT, OpCodes.E_OP_CODE_DIV_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    val value = machineData.apData.getLong(func.addr2 * 8)
                    if (op == OpCodes.E_OP_CODE_DIV_DAT && value == 0L)
                        rc = -2
                    else {
                        machineData.machineState.pc += rc
                        val addData1 = machineData.apData.getLong(func.addr1 * 8)
                        val addData2 = machineData.apData.getLong(func.addr2 * 8)
                        when (op) {
                            OpCodes.E_OP_CODE_ADD_DAT -> machineData.apData.putLong(func.addr1 * 8, addData1 + addData2)
                            OpCodes.E_OP_CODE_SUB_DAT -> machineData.apData.putLong(func.addr1 * 8, addData1 - addData2)
                            OpCodes.E_OP_CODE_MUL_DAT -> machineData.apData.putLong(func.addr1 * 8, addData1 * addData2)
                            OpCodes.E_OP_CODE_DIV_DAT -> machineData.apData.putLong(func.addr1 * 8, addData1 / addData2)
                        }
                        machineData.apData.clear()
                    }
                }
            }
            OpCodes.E_OP_CODE_BOR_DAT, OpCodes.E_OP_CODE_AND_DAT, OpCodes.E_OP_CODE_XOR_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    machineData.machineState.pc += rc
                    val value = machineData.apData.getLong(func.addr2 * 8)
                    val incData = machineData.apData.getLong(func.addr1 * 8)
                    when (op) {
                        OpCodes.E_OP_CODE_BOR_DAT -> machineData.apData.putLong(func.addr1 * 8, incData or value)
                        OpCodes.E_OP_CODE_AND_DAT -> machineData.apData.putLong(func.addr1 * 8, incData and value)
                        OpCodes.E_OP_CODE_XOR_DAT -> machineData.apData.putLong(func.addr1 * 8, incData xor value)
                    }
                    machineData.apData.clear()
                }
            }
            OpCodes.E_OP_CODE_SET_IND -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    val addr = machineData.apData.getLong(func.addr2 * 8)

                    if (!validAddr(addr.toInt(), false)) rc = -1 else {
                        machineData.machineState.pc += rc
                        val value = machineData.apData.getLong(addr.toInt() * 8)
                        machineData.apData.putLong(func.addr1 * 8, value)
                        machineData.apData.clear()
                    }
                }
            }
            OpCodes.E_OP_CODE_SET_IDX -> {
                val addr1 = func.addr1
                val addr2 = func.addr2
                val size = 8

                rc = addrs

                if (rc == 0) {
                    machineData.apCode.position(size)
                    rc = getAddr(false)
                    machineData.apCode.position(machineData.apCode.position() - size)

                    if (rc == 0) {
                        rc = 13
                        val base = machineData.apData.getLong(addr2 * 8)
                        val offs = machineData.apData.getLong(func.addr1 * 8)

                        val addr = base + offs

                        if (!validAddr(addr.toInt(), false)) {
                            rc = -1
                        } else {
                            machineData.machineState.pc += rc
                            machineData.apData.putLong(addr1 * 8, machineData.apData.getLong(addr.toInt() * 8))
                            machineData.apData.clear()
                        }
                    }
                }
            }
            OpCodes.E_OP_CODE_PSH_DAT, OpCodes.E_OP_CODE_POP_DAT -> {
                rc = getAddr(false)
                if (rc == 0) {
                    rc = 5
                    if (op == OpCodes.E_OP_CODE_PSH_DAT && machineData.machineState.us == machineData.cUserStackBytes / 8 || op == OpCodes.E_OP_CODE_POP_DAT && machineData.machineState.us == 0) {
                        rc = -1
                    } else {
                        machineData.machineState.pc += rc
                        if (op == OpCodes.E_OP_CODE_PSH_DAT) {
                            val value = machineData.apData.getLong(func.addr1 * 8)
                            machineData.machineState.us++
                            machineData.apData.putLong(
                                machineData.dSize +
                                        machineData.cCallStackBytes +
                                        machineData.cUserStackBytes - machineData.machineState.us * 8, value
                            )
                            machineData.apData.clear()
                        } else {
                            val value = machineData.apData.getLong(
                                machineData.dSize +
                                        machineData.cCallStackBytes +
                                        machineData.cUserStackBytes - machineData.machineState.us * 8
                            )
                            machineData.machineState.us--
                            machineData.apData.putLong(func.addr1 * 8, value)
                            machineData.apData.clear()
                        }
                    }
                }
            }
            OpCodes.E_OP_CODE_JMP_SUB -> {
                rc = getAddr(true)

                if (rc == 0) {
                    rc = 5
                    when {
                        machineData.machineState.cs == machineData.cCallStackBytes / 8 -> rc = -1
                        machineData.machineState.jumps.contains(func.addr1) -> {
                            machineData.machineState.cs++
                            machineData.apData.putLong(
                                machineData.dSize + machineData.cCallStackBytes - machineData.machineState.cs * 8,
                                (machineData.machineState.pc + rc).toLong()
                            )
                            machineData.apData.clear()
                            machineData.machineState.pc = func.addr1
                        }
                        else -> rc = -2
                    }
                }
            }
            OpCodes.E_OP_CODE_RET_SUB -> {
                rc = 1

                if (machineData.machineState.cs == 0) rc = -1 else {
                    val value = machineData.apData.getLong(machineData.dSize + machineData.cCallStackBytes - machineData.machineState.cs * 8)
                    machineData.machineState.cs--
                    val addr = value.toInt()
                    if (machineData.machineState.jumps.contains(addr))
                        machineData.machineState.pc = addr
                    else
                        rc = -2
                }
            }
            OpCodes.E_OP_CODE_IND_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    val addr = machineData.apData.getLong(func.addr1 * 8)

                    if (!validAddr(addr.toInt(), false)) rc = -1 else {
                        machineData.machineState.pc += rc
                        machineData.apData.putLong(addr.toInt() * 8, machineData.apData.getLong(func.addr2 * 8))
                        machineData.apData.clear()
                    }
                }
            }
            OpCodes.E_OP_CODE_IDX_DAT -> {
                val addr1 = func.addr1
                val addr2 = func.addr2
                val size = 8

                rc = addrs

                if (rc == 0) {
                    machineData.apCode.position(size)
                    rc = getAddr(false)
                    machineData.apCode.position(machineData.apCode.position() - size)

                    if (rc == 0) {
                        rc = 13
                        val addr = machineData.apData.getLong(addr1 * 8) + machineData.apData.getLong(addr2 * 8)

                        if (!validAddr(addr.toInt(), false))
                            rc = -1
                        else {
                            machineData.machineState.pc += rc
                            machineData.apData.putLong(
                                addr.toInt() * 8,
                                machineData.apData.getLong(func.addr1 * 8)
                            )
                            machineData.apData.clear()
                        }
                    }
                }
            }
            OpCodes.E_OP_CODE_MOD_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    val modData1 = machineData.apData.getLong(func.addr1 * 8)
                    val modData2 = machineData.apData.getLong(func.addr2 * 8)

                    if (modData2 == 0L)
                        rc = -2
                    else {
                        machineData.machineState.pc += rc
                        machineData.apData.putLong(func.addr1 * 8, modData1 % modData2)
                    }
                }
            }
            OpCodes.E_OP_CODE_SHL_DAT, OpCodes.E_OP_CODE_SHR_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    machineData.machineState.pc += rc
                    val value = machineData.apData.getLong(func.addr1 * 8)
                    val shift = machineData.apData.getLong(func.addr2 * 8).coerceIn(0L, 63L).toInt()

                    if (op == OpCodes.E_OP_CODE_SHL_DAT)
                        machineData.apData.putLong(func.addr1 * 8, value shl shift)
                    else
                        machineData.apData.putLong(func.addr1 * 8, value ushr shift)
                }
            }
            OpCodes.E_OP_CODE_JMP_ADR -> {
                rc = getAddr(true)

                if (rc == 0) {
                    rc = 5
                    if (machineData.machineState.jumps.contains(func.addr1))
                        machineData.machineState.pc = func.addr1
                    else
                        rc = -2
                }
            }
            OpCodes.E_OP_CODE_BZR_DAT, OpCodes.E_OP_CODE_BNZ_DAT -> {
                rc = addrOff

                if (rc == 0) {
                    rc = 6
                    val value = machineData.apData.getLong(func.addr1 * 8)
                    if (op == OpCodes.E_OP_CODE_BZR_DAT && value == 0L || op == OpCodes.E_OP_CODE_BNZ_DAT && value != 0L) {
                        if (machineData.machineState.jumps.contains(machineData.machineState.pc + func.off))
                            machineData.machineState.pc += func.off.toInt()
                        else
                            rc = -2
                    } else
                        machineData.machineState.pc += rc
                }
            }
            OpCodes.E_OP_CODE_BGT_DAT, OpCodes.E_OP_CODE_BLT_DAT, OpCodes.E_OP_CODE_BGE_DAT, OpCodes.E_OP_CODE_BLE_DAT, OpCodes.E_OP_CODE_BEQ_DAT, OpCodes.E_OP_CODE_BNE_DAT -> {
                rc = addrsOff

                if (rc == 0) {
                    rc = 10
                    val val1 = machineData.apData.getLong(func.addr1 * 8)
                    val val2 = machineData.apData.getLong(func.addr2 * 8)

                    if (op == OpCodes.E_OP_CODE_BGT_DAT && val1 > val2 ||
                        op == OpCodes.E_OP_CODE_BLT_DAT && val1 < val2 ||
                        op == OpCodes.E_OP_CODE_BGE_DAT && val1 >= val2 ||
                        op == OpCodes.E_OP_CODE_BLE_DAT && val1 <= val2 ||
                        op == OpCodes.E_OP_CODE_BEQ_DAT && val1 == val2 ||
                        op == OpCodes.E_OP_CODE_BNE_DAT && val1 != val2
                    ) {
                        if (machineData.machineState.jumps.contains(machineData.machineState.pc + func.off))
                            machineData.machineState.pc += func.off.toInt()
                        else
                            rc = -2
                    } else
                        machineData.machineState.pc += rc
                }
            }
            OpCodes.E_OP_CODE_SLP_DAT -> {
                rc = getAddr(true)

                if (rc == 0) {
                    rc = 5

                    machineData.machineState.pc += rc
                    var numBlocks = machineData.apData.getLong(func.addr1 * 8).toInt()
                    if (numBlocks < 0)
                        numBlocks = 0
                    val maxNumBlocks =
                        dp.atConstants[machineData.creationBlockHeight].maxWaitForNumOfBlocks.toInt()
                    if (numBlocks > maxNumBlocks)
                        numBlocks = maxNumBlocks
                    machineData.waitForNumberOfBlocks = numBlocks
                    machineData.machineState.stopped = true
                }
            }
            OpCodes.E_OP_CODE_FIZ_DAT, OpCodes.E_OP_CODE_STZ_DAT -> {
                rc = getAddr(false)

                if (rc == 0) {
                    rc = 5
                    if (machineData.apData.getLong(func.addr1 * 8) == 0L) {
                        if (op == OpCodes.E_OP_CODE_STZ_DAT) {
                            machineData.machineState.pc += rc
                            machineData.machineState.stopped = true
                        } else {
                            machineData.machineState.pc = machineData.machineState.pcs
                            machineData.machineState.finished = true
                        }
                        machineData.setFreeze(true)
                    } else {
                        rc = 5
                        machineData.machineState.pc += rc
                    }
                }
            }
            OpCodes.E_OP_CODE_FIN_IMD, OpCodes.E_OP_CODE_STP_IMD -> {
                rc = 1

                if (op == OpCodes.E_OP_CODE_STP_IMD) {
                    machineData.machineState.pc += rc
                    machineData.machineState.stopped = true
                } else {
                    machineData.machineState.pc = machineData.machineState.pcs
                    machineData.machineState.finished = true
                }
                machineData.setFreeze(true)
            }
            OpCodes.E_OP_CODE_SLP_IMD -> {
                rc = 1

                machineData.machineState.pc += rc
                machineData.machineState.stopped = true
                machineData.setFreeze(true)
            }
            OpCodes.E_OP_CODE_SET_PCS -> {
                rc = 1

                machineData.machineState.pc += rc
                machineData.machineState.pcs = machineData.machineState.pc
            }
            OpCodes.E_OP_CODE_EXT_FUN -> {
                rc = getFun()

                if (rc == 0) {
                    rc = 3

                    machineData.machineState.pc += rc
                    dp.atApiController.func(func.func.toInt(), machineData)
                }
            }
            OpCodes.E_OP_CODE_EXT_FUN_DAT -> {
                rc = funAddr
                if (rc == 0) {
                    rc = 7

                    machineData.machineState.pc += rc
                    val value = machineData.apData.getLong(func.addr1 * 8)
                    dp.atApiController.func1(func.func.toInt(), value, machineData)
                }
            }
            OpCodes.E_OP_CODE_EXT_FUN_DAT_2 -> {
                rc = funAddrs

                if (rc == 0) {
                    rc = 11

                    machineData.machineState.pc += rc
                    val val1 = machineData.apData.getLong(func.addr3 * 8)
                    val val2 = machineData.apData.getLong(func.addr2 * 8)
                    dp.atApiController.func2(func.func.toInt(), val1, val2, machineData)
                }
            }
            OpCodes.E_OP_CODE_EXT_FUN_RET -> {
                rc = funAddr

                if (rc == 0) {
                    rc = 7

                    machineData.machineState.pc += rc
                    machineData.apData.putLong(
                        func.addr1 * 8,
                        dp.atApiController.func(func.func.toInt(), machineData)
                    )
                    machineData.apData.clear()
                }
            }
            OpCodes.E_OP_CODE_EXT_FUN_RET_DAT, OpCodes.E_OP_CODE_EXT_FUN_RET_DAT_2 -> {
                rc = funAddrs
                val size = 10

                if ((rc == 0) && op == OpCodes.E_OP_CODE_EXT_FUN_RET_DAT_2) {
                    machineData.apCode.position(size)
                    rc = getAddr(false)
                    machineData.apCode.position(machineData.apCode.position() - size)
                }

                if (rc == 0) {
                    rc = 1 + size + if (op == OpCodes.E_OP_CODE_EXT_FUN_RET_DAT_2) 4 else 0

                    machineData.machineState.pc += rc
                    val value = machineData.apData.getLong(func.addr2 * 8)

                    if (op != OpCodes.E_OP_CODE_EXT_FUN_RET_DAT_2)
                        machineData.apData.putLong(func.addr3 * 8, dp.atApiController.func1(func.func.toInt(), value, machineData))
                    else {
                        val val2 = machineData.apData.getLong(func.addr1 * 8)
                        machineData.apData.putLong(func.addr3 * 8, dp.atApiController.func2(func.func.toInt(), value, val2, machineData))
                    }
                    machineData.apData.clear()
                }
            }
            OpCodes.E_OP_CODE_ERR_ADR -> {
                getAddr(true)

                // don't check rc to allow for unsetting handler with -1
                rc = 5

                if (func.addr1 == -1 || machineData.machineState.jumps.contains(func.addr1)) {
                    machineData.machineState.pc += rc
                    machineData.machineState.err = func.addr1
                } else
                    rc = -2
            }
            else -> {
                // This is here because otherwise the when statement above becomes a lookup switch (instead of a table switch)
                // which is O(log n) whereas table switch is O(1) so is faster.
                if (op == OpCodes.E_OP_CODE_NOP) {
                    ++rc
                    ++machineData.machineState.pc
                }
                rc = -2
            }
        }

        return rc
    }

    fun simulateOp(): Int {
        var rc = 0
        if (machineData.cSize < 1 || machineData.machineState.pc >= machineData.cSize) return 0
        machineData.machineState.jumps.add(machineData.machineState.pc)
        // This is because of a Kotlin bug (KT-36047) that causes the compiler to not produce an optimized TABLESWITCH instruction when this is inlined.
        @Suppress("MoveVariableDeclarationIntoWhen")
        val op = machineData.apCode.get(machineData.machineState.pc)
        when (op) {
            OpCodes.E_OP_CODE_SET_VAL -> {
                addressVal
                rc = 13
            }
            OpCodes.E_OP_CODE_CLR_DAT,
            OpCodes.E_OP_CODE_DEC_DAT,
            OpCodes.E_OP_CODE_FIZ_DAT,
            OpCodes.E_OP_CODE_INC_DAT,
            OpCodes.E_OP_CODE_NOT_DAT,
            OpCodes.E_OP_CODE_POP_DAT,
            OpCodes.E_OP_CODE_PSH_DAT,
            OpCodes.E_OP_CODE_STZ_DAT -> {
                getAddr(false)
                rc = 5
            }
            OpCodes.E_OP_CODE_ADD_DAT,
            OpCodes.E_OP_CODE_AND_DAT,
            OpCodes.E_OP_CODE_BOR_DAT,
            OpCodes.E_OP_CODE_DIV_DAT,
            OpCodes.E_OP_CODE_MOD_DAT,
            OpCodes.E_OP_CODE_MUL_DAT,
            OpCodes.E_OP_CODE_SET_DAT,
            OpCodes.E_OP_CODE_SHL_DAT,
            OpCodes.E_OP_CODE_SHR_DAT,
            OpCodes.E_OP_CODE_SUB_DAT,
            OpCodes.E_OP_CODE_XOR_DAT -> {
                addrs
                rc = 9
            }
            OpCodes.E_OP_CODE_IND_DAT, OpCodes.E_OP_CODE_SET_IND -> {
                rc = addrs
                if (rc == 0) rc = 9
            }
            OpCodes.E_OP_CODE_SET_IDX -> {
                machineData.apCode.position(8)
                rc = getAddr(false)
                machineData.apCode.position(machineData.apCode.position() - 8)
            }
            OpCodes.E_OP_CODE_SLP_DAT, OpCodes.E_OP_CODE_JMP_ADR, OpCodes.E_OP_CODE_ERR_ADR, OpCodes.E_OP_CODE_JMP_SUB -> {
                getAddr(true)
                rc = 5
            }
            OpCodes.E_OP_CODE_RET_SUB -> {
                rc = 1
            }
            OpCodes.E_OP_CODE_IDX_DAT -> {
                addrs
                machineData.apCode.position(8)
                rc = getAddr(false)
                machineData.apCode.position(machineData.apCode.position() - 8)
            }
            OpCodes.E_OP_CODE_BZR_DAT, OpCodes.E_OP_CODE_BNZ_DAT -> {
                addrOff
                rc = 6
            }
            OpCodes.E_OP_CODE_BGT_DAT,
            OpCodes.E_OP_CODE_BLT_DAT,
            OpCodes.E_OP_CODE_BGE_DAT,
            OpCodes.E_OP_CODE_BLE_DAT,
            OpCodes.E_OP_CODE_BEQ_DAT,
            OpCodes.E_OP_CODE_BNE_DAT -> {
                addrsOff
                rc = 10
            }
            OpCodes.E_OP_CODE_FIN_IMD,
            OpCodes.E_OP_CODE_STP_IMD,
            OpCodes.E_OP_CODE_SLP_IMD,
            OpCodes.E_OP_CODE_SET_PCS -> {
                rc = 1
            }
            OpCodes.E_OP_CODE_EXT_FUN -> {
                getFun()
                rc = 3
            }
            OpCodes.E_OP_CODE_EXT_FUN_DAT -> {
                rc = funAddr
                if (rc == 0) rc = 7
            }
            OpCodes.E_OP_CODE_EXT_FUN_DAT_2 -> {
                funAddrs
                rc = 11
            }
            OpCodes.E_OP_CODE_EXT_FUN_RET -> {
                funAddr
                rc = 7
            }
            OpCodes.E_OP_CODE_EXT_FUN_RET_DAT, OpCodes.E_OP_CODE_EXT_FUN_RET_DAT_2 -> {
                rc = funAddrs
                val size = 10

                if (op == OpCodes.E_OP_CODE_EXT_FUN_RET_DAT_2) {
                    machineData.apCode.position(size)
                    rc = getAddr(false)
                    machineData.apCode.position(machineData.apCode.position() - size)
                }

                if (rc == 0) {
                    rc = 1 + size + if (op == OpCodes.E_OP_CODE_EXT_FUN_RET_DAT_2) 4 else 0
                }
            }
            else -> {
                // This is here because otherwise the when statement above becomes a lookup switch (instead of a table switch)
                // which is O(log n) whereas table switch is O(1) so is faster.
                if (op == OpCodes.E_OP_CODE_NOP) {
                    ++rc
                }
            }
        }

        return rc
    }

    private inner class Fun {
        internal var func: Short = 0
        internal var addr1: Int = 0
        internal var addr2: Int = 0
        internal var value: Long = 0
        internal var off: Byte = 0
        internal var addr3: Int = 0
    }
}
