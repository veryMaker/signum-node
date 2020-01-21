/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.
*/

package brs.at

import brs.entity.DependencyProvider
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

internal class AtMachineProcessor(
    private val dp: DependencyProvider,
    private val machineData: AtMachineState
) {
    private val func = Fun()

    private val addrs: Int
        get() {
            if (machineData.machineState.programCounter + 4 + 4 >= machineData.cSize) {
                return -1
            }

            func.addr1 = machineData.apCode.getInt(machineData.machineState.programCounter + 1)
            func.addr2 = machineData.apCode.getInt(machineData.machineState.programCounter + 1 + 4)
            return if (!validAddr(func.addr1, false) || !validAddr(func.addr2, false)) {
                -1
            } else 0
        }

    private val addrOff: Int
        get() {
            if (machineData.machineState.programCounter + 5 >= machineData.cSize) {
                return -1
            }

            func.addr1 = machineData.apCode.getInt(machineData.machineState.programCounter + 1)
            func.off = machineData.apCode.get(machineData.machineState.programCounter + 5)
            return if (!validAddr(func.addr1, false) || !validAddr(machineData.machineState.programCounter + func.off, true)) {
                -1
            } else 0
        }

    private val addrsOff: Int
        get() {
            if (machineData.machineState.programCounter + 9 >= machineData.cSize) {
                return -1
            }

            func.addr1 = machineData.apCode.getInt(machineData.machineState.programCounter + 1)
            func.addr2 = machineData.apCode.getInt(machineData.machineState.programCounter + 5)
            func.off = machineData.apCode.get(machineData.machineState.programCounter + 9)

            return if (!validAddr(func.addr1, false) ||
                !validAddr(func.addr2, false) ||
                !validAddr(machineData.machineState.programCounter + func.off, true)
            ) {
                -1
            } else 0
        }

    private val funAddr: Int
        get() {
            if (machineData.machineState.programCounter + 4 + 4 >= machineData.cSize) {
                return -1
            }

            func.func = machineData.apCode.getShort(machineData.machineState.programCounter + 1)
            func.addr1 = machineData.apCode.getInt(machineData.machineState.programCounter + 1 + 2)
            return if (!validAddr(func.addr1, false)) {
                -1
            } else 0
        }

    private val funAddrs: Int
        get() {
            if (machineData.machineState.programCounter + 4 + 4 + 2 >= machineData.cSize) {
                return -1
            }

            func.func = machineData.apCode.getShort(machineData.machineState.programCounter + 1)
            func.addr3 = machineData.apCode.getInt(machineData.machineState.programCounter + 1 + 2)
            func.addr2 = machineData.apCode.getInt(machineData.machineState.programCounter + 1 + 2 + 4)

            return if (!validAddr(func.addr3, false) || !validAddr(func.addr2, false)) {
                -1
            } else 0
        }

    private val addressVal: Int
        get() {
            if (machineData.machineState.programCounter + 4 + 8 >= machineData.cSize) {
                return -1
            }

            func.addr1 = machineData.apCode.getInt(machineData.machineState.programCounter + 1)
            func.value = machineData.apCode.getLong(machineData.machineState.programCounter + 1 + 4)

            return if (!validAddr(func.addr1, false)) {
                -1
            } else 0
        }

    private fun getFun(): Int {
        if (machineData.machineState.programCounter + 2 >= machineData.cSize)
            return -1
        else {
            func.func = machineData.apCode.getShort(machineData.machineState.programCounter + 1)
        }

        return 0
    }

    private fun getAddr(isCode: Boolean): Int {
        if (machineData.machineState.programCounter + 4 >= machineData.cSize) {
            return -1
        }

        func.addr1 = machineData.apCode.getInt(machineData.apCode.position() + machineData.machineState.programCounter + 1)
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
        if (machineData.cSize < 1 || machineData.machineState.programCounter >= machineData.cSize) return 0
        // This is because of a Kotlin bug (KT-36047) that causes the compiler to not produce an optimized TABLESWITCH instruction when this is inlined.
        @Suppress("MoveVariableDeclarationIntoWhen")
        val op = machineData.apCode.get(machineData.machineState.programCounter)
        when (op) {
            OpCodes.SET_VAL -> {
                rc = addressVal
                if (rc == 0) {
                    rc = 13
                    machineData.machineState.programCounter += rc
                    machineData.apData.putLong(func.addr1 * 8, func.value)
                    machineData.apData.clear()
                }
            }
            OpCodes.SET_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    machineData.machineState.programCounter += rc
                    machineData.apData.putLong(func.addr1 * 8, machineData.apData.getLong(func.addr2 * 8)) // HERE
                    machineData.apData.clear()
                }
            }
            OpCodes.CLR_DAT -> {
                rc = getAddr(false)

                if (rc == 0) {
                    rc = 5
                    machineData.machineState.programCounter += rc
                    machineData.apData.putLong(func.addr1 * 8, 0.toLong())
                    machineData.apData.clear()
                }
            }
            OpCodes.INC_DAT, OpCodes.DEC_DAT, OpCodes.NOT_DAT -> {
                rc = getAddr(false)
                if (rc == 0) {
                    rc = 5
                    machineData.machineState.programCounter += rc
                    val incData = machineData.apData.getLong(func.addr1 * 8)
                    when (op) {
                        OpCodes.INC_DAT -> machineData.apData.putLong(func.addr1 * 8, incData + 1)
                        OpCodes.DEC_DAT -> machineData.apData.putLong(func.addr1 * 8, incData - 1)
                        OpCodes.NOT_DAT -> machineData.apData.putLong(func.addr1 * 8, incData.inv())
                    }
                    machineData.apData.clear()
                }
            }
            OpCodes.ADD_DAT, OpCodes.SUB_DAT, OpCodes.MUL_DAT, OpCodes.DIV_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    val value = machineData.apData.getLong(func.addr2 * 8)
                    if (op == OpCodes.DIV_DAT && value == 0L)
                        rc = -2
                    else {
                        machineData.machineState.programCounter += rc
                        val addData1 = machineData.apData.getLong(func.addr1 * 8)
                        val addData2 = machineData.apData.getLong(func.addr2 * 8)
                        when (op) {
                            OpCodes.ADD_DAT -> machineData.apData.putLong(func.addr1 * 8, addData1 + addData2)
                            OpCodes.SUB_DAT -> machineData.apData.putLong(func.addr1 * 8, addData1 - addData2)
                            OpCodes.MUL_DAT -> machineData.apData.putLong(func.addr1 * 8, addData1 * addData2)
                            OpCodes.DIV_DAT -> machineData.apData.putLong(func.addr1 * 8, addData1 / addData2)
                        }
                        machineData.apData.clear()
                    }
                }
            }
            OpCodes.BOR_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    machineData.machineState.programCounter += rc
                    val apData = machineData.apData.array()
                    val firstAddress = func.addr1 * 8
                    val secondAddress = func.addr2 * 8
                    for (i in firstAddress..firstAddress+7) {
                        apData[i] = apData[i] or apData[secondAddress+i]
                    }
                    machineData.apData.clear()
                }
            }
            OpCodes.AND_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    machineData.machineState.programCounter += rc
                    val apData = machineData.apData.array()
                    val firstAddress = func.addr1 * 8
                    val secondAddress = func.addr2 * 8
                    for (i in firstAddress..firstAddress+7) {
                        apData[i] = apData[i] and apData[secondAddress+i]
                    }
                    machineData.apData.clear()
                }
            }
            OpCodes.XOR_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    machineData.machineState.programCounter += rc
                    val apData = machineData.apData.array()
                    val firstAddress = func.addr1 * 8
                    val secondAddress = func.addr2 * 8
                    for (i in firstAddress..firstAddress+7) {
                        apData[i] = apData[i] xor apData[secondAddress+i]
                    }
                    machineData.apData.clear()
                }
            }
            OpCodes.SET_IND -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    val addr = machineData.apData.getLong(func.addr2 * 8)

                    if (!validAddr(addr.toInt(), false)) rc = -1 else {
                        machineData.machineState.programCounter += rc
                        val value = machineData.apData.getLong(addr.toInt() * 8)
                        machineData.apData.putLong(func.addr1 * 8, value)
                        machineData.apData.clear()
                    }
                }
            }
            OpCodes.SET_IDX -> {
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
                            machineData.machineState.programCounter += rc
                            machineData.apData.putLong(addr1 * 8, machineData.apData.getLong(addr.toInt() * 8))
                            machineData.apData.clear()
                        }
                    }
                }
            }
            OpCodes.PSH_DAT, OpCodes.POP_DAT -> {
                rc = getAddr(false)
                if (rc == 0) {
                    rc = 5
                    if (op == OpCodes.PSH_DAT && machineData.machineState.userStackCounter == machineData.cUserStackBytes / 8 || op == OpCodes.POP_DAT && machineData.machineState.userStackCounter == 0) {
                        rc = -1
                    } else {
                        machineData.machineState.programCounter += rc
                        if (op == OpCodes.PSH_DAT) {
                            val value = machineData.apData.getLong(func.addr1 * 8)
                            machineData.machineState.userStackCounter++
                            machineData.apData.putLong(
                                machineData.dSize +
                                        machineData.cCallStackBytes +
                                        machineData.cUserStackBytes - machineData.machineState.userStackCounter * 8, value
                            )
                            machineData.apData.clear()
                        } else {
                            val value = machineData.apData.getLong(
                                machineData.dSize +
                                        machineData.cCallStackBytes +
                                        machineData.cUserStackBytes - machineData.machineState.userStackCounter * 8
                            )
                            machineData.machineState.userStackCounter--
                            machineData.apData.putLong(func.addr1 * 8, value)
                            machineData.apData.clear()
                        }
                    }
                }
            }
            OpCodes.JMP_SUB -> {
                rc = getAddr(true)

                if (rc == 0) {
                    rc = 5
                    when {
                        machineData.machineState.callStackCounter == machineData.cCallStackBytes / 8 -> rc = -1
                        machineData.machineState.jumps.contains(func.addr1) -> {
                            machineData.machineState.callStackCounter++
                            machineData.apData.putLong(
                                machineData.dSize + machineData.cCallStackBytes - machineData.machineState.callStackCounter * 8,
                                (machineData.machineState.programCounter + rc).toLong()
                            )
                            machineData.apData.clear()
                            machineData.machineState.programCounter = func.addr1
                        }
                        else -> rc = -2
                    }
                }
            }
            OpCodes.RET_SUB -> {
                rc = 1

                if (machineData.machineState.callStackCounter == 0) rc = -1 else {
                    val value = machineData.apData.getLong(machineData.dSize + machineData.cCallStackBytes - machineData.machineState.callStackCounter * 8)
                    machineData.machineState.callStackCounter--
                    val addr = value.toInt()
                    if (machineData.machineState.jumps.contains(addr))
                        machineData.machineState.programCounter = addr
                    else
                        rc = -2
                }
            }
            OpCodes.IND_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    val addr = machineData.apData.getLong(func.addr1 * 8)

                    if (!validAddr(addr.toInt(), false)) rc = -1 else {
                        machineData.machineState.programCounter += rc
                        machineData.apData.putLong(addr.toInt() * 8, machineData.apData.getLong(func.addr2 * 8))
                        machineData.apData.clear()
                    }
                }
            }
            OpCodes.IDX_DAT -> {
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
                            machineData.machineState.programCounter += rc
                            machineData.apData.putLong(
                                addr.toInt() * 8,
                                machineData.apData.getLong(func.addr1 * 8)
                            )
                            machineData.apData.clear()
                        }
                    }
                }
            }
            OpCodes.MOD_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    val modData1 = machineData.apData.getLong(func.addr1 * 8)
                    val modData2 = machineData.apData.getLong(func.addr2 * 8)

                    if (modData2 == 0L)
                        rc = -2
                    else {
                        machineData.machineState.programCounter += rc
                        machineData.apData.putLong(func.addr1 * 8, modData1 % modData2)
                    }
                }
            }
            OpCodes.SHL_DAT, OpCodes.SHR_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    machineData.machineState.programCounter += rc
                    val value = machineData.apData.getLong(func.addr1 * 8)
                    val shift = machineData.apData.getLong(func.addr2 * 8).coerceIn(0L, 63L).toInt()

                    if (op == OpCodes.SHL_DAT)
                        machineData.apData.putLong(func.addr1 * 8, value shl shift)
                    else
                        machineData.apData.putLong(func.addr1 * 8, value ushr shift)
                }
            }
            OpCodes.JMP_ADR -> {
                rc = getAddr(true)

                if (rc == 0) {
                    rc = 5
                    if (machineData.machineState.jumps.contains(func.addr1))
                        machineData.machineState.programCounter = func.addr1
                    else
                        rc = -2
                }
            }
            OpCodes.BZR_DAT, OpCodes.BNZ_DAT -> {
                rc = addrOff

                if (rc == 0) {
                    rc = 6
                    val value = machineData.apData.getLong(func.addr1 * 8)
                    if (op == OpCodes.BZR_DAT && value == 0L || op == OpCodes.BNZ_DAT && value != 0L) {
                        if (machineData.machineState.jumps.contains(machineData.machineState.programCounter + func.off))
                            machineData.machineState.programCounter += func.off.toInt()
                        else
                            rc = -2
                    } else
                        machineData.machineState.programCounter += rc
                }
            }
            OpCodes.BGT_DAT, OpCodes.BLT_DAT, OpCodes.BGE_DAT, OpCodes.BLE_DAT, OpCodes.BEQ_DAT, OpCodes.BNE_DAT -> {
                rc = addrsOff

                if (rc == 0) {
                    rc = 10
                    val val1 = machineData.apData.getLong(func.addr1 * 8)
                    val val2 = machineData.apData.getLong(func.addr2 * 8)

                    if (op == OpCodes.BGT_DAT && val1 > val2 ||
                        op == OpCodes.BLT_DAT && val1 < val2 ||
                        op == OpCodes.BGE_DAT && val1 >= val2 ||
                        op == OpCodes.BLE_DAT && val1 <= val2 ||
                        op == OpCodes.BEQ_DAT && val1 == val2 ||
                        op == OpCodes.BNE_DAT && val1 != val2
                    ) {
                        if (machineData.machineState.jumps.contains(machineData.machineState.programCounter + func.off))
                            machineData.machineState.programCounter += func.off.toInt()
                        else
                            rc = -2
                    } else
                        machineData.machineState.programCounter += rc
                }
            }
            OpCodes.SLP_DAT -> {
                rc = getAddr(true)

                if (rc == 0) {
                    rc = 5

                    machineData.machineState.programCounter += rc
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
            OpCodes.FIZ_DAT, OpCodes.STZ_DAT -> {
                rc = getAddr(false)

                if (rc == 0) {
                    rc = 5
                    if (machineData.apData.getLong(func.addr1 * 8) == 0L) {
                        if (op == OpCodes.STZ_DAT) {
                            machineData.machineState.programCounter += rc
                            machineData.machineState.stopped = true
                        } else {
                            machineData.machineState.programCounter = machineData.machineState.pcNextStartPoint
                            machineData.machineState.finished = true
                        }
                        machineData.setFreeze(true)
                    } else {
                        rc = 5
                        machineData.machineState.programCounter += rc
                    }
                }
            }
            OpCodes.FIN_IMD, OpCodes.STP_IMD -> {
                rc = 1

                if (op == OpCodes.STP_IMD) {
                    machineData.machineState.programCounter += rc
                    machineData.machineState.stopped = true
                } else {
                    machineData.machineState.programCounter = machineData.machineState.pcNextStartPoint
                    machineData.machineState.finished = true
                }
                machineData.setFreeze(true)
            }
            OpCodes.SLP_IMD -> {
                rc = 1

                machineData.machineState.programCounter += rc
                machineData.machineState.stopped = true
                machineData.setFreeze(true)
            }
            OpCodes.SET_PCS -> {
                rc = 1

                machineData.machineState.programCounter += rc
                machineData.machineState.pcNextStartPoint = machineData.machineState.programCounter
            }
            OpCodes.EXT_FUN -> {
                rc = getFun()

                if (rc == 0) {
                    rc = 3

                    machineData.machineState.programCounter += rc
                    dp.atApiController.func(func.func.toInt(), machineData)
                }
            }
            OpCodes.EXT_FUN_DAT -> {
                rc = funAddr
                if (rc == 0) {
                    rc = 7

                    machineData.machineState.programCounter += rc
                    val value = machineData.apData.getLong(func.addr1 * 8)
                    dp.atApiController.func1(func.func.toInt(), value, machineData)
                }
            }
            OpCodes.EXT_FUN_DAT_2 -> {
                rc = funAddrs

                if (rc == 0) {
                    rc = 11

                    machineData.machineState.programCounter += rc
                    val val1 = machineData.apData.getLong(func.addr3 * 8)
                    val val2 = machineData.apData.getLong(func.addr2 * 8)
                    dp.atApiController.func2(func.func.toInt(), val1, val2, machineData)
                }
            }
            OpCodes.EXT_FUN_RET -> {
                rc = funAddr

                if (rc == 0) {
                    rc = 7

                    machineData.machineState.programCounter += rc
                    machineData.apData.putLong(
                        func.addr1 * 8,
                        dp.atApiController.func(func.func.toInt(), machineData)
                    )
                    machineData.apData.clear()
                }
            }
            OpCodes.EXT_FUN_RET_DAT, OpCodes.EXT_FUN_RET_DAT_2 -> {
                rc = funAddrs
                val size = 10

                if ((rc == 0) && op == OpCodes.EXT_FUN_RET_DAT_2) {
                    machineData.apCode.position(size)
                    rc = getAddr(false)
                    machineData.apCode.position(machineData.apCode.position() - size)
                }

                if (rc == 0) {
                    rc = 1 + size + if (op == OpCodes.EXT_FUN_RET_DAT_2) 4 else 0

                    machineData.machineState.programCounter += rc
                    val value = machineData.apData.getLong(func.addr2 * 8)

                    if (op != OpCodes.EXT_FUN_RET_DAT_2)
                        machineData.apData.putLong(func.addr3 * 8, dp.atApiController.func1(func.func.toInt(), value, machineData))
                    else {
                        val val2 = machineData.apData.getLong(func.addr1 * 8)
                        machineData.apData.putLong(func.addr3 * 8, dp.atApiController.func2(func.func.toInt(), value, val2, machineData))
                    }
                    machineData.apData.clear()
                }
            }
            OpCodes.ERR_ADR -> {
                getAddr(true)

                // don't check rc to allow for unsetting handler with -1
                rc = 5

                if (func.addr1 == -1 || machineData.machineState.jumps.contains(func.addr1)) {
                    machineData.machineState.programCounter += rc
                    machineData.machineState.pcErrorHandlerPoint = func.addr1
                } else
                    rc = -2
            }
            else -> {
                // This is here because otherwise the when statement above becomes a lookup switch (instead of a table switch)
                // which is O(log n) whereas table switch is O(1) so is faster.
                if (op == OpCodes.NOP) {
                    ++rc
                    ++machineData.machineState.programCounter
                }
                rc = -2
            }
        }

        return rc
    }

    fun simulateOp(): Int {
        var rc = 0
        if (machineData.cSize < 1 || machineData.machineState.programCounter >= machineData.cSize) return 0
        machineData.machineState.jumps.add(machineData.machineState.programCounter)
        // This is because of a Kotlin bug (KT-36047) that causes the compiler to not produce an optimized TABLESWITCH instruction when this is inlined.
        @Suppress("MoveVariableDeclarationIntoWhen")
        val op = machineData.apCode.get(machineData.machineState.programCounter)
        when (op) {
            OpCodes.SET_VAL -> {
                addressVal
                rc = 13
            }
            OpCodes.CLR_DAT,
            OpCodes.DEC_DAT,
            OpCodes.FIZ_DAT,
            OpCodes.INC_DAT,
            OpCodes.NOT_DAT,
            OpCodes.POP_DAT,
            OpCodes.PSH_DAT,
            OpCodes.STZ_DAT -> {
                getAddr(false)
                rc = 5
            }
            OpCodes.ADD_DAT,
            OpCodes.AND_DAT,
            OpCodes.BOR_DAT,
            OpCodes.DIV_DAT,
            OpCodes.MOD_DAT,
            OpCodes.MUL_DAT,
            OpCodes.SET_DAT,
            OpCodes.SHL_DAT,
            OpCodes.SHR_DAT,
            OpCodes.SUB_DAT,
            OpCodes.XOR_DAT -> {
                addrs
                rc = 9
            }
            OpCodes.IND_DAT, OpCodes.SET_IND -> {
                rc = addrs
                if (rc == 0) rc = 9
            }
            OpCodes.SET_IDX -> {
                machineData.apCode.position(8)
                rc = getAddr(false)
                machineData.apCode.position(machineData.apCode.position() - 8)
            }
            OpCodes.SLP_DAT, OpCodes.JMP_ADR, OpCodes.ERR_ADR, OpCodes.JMP_SUB -> {
                getAddr(true)
                rc = 5
            }
            OpCodes.RET_SUB -> {
                rc = 1
            }
            OpCodes.IDX_DAT -> {
                addrs
                machineData.apCode.position(8)
                rc = getAddr(false)
                machineData.apCode.position(machineData.apCode.position() - 8)
            }
            OpCodes.BZR_DAT, OpCodes.BNZ_DAT -> {
                addrOff
                rc = 6
            }
            OpCodes.BGT_DAT,
            OpCodes.BLT_DAT,
            OpCodes.BGE_DAT,
            OpCodes.BLE_DAT,
            OpCodes.BEQ_DAT,
            OpCodes.BNE_DAT -> {
                addrsOff
                rc = 10
            }
            OpCodes.FIN_IMD,
            OpCodes.STP_IMD,
            OpCodes.SLP_IMD,
            OpCodes.SET_PCS -> {
                rc = 1
            }
            OpCodes.EXT_FUN -> {
                getFun()
                rc = 3
            }
            OpCodes.EXT_FUN_DAT -> {
                rc = funAddr
                if (rc == 0) rc = 7
            }
            OpCodes.EXT_FUN_DAT_2 -> {
                funAddrs
                rc = 11
            }
            OpCodes.EXT_FUN_RET -> {
                funAddr
                rc = 7
            }
            OpCodes.EXT_FUN_RET_DAT -> {
                rc = funAddrs
                if (rc == 0) rc = 11
            }
            OpCodes.EXT_FUN_RET_DAT_2 -> {
                funAddrs
                machineData.apCode.position(10)
                rc = getAddr(false)
                machineData.apCode.position(machineData.apCode.position() - 10)
                if (rc == 0) rc = 15
            }
            else -> {
                // This is here because otherwise the when statement above becomes a lookup switch (instead of a table switch)
                // which is O(log n) whereas table switch is O(1) so is faster.
                if (op == OpCodes.NOP) {
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
