package brs.at

import brs.DependencyProvider

class AtApiController(private val dp: DependencyProvider) {
    fun func(funcNum: Int, state: AtMachineState): Long {
        when (funcNum) {
            256 -> return dp.atApi.getA1(state)
            257 -> return dp.atApi.getA2(state)
            258 -> return dp.atApi.getA3(state)
            259 -> return dp.atApi.getA4(state)
            260 -> return dp.atApi.getB1(state)
            261 -> return dp.atApi.getB2(state)
            262 -> return dp.atApi.getB3(state)
            263 -> return dp.atApi.getB4(state)
            288 -> dp.atApi.clearA(state)
            289 -> dp.atApi.clearB(state)
            290 -> {
                dp.atApi.clearA(state)
                dp.atApi.clearB(state)
            }
            291 -> dp.atApi.copyAFromB(state)
            292 -> dp.atApi.copyBFromA(state)
            293 -> return dp.atApi.checkAIsZero(state)
            294 -> return dp.atApi.checkBIsZero(state)
            295 -> return dp.atApi.checkAEqualsB(state)
            296 -> dp.atApi.swapAAndB(state)
            297 -> dp.atApi.orAWithB(state)
            298 -> dp.atApi.orBWithA(state)
            299 -> dp.atApi.andAWithB(state)
            300 -> dp.atApi.andBWithA(state)
            301 -> dp.atApi.xorAWithB(state)
            302 -> dp.atApi.xorBWithA(state)
            320 -> dp.atApi.addAToB(state)
            321 -> dp.atApi.addBToA(state)
            322 -> dp.atApi.subAFromB(state)
            323 -> dp.atApi.subBFromA(state)
            324 -> dp.atApi.mulAByB(state)
            325 -> dp.atApi.mulBByA(state)
            326 -> dp.atApi.divAByB(state)
            327 -> dp.atApi.divBByA(state)

            512 -> dp.atApi.md5Atob(state)
            513 -> return dp.atApi.checkMd5AWithB(state)
            514 -> dp.atApi.hash160AToB(state)
            515 -> return dp.atApi.checkHash160AWithB(state)
            516 -> dp.atApi.sha256AToB(state)
            517 -> return dp.atApi.checkSha256AWithB(state)

            768 -> return dp.atApi.getBlockTimestamp(state)    // 0x0300
            769 -> return dp.atApi.getCreationTimestamp(state) // 0x0301
            770 -> return dp.atApi.getLastBlockTimestamp(state)
            771 -> dp.atApi.putLastBlockHashInA(state)
            773 -> return dp.atApi.getTypeForTxInA(state)
            774 -> return dp.atApi.getAmountForTxInA(state)
            775 -> return dp.atApi.getTimestampForTxInA(state)
            776 -> return dp.atApi.getRandomIdForTxInA(state)
            777 -> dp.atApi.messageFromTxInAToB(state)
            778 -> dp.atApi.bToAddressOfTxInA(state)
            779 -> dp.atApi.bToAddressOfCreator(state)

            1024 -> return dp.atApi.getCurrentBalance(state)
            1025 -> return dp.atApi.getPreviousBalance(state)
            1027 -> dp.atApi.sendAllToAddressInB(state)
            1028 -> dp.atApi.sendOldToAddressInB(state)
            1029 -> dp.atApi.sendAToAddressInB(state)
            else -> return 0
        }
        return 0
    }

    fun func1(funcNum: Int, value: Long, state: AtMachineState): Long {
        when (funcNum) {
            272 -> dp.atApi.setA1(value, state)
            273 -> dp.atApi.setA2(value, state)
            274 -> dp.atApi.setA3(value, state)
            275 -> dp.atApi.setA4(value, state)
            278 -> dp.atApi.setB1(value, state)
            279 -> dp.atApi.setB2(value, state)
            280 -> dp.atApi.setB3(value, state)
            281 -> dp.atApi.setB4(value, state)
            772 -> dp.atApi.aToTxAfterTimestamp(value, state)
            1026 -> dp.atApi.sendToAddressInB(value, state)
            else -> return 0
        }
        return 0
    }

    fun func2(funcNum: Int, val1: Long, val2: Long, state: AtMachineState): Long {
        when (funcNum) {
            276 -> dp.atApi.setA1A2(val1, val2, state)
            277 -> dp.atApi.setA3A4(val1, val2, state)
            282 -> dp.atApi.setB1B2(val1, val2, state)
            283 -> dp.atApi.setB3B4(val1, val2, state)
            1030 -> return dp.atApi.addMinutesToTimestamp(val1, val2, state)
            else -> return 0
        }
        return 0
    }
}
