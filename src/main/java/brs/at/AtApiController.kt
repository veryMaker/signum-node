package brs.at

internal object AtApiController {
    private val atApi = AtApiImpl()

    fun func(funcNum: Int, state: AtMachineState): Long {
        when (funcNum) {
            256 -> return atApi.getA1(state)
            257 -> return atApi.getA2(state)
            258 -> return atApi.getA3(state)
            259 -> return atApi.getA4(state)
            260 -> return atApi.getB1(state)
            261 -> return atApi.getB2(state)
            262 -> return atApi.getB3(state)
            263 -> return atApi.getB4(state)
            288 -> atApi.clearA(state)
            289 -> atApi.clearB(state)
            290 -> {
                atApi.clearA(state)
                atApi.clearB(state)
            }
            291 -> atApi.copyAFromB(state)
            292 -> atApi.copyBFromA(state)
            293 -> return atApi.checkAIsZero(state)
            294 -> return atApi.checkBIsZero(state)
            295 -> return atApi.checkAEqualsB(state)
            296 -> atApi.swapAAndB(state)
            297 -> atApi.orAWithB(state)
            298 -> atApi.orBWithA(state)
            299 -> atApi.andAWithB(state)
            300 -> atApi.andBWithA(state)
            301 -> atApi.xorAWithB(state)
            302 -> atApi.xorBWithA(state)
            320 -> atApi.addAToB(state)
            321 -> atApi.addBToA(state)
            322 -> atApi.subAFromB(state)
            323 -> atApi.subBFromA(state)
            324 -> atApi.mulAByB(state)
            325 -> atApi.mulBByA(state)
            326 -> atApi.divAByB(state)
            327 -> atApi.divBByA(state)

            512 -> atApi.md5Atob(state)
            513 -> return atApi.checkMd5AWithB(state)
            514 -> atApi.hash160AToB(state)
            515 -> return atApi.checkHash160AWithB(state)
            516 -> atApi.sha256AToB(state)
            517 -> return atApi.checkSha256AWithB(state)

            768 -> return atApi.getBlockTimestamp(state)    // 0x0300
            769 -> return atApi.getCreationTimestamp(state) // 0x0301
            770 -> return atApi.getLastBlockTimestamp(state)
            771 -> atApi.putLastBlockHashInA(state)
            773 -> return atApi.getTypeForTxInA(state)
            774 -> return atApi.getAmountForTxInA(state)
            775 -> return atApi.getTimestampForTxInA(state)
            776 -> return atApi.getRandomIdForTxInA(state)
            777 -> atApi.messageFromTxInAToB(state)
            778 -> atApi.bToAddressOfTxInA(state)
            779 -> atApi.bToAddressOfCreator(state)

            1024 -> return atApi.getCurrentBalance(state)
            1025 -> return atApi.getPreviousBalance(state)
            1027 -> atApi.sendAllToAddressInB(state)
            1028 -> atApi.sendOldToAddressInB(state)
            1029 -> atApi.sendAToAddressInB(state)
            else -> return 0
        }
        return 0
    }

    fun func1(funcNum: Int, `val`: Long, state: AtMachineState): Long {
        when (funcNum) {
            272 -> atApi.setA1(`val`, state)
            273 -> atApi.setA2(`val`, state)
            274 -> atApi.setA3(`val`, state)
            275 -> atApi.setA4(`val`, state)
            278 -> atApi.setB1(`val`, state)
            279 -> atApi.setB2(`val`, state)
            280 -> atApi.setB3(`val`, state)
            281 -> atApi.setB4(`val`, state)
            772 -> atApi.aToTxAfterTimestamp(`val`, state)
            1026 -> atApi.sendToAddressInB(`val`, state)
            else -> return 0
        }
        return 0
    }

    fun func2(funcNum: Int, val1: Long, val2: Long, state: AtMachineState): Long {
        when (funcNum) {
            276 -> atApi.setA1A2(val1, val2, state)
            277 -> atApi.setA3A4(val1, val2, state)
            282 -> atApi.setB1B2(val1, val2, state)
            283 -> atApi.setB3B4(val1, val2, state)
            1030 -> return atApi.addMinutesToTimestamp(val1, val2, state)
            else -> return 0
        }
        return 0
    }
}
