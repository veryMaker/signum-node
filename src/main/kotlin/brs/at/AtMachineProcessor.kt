/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.
*/

package brs.at

import brs.entity.DependencyProvider
import brs.util.logging.safeDebug
import org.slf4j.LoggerFactory
import org.slf4j.helpers.NOPLogger

internal class AtMachineProcessor(
    private val dp: DependencyProvider,
    private val machineData: AtMachineState,
    enableLogger: Boolean
) {
    private val logger =
        if (enableLogger) LoggerFactory.getLogger(AtMachineProcessor::class.java) else NOPLogger.NOP_LOGGER
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

    fun processOp(disassemble: Boolean): Int {
        var rc = 0

        if (machineData.cSize < 1 || machineData.machineState.pc >= machineData.cSize)
            return 0

        if (disassemble) {
            machineData.machineState.jumps.add(machineData.machineState.pc)
        }

        val op = machineData.apCode.get(machineData.machineState.pc)
        if (op > 0 && disassemble && !disassemble) {
            logger.safeDebug { String.format("%8x", machineData.machineState.pc).replace(' ', '0') }
            if (machineData.machineState.pc == machineData.machineState.opc)
                logger.safeDebug { "* " }
            else
                logger.safeDebug { "  " }
        }

        when {
            op == OpCodes.E_OP_CODE_NOP -> if (disassemble) {
                if (!disassemble)
                    logger.safeDebug { "NOP" }
                ++rc
            } else {
                ++rc
                ++machineData.machineState.pc
            }
            op == OpCodes.E_OP_CODE_SET_VAL -> {
                rc = addressVal

                if (rc == 0 || disassemble) {
                    rc = 13
                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "SET @ ${String.format("%8s", func.addr1).replace(' ', '0')} ${String.format("#%16s", java.lang.Long.toHexString(func.value)).replace(' ', '0')}" }
                    } else {
                        machineData.machineState.pc += rc
                        machineData.apData.putLong(func.addr1 * 8, func.value)
                        machineData.apData.clear()
                    }
                }
            }
            op == OpCodes.E_OP_CODE_SET_DAT -> {
                rc = addrs

                if (rc == 0 || disassemble) {
                    rc = 9
                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "SET @ ${String.format("%8s", func.addr1).replace(' ', '0')} \$${String.format("%8s", func.addr2).replace(' ', '0')}" }
                    } else {
                        machineData.machineState.pc += rc
                        machineData.apData.putLong(func.addr1 * 8, machineData.apData.getLong(func.addr2 * 8)) // HERE
                        machineData.apData.clear()
                    }
                }
            }
            op == OpCodes.E_OP_CODE_CLR_DAT -> {
                rc = getAddr(false)

                if (rc == 0 || disassemble) {
                    rc = 5
                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "CLR @ ${String.format("%8s", func.addr1)}" }
                    } else {
                        machineData.machineState.pc += rc
                        machineData.apData.putLong(func.addr1 * 8, 0.toLong())
                        machineData.apData.clear()
                    }
                }
            }
            op == OpCodes.E_OP_CODE_INC_DAT ||
                    op == OpCodes.E_OP_CODE_DEC_DAT ||
                    op == OpCodes.E_OP_CODE_NOT_DAT -> {
                rc = getAddr(false)
                if (rc == 0 || disassemble) {
                    rc = 5
                    if (disassemble) {
                        if (!disassemble) {
                            when (op) {
                                OpCodes.E_OP_CODE_INC_DAT -> logger.safeDebug { "INC @" }
                                OpCodes.E_OP_CODE_DEC_DAT -> logger.safeDebug { "DEC @" }
                                OpCodes.E_OP_CODE_NOT_DAT -> logger.safeDebug { "NOT @" }
                            }
                            logger.safeDebug { String.format("%d", func.addr1).replace(' ', '0') }
                        }
                    } else {
                        machineData.machineState.pc += rc
                        when (op) {
                            OpCodes.E_OP_CODE_INC_DAT -> {
                                val incData = machineData.apData.getLong(func.addr1 * 8) + 1
                                machineData.apData.putLong(func.addr1 * 8, incData)
                                machineData.apData.clear()
                            }
                            OpCodes.E_OP_CODE_DEC_DAT -> {
                                val incData = machineData.apData.getLong(func.addr1 * 8) - 1
                                machineData.apData.putLong(func.addr1 * 8, incData)
                                machineData.apData.clear()
                            }
                            OpCodes.E_OP_CODE_NOT_DAT -> {
                                val incData = machineData.apData.getLong(func.addr1 * 8)
                                machineData.apData.putLong(func.addr1 * 8, incData.inv())
                                machineData.apData.clear()
                            }
                        }
                    }
                }
            }
            op == OpCodes.E_OP_CODE_ADD_DAT ||
                    op == OpCodes.E_OP_CODE_SUB_DAT ||
                    op == OpCodes.E_OP_CODE_MUL_DAT ||
                    op == OpCodes.E_OP_CODE_DIV_DAT -> {
                rc = addrs

                if (rc == 0 || disassemble) {
                    rc = 9
                    if (disassemble) {
                        if (!disassemble) {
                            when (op) {
                                OpCodes.E_OP_CODE_ADD_DAT -> logger.safeDebug { "ADD @" }
                                OpCodes.E_OP_CODE_SUB_DAT -> logger.safeDebug { "SUB @" }
                                OpCodes.E_OP_CODE_MUL_DAT -> logger.safeDebug { "MUL @" }
                                OpCodes.E_OP_CODE_DIV_DAT -> logger.safeDebug { "DIV @" }
                            }
                            logger.safeDebug { "${String.format("%8x", func.addr1).replace(' ', '0')} \$${String.format("%8s", func.addr2).replace(' ', '0')}" }
                        }
                    } else {
                        val value = machineData.apData.getLong(func.addr2 * 8)
                        if (op == OpCodes.E_OP_CODE_DIV_DAT && value == 0L)
                            rc = -2
                        else {
                            machineData.machineState.pc += rc
                            when (op) {
                                OpCodes.E_OP_CODE_ADD_DAT -> {
                                    val addData1 = machineData.apData.getLong(func.addr1 * 8)
                                    val addData2 = machineData.apData.getLong(func.addr2 * 8)
                                    machineData.apData.putLong(func.addr1 * 8, addData1 + addData2)
                                    machineData.apData.clear()
                                }
                                OpCodes.E_OP_CODE_SUB_DAT -> {
                                    val addData1 = machineData.apData.getLong(func.addr1 * 8)
                                    val addData2 = machineData.apData.getLong(func.addr2 * 8)
                                    machineData.apData.putLong(func.addr1 * 8, addData1 - addData2)
                                    machineData.apData.clear()
                                }
                                OpCodes.E_OP_CODE_MUL_DAT -> {
                                    val addData1 = machineData.apData.getLong(func.addr1 * 8)
                                    val addData2 = machineData.apData.getLong(func.addr2 * 8)
                                    machineData.apData.putLong(func.addr1 * 8, addData1 * addData2)
                                    machineData.apData.clear()
                                }
                                OpCodes.E_OP_CODE_DIV_DAT -> {
                                    val addData1 = machineData.apData.getLong(func.addr1 * 8)
                                    val addData2 = machineData.apData.getLong(func.addr2 * 8)
                                    machineData.apData.putLong(func.addr1 * 8, addData1 / addData2)
                                    machineData.apData.clear()
                                }
                            }
                        }
                    }
                }
            }
            op == OpCodes.E_OP_CODE_BOR_DAT ||
                    op == OpCodes.E_OP_CODE_AND_DAT ||
                    op == OpCodes.E_OP_CODE_XOR_DAT -> {
                rc = addrs

                if (rc == 0 || disassemble) {
                    rc = 9
                    if (disassemble) {
                        if (!disassemble) {
                            when (op) {
                                OpCodes.E_OP_CODE_BOR_DAT -> logger.safeDebug { "BOR @" }
                                OpCodes.E_OP_CODE_AND_DAT -> logger.safeDebug { "AND @" }
                                OpCodes.E_OP_CODE_XOR_DAT -> logger.safeDebug { "XOR @" }
                            }
                            logger.safeDebug { String.format("%16s $%16s", func.addr1, func.addr2).replace(' ', '0') }
                        }
                    } else {
                        machineData.machineState.pc += rc
                        val value = machineData.apData.getLong(func.addr2 * 8)

                        when (op) {
                            OpCodes.E_OP_CODE_BOR_DAT -> {
                                val incData = machineData.apData.getLong(func.addr1 * 8)
                                machineData.apData.putLong(func.addr1 * 8, incData or value)
                                machineData.apData.clear()
                            }
                            OpCodes.E_OP_CODE_AND_DAT -> {
                                val incData = machineData.apData.getLong(func.addr1 * 8)
                                machineData.apData.putLong(func.addr1 * 8, incData and value)
                                machineData.apData.clear()
                            }
                            OpCodes.E_OP_CODE_XOR_DAT -> {
                                val incData = machineData.apData.getLong(func.addr1 * 8)
                                machineData.apData.putLong(func.addr1 * 8, incData xor value)
                                machineData.apData.clear()
                            }
                        }
                    }
                }
            }
            op == OpCodes.E_OP_CODE_SET_IND -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "SET @ ${String.format("%8s", func.addr1).replace(' ', '0')} ${String.format("$($%8s", func.addr2).replace(' ', '0')}" }
                    } else {
                        val addr = machineData.apData.getLong(func.addr2 * 8)

                        if (!validAddr(addr.toInt(), false))
                            rc = -1
                        else {
                            machineData.machineState.pc += rc
                            val value = machineData.apData.getLong(addr.toInt() * 8)
                            machineData.apData.putLong(func.addr1 * 8, value)
                            machineData.apData.clear()
                        }
                    }
                }
            }
            op == OpCodes.E_OP_CODE_SET_IDX -> {
                val addr1 = func.addr1
                val addr2 = func.addr2
                val size = 8

                rc = addrs

                if (rc == 0 || disassemble) {
                    machineData.apCode.position(size)
                    rc = getAddr(false)
                    machineData.apCode.position(machineData.apCode.position() - size)

                    if (rc == 0 || disassemble) {
                        rc = 13
                        val base = machineData.apData.getLong(addr2 * 8)
                        val offs = machineData.apData.getLong(func.addr1 * 8)

                        val addr = base + offs

                        logger.safeDebug { "addr1: ${func.addr1}" }
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
            op == OpCodes.E_OP_CODE_PSH_DAT || op == OpCodes.E_OP_CODE_POP_DAT -> {
                rc = getAddr(false)
                if (rc == 0 || disassemble) {
                    rc = 5
                    if (disassemble) {
                        if (!disassemble) {
                            if (op == OpCodes.E_OP_CODE_PSH_DAT)
                                logger.safeDebug { "PSH $" }
                            else
                                logger.safeDebug { "POP @" }
                            logger.safeDebug { String.format("%8s", func.addr1).replace(' ', '0') }
                        }
                    } else if (op == OpCodes.E_OP_CODE_PSH_DAT && machineData.machineState.us == machineData.cUserStackBytes / 8 || op == OpCodes.E_OP_CODE_POP_DAT && machineData.machineState.us == 0) {
                        rc = -1
                    } else {
                        machineData.machineState.pc += rc
                        if (op == OpCodes.E_OP_CODE_PSH_DAT) {
                            val value = machineData.apData.getLong(func.addr1 * 8)
                            machineData.machineState.us++
                            machineData.apData.putLong(machineData.dSize +
                                    machineData.cCallStackBytes +
                                    machineData.cUserStackBytes - machineData.machineState.us * 8, value)
                            machineData.apData.clear()
                        } else {
                            val value = machineData.apData.getLong(machineData.dSize +
                                    machineData.cCallStackBytes +
                                    machineData.cUserStackBytes - machineData.machineState.us * 8)
                            machineData.machineState.us--
                            machineData.apData.putLong(func.addr1 * 8, value)
                            machineData.apData.clear()
                        }
                    }
                }
            }
            op == OpCodes.E_OP_CODE_JMP_SUB -> {
                rc = getAddr(true)

                if (rc == 0 || disassemble) {
                    rc = 5
                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "JSR : ${String.format("%8s", func.addr1).replace(' ', '0')}" }
                    } else {
                        when {
                            machineData.machineState.cs == machineData.cCallStackBytes / 8 -> rc = -1
                            machineData.machineState.jumps.contains(func.addr1) -> {
                                machineData.machineState.cs++
                                machineData.apData.putLong(machineData.dSize + machineData.cCallStackBytes - machineData.machineState.cs * 8,
                                    (machineData.machineState.pc + rc).toLong())
                                machineData.apData.clear()
                                machineData.machineState.pc = func.addr1
                            }
                            else -> rc = -2
                        }
                    }
                }
            }
            op == OpCodes.E_OP_CODE_RET_SUB -> {
                rc = 1

                if (disassemble) {
                    if (!disassemble)
                        logger.safeDebug { "RET\n" }
                } else {
                    if (machineData.machineState.cs == 0)
                        rc = -1
                    else {
                        val value = machineData.apData.getLong(machineData.dSize + machineData.cCallStackBytes - machineData.machineState.cs * 8)
                        machineData.machineState.cs--
                        val addr = value.toInt()
                        if (machineData.machineState.jumps.contains(addr))
                            machineData.machineState.pc = addr
                        else
                            rc = -2
                    }
                }
            }
            op == OpCodes.E_OP_CODE_IND_DAT -> {
                rc = addrs

                if (rc == 0) {
                    rc = 9
                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "SET @${String.format("($%8s)", func.addr1).replace(' ', '0')} ${String.format("$%8s", func.addr2).replace(' ', '0')}" }
                    } else {
                        val addr = machineData.apData.getLong(func.addr1 * 8)

                        if (!validAddr(addr.toInt(), false))
                            rc = -1
                        else {
                            machineData.machineState.pc += rc
                            machineData.apData.putLong(addr.toInt() * 8, machineData.apData.getLong(func.addr2 * 8))
                            machineData.apData.clear()
                        }
                    }
                }
            }
            op == OpCodes.E_OP_CODE_IDX_DAT -> {
                val addr1 = func.addr1
                val addr2 = func.addr2
                val size = 8

                rc = addrs

                if (rc == 0 || disassemble) {
                    machineData.apCode.position(size)
                    rc = getAddr(false)
                    machineData.apCode.position(machineData.apCode.position() - size)

                    if (rc == 0 || disassemble) {
                        rc = 13
                        if (disassemble) {
                            if (!disassemble)
                                logger.safeDebug { "SET @${String.format("($%8s+$%8s)", addr1, addr2).replace(' ', '0')} ${String.format("$%8s", func.addr1).replace(' ', '0')}" }
                        } else {
                            val addr = machineData.apData.getLong(addr1 * 8) + machineData.apData.getLong(addr2 * 8)

                            if (!validAddr(addr.toInt(), false))
                                rc = -1
                            else {
                                machineData.machineState.pc += rc
                                machineData.apData.putLong(addr.toInt() * 8, machineData.apData.getLong(func.addr1 * 8))
                                machineData.apData.clear()
                            }
                        }
                    }
                }
            }
            op == OpCodes.E_OP_CODE_MOD_DAT -> {
                rc = addrs

                if (rc == 0 || disassemble) {
                    rc = 9
                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "MOD @${String.format("%8x", func.addr1).replace(' ', '0')} \$${String.format("%8s", func.addr2).replace(' ', '0')}" }
                    } else {
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
            }
            op == OpCodes.E_OP_CODE_SHL_DAT || op == OpCodes.E_OP_CODE_SHR_DAT -> {
                rc = addrs

                if (rc == 0 || disassemble) {
                    rc = 9
                    if (disassemble) {
                        if (!disassemble) {
                            if (op == OpCodes.E_OP_CODE_SHL_DAT)
                                logger.safeDebug { "SHL @${String.format("%8x", func.addr1).replace(' ', '0')} \$${String.format("%8x", func.addr2).replace(' ', '0')}" }
                            else
                                logger.safeDebug { "SHR @${String.format("%8x", func.addr1).replace(' ', '0')} \$${String.format("%8x", func.addr2).replace(' ', '0')}" }
                        }
                    } else {
                        machineData.machineState.pc += rc
                        val value = machineData.apData.getLong(func.addr1 * 8)
                        val shift = machineData.apData.getLong(func.addr2 * 8).coerceAtLeast(0).coerceAtMost(63).toInt()

                        if (op == OpCodes.E_OP_CODE_SHL_DAT)
                            machineData.apData.putLong(func.addr1 * 8, value shl shift)
                        else
                            machineData.apData.putLong(func.addr1 * 8, value ushr shift)
                    }
                }
            }
            op == OpCodes.E_OP_CODE_JMP_ADR -> {
                rc = getAddr(true)

                if (rc == 0 || disassemble) {
                    rc = 5
                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "JMP : ${String.format("%8x", func.addr1)}" }
                    } else if (machineData.machineState.jumps.contains(func.addr1))
                        machineData.machineState.pc = func.addr1
                    else
                        rc = -2
                }
            }
            op == OpCodes.E_OP_CODE_BZR_DAT || op == OpCodes.E_OP_CODE_BNZ_DAT -> {
                rc = addrOff

                if (rc == 0 || disassemble) {
                    rc = 6
                    if (disassemble) {
                        if (!disassemble) {
                            if (op == OpCodes.E_OP_CODE_BZR_DAT)
                                logger.safeDebug { "BZR $" }
                            else
                                logger.safeDebug { "BNZ $" }

                            logger.safeDebug { "${String.format("%8x", func.addr1).replace(' ', '0')}, :${String.format("%8x", machineData.machineState.pc + func.off).replace(' ', '0')}" }
                        }
                    } else {
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
            }
            op == OpCodes.E_OP_CODE_BGT_DAT || op == OpCodes.E_OP_CODE_BLT_DAT ||
                    op == OpCodes.E_OP_CODE_BGE_DAT || op == OpCodes.E_OP_CODE_BLE_DAT ||
                    op == OpCodes.E_OP_CODE_BEQ_DAT || op == OpCodes.E_OP_CODE_BNE_DAT -> {
                rc = addrsOff

                if (rc == 0 || disassemble) {
                    rc = 10
                    if (disassemble) {
                        if (!disassemble) {
                            when (op) {
                                OpCodes.E_OP_CODE_BGT_DAT -> logger.safeDebug { "BGT $" }
                                OpCodes.E_OP_CODE_BLT_DAT -> logger.safeDebug { "BLT $" }
                                OpCodes.E_OP_CODE_BGE_DAT -> logger.safeDebug { "BGE $" }
                                OpCodes.E_OP_CODE_BLE_DAT -> logger.safeDebug { "BLE $" }
                                OpCodes.E_OP_CODE_BEQ_DAT -> logger.safeDebug { "BEQ $" }
                                else -> logger.safeDebug { "BNE $" }
                            }

                            logger.safeDebug { "${String.format("%8x", func.addr1).replace(' ', '0')} \$${String.format("%8x", func.addr2).replace(' ', '0')} :${String.format("%8x", machineData.machineState.pc + func.off).replace(' ', '0')}" }
                        }
                    } else {
                        val val1 = machineData.apData.getLong(func.addr1 * 8)
                        val val2 = machineData.apData.getLong(func.addr2 * 8)

                        if (op == OpCodes.E_OP_CODE_BGT_DAT && val1 > val2 ||
                            op == OpCodes.E_OP_CODE_BLT_DAT && val1 < val2 ||
                            op == OpCodes.E_OP_CODE_BGE_DAT && val1 >= val2 ||
                            op == OpCodes.E_OP_CODE_BLE_DAT && val1 <= val2 ||
                            op == OpCodes.E_OP_CODE_BEQ_DAT && val1 == val2 ||
                            op == OpCodes.E_OP_CODE_BNE_DAT && val1 != val2) {
                            if (machineData.machineState.jumps.contains(machineData.machineState.pc + func.off))
                                machineData.machineState.pc += func.off.toInt()
                            else
                                rc = -2
                        } else
                            machineData.machineState.pc += rc
                    }
                }
            }
            op == OpCodes.E_OP_CODE_SLP_DAT -> {
                rc = getAddr(true)

                if (rc == 0 || disassemble) {
                    rc = 1 + 4

                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "SLP @ ${String.format("%8x", func.addr1)}" }
                    } else {
                        machineData.machineState.pc += rc
                        var numBlocks = machineData.apData.getLong(func.addr1 * 8).toInt()
                        if (numBlocks < 0)
                            numBlocks = 0
                        val maxNumBlocks = dp.atConstants[machineData.creationBlockHeight].maxWaitForNumOfBlocks.toInt()
                        if (numBlocks > maxNumBlocks)
                            numBlocks = maxNumBlocks
                        machineData.waitForNumberOfBlocks = numBlocks
                        machineData.machineState.stopped = true
                    }
                }
            }
            op == OpCodes.E_OP_CODE_FIZ_DAT || op == OpCodes.E_OP_CODE_STZ_DAT -> {
                rc = getAddr(false)

                if (rc == 0 || disassemble) {
                    rc = 5
                    if (disassemble) {
                        if (!disassemble) {
                            if (op == OpCodes.E_OP_CODE_FIZ_DAT)
                                logger.safeDebug { "FIZ @" }
                            else
                                logger.safeDebug { "STZ @" }

                            logger.safeDebug { String.format("%8x", func.addr1).replace(' ', '0') }
                        }
                    } else {
                        if (machineData.apData.getLong(func.addr1 * 8) == 0L) {
                            if (op == OpCodes.E_OP_CODE_STZ_DAT) {
                                machineData.machineState.pc += rc
                                machineData.machineState.stopped = true
                                machineData.setFreeze(true)
                            } else {
                                machineData.machineState.pc = machineData.machineState.pcs
                                machineData.machineState.finished = true
                                machineData.setFreeze(true)
                            }
                        } else {
                            rc = 5
                            machineData.machineState.pc += rc
                        }
                    }
                }
            }
            op == OpCodes.E_OP_CODE_FIN_IMD || op == OpCodes.E_OP_CODE_STP_IMD -> {
                rc = 1

                if (disassemble) {
                    if (!disassemble) {
                        if (op == OpCodes.E_OP_CODE_FIN_IMD)
                            logger.safeDebug { "FIN\n" }
                        else
                            logger.safeDebug { "STP" }
                    }
                } else if (op == OpCodes.E_OP_CODE_STP_IMD) {
                    machineData.machineState.pc += rc
                    machineData.machineState.stopped = true
                    machineData.setFreeze(true)
                } else {
                    machineData.machineState.pc = machineData.machineState.pcs
                    machineData.machineState.finished = true
                    machineData.setFreeze(true)
                }
            }
            op == OpCodes.E_OP_CODE_SLP_IMD -> {
                rc = 1

                if (disassemble) {
                    if (!disassemble) {
                        logger.safeDebug { "SLP\n" }
                    }
                } else {
                    machineData.machineState.pc += rc
                    machineData.machineState.stopped = true
                    machineData.setFreeze(true)
                }
            }
            op == OpCodes.E_OP_CODE_SET_PCS -> {
                rc = 1

                if (disassemble) {
                    if (!disassemble)
                        logger.safeDebug { "PCS" }
                } else {
                    machineData.machineState.pc += rc
                    machineData.machineState.pcs = machineData.machineState.pc
                }
            }
            op == OpCodes.E_OP_CODE_EXT_FUN -> {
                rc = getFun()

                if (rc == 0 || disassemble) {
                    rc = 1 + 2

                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "FUN ${func.func}" }
                    } else {
                        machineData.machineState.pc += rc
                        dp.atApiController.func(func.func.toInt(), machineData)
                    }
                }
            }
            op == OpCodes.E_OP_CODE_EXT_FUN_DAT -> {
                rc = funAddr
                if (rc == 0) {
                    rc = 7

                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "FUN ${func.func} \$${String.format("%8x", func.addr1).replace(' ', '0')}" }
                    } else {
                        machineData.machineState.pc += rc
                        val value = machineData.apData.getLong(func.addr1 * 8)
                        dp.atApiController.func1(func.func.toInt(), value, machineData)
                    }
                }
            }
            op == OpCodes.E_OP_CODE_EXT_FUN_DAT_2 -> {
                rc = funAddrs

                if (rc == 0 || disassemble) {
                    rc = 11

                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "FUN ${func.func} \$${String.format("%8x", func.addr3).replace(' ', '0')} \$${String.format("%8x", func.addr2).replace(' ', '0')}" }
                    } else {
                        machineData.machineState.pc += rc
                        val val1 = machineData.apData.getLong(func.addr3 * 8)
                        val val2 = machineData.apData.getLong(func.addr2 * 8)

                        dp.atApiController.func2(func.func.toInt(), val1, val2, machineData)
                    }
                }
            }
            op == OpCodes.E_OP_CODE_EXT_FUN_RET -> {
                rc = funAddr

                if (rc == 0 || disassemble) {
                    rc = 7

                    if (disassemble) {
                        if (!disassemble)
                            logger.safeDebug { "FUN @${String.format("%8x", func.addr1).replace(' ', '0')} ${func.func}" }
                    } else {
                        machineData.machineState.pc += rc

                        machineData.apData.putLong(func.addr1 * 8, dp.atApiController.func(func.func.toInt(), machineData))
                        machineData.apData.clear()
                    }
                }
            }
            op == OpCodes.E_OP_CODE_EXT_FUN_RET_DAT || op == OpCodes.E_OP_CODE_EXT_FUN_RET_DAT_2 -> {
                rc = funAddrs
                val size = 10

                if ((rc == 0 || disassemble) && op == OpCodes.E_OP_CODE_EXT_FUN_RET_DAT_2) {
                    machineData.apCode.position(size)
                    rc = getAddr(false)
                    machineData.apCode.position(machineData.apCode.position() - size)
                }

                if (rc == 0) {
                    rc = 1 + size + if (op == OpCodes.E_OP_CODE_EXT_FUN_RET_DAT_2) 4 else 0

                    if (disassemble) {
                        if (!disassemble) {
                            logger.safeDebug { "FUN @${String.format("%8x", func.addr3).replace(' ', '0')} ${func.func} \$${String.format("%8x", func.addr2).replace(' ', '0')}" }
                            if (op == OpCodes.E_OP_CODE_EXT_FUN_RET_DAT_2)
                                logger.safeDebug { "\$${String.format("%8x", func.addr1).replace(' ', '0')}"  }
                        }
                    } else {
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
            }
            op == OpCodes.E_OP_CODE_ERR_ADR -> {
                getAddr(true) // rico666: Why getAddr if rc is set hard anyway ?? // TODO check if this updates the buffer or can be removed

                // don't check rc to allow for unsetting handler with -1
                rc = 5

                if (disassemble) {
                    if (!disassemble)
                        logger.safeDebug { "ERR :${String.format("%8x", func.addr1)}" }
                } else {
                    if (func.addr1 == -1 || machineData.machineState.jumps.contains(func.addr1)) {
                        machineData.machineState.pc += rc
                        machineData.machineState.err = func.addr1
                    } else
                        rc = -2
                }
            }
            !disassemble -> rc = -2
        }

        if (rc == -1 && disassemble && !disassemble)
            logger.safeDebug { "\n(overflow)" }

        if (rc == -2 && disassemble && !disassemble)
            logger.safeDebug { "\n(invalid op)" }

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
