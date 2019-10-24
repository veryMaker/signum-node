package brs.common

import brs.Constants.ONE_BURST
import brs.crypto.Crypto

object TestConstants {

    const val TEST_ACCOUNT_ID = "BURST-D95D-67CQ-8VDN-5EVAR"

    const val TEST_ACCOUNT_NUMERIC_ID_PARSED = 4297397359864028267L

    const val TEST_SECRET_PHRASE = "ach wie gut dass niemand weiss dass ich Rumpelstilzchen heiss"

    const val TEST_PUBLIC_KEY = "6b223e427b2d44ef8fe2dcb64845d7d9790045167202f1849facef10398bd529"

    val TEST_PUBLIC_KEY_BYTES = Crypto.getPublicKey(TEST_SECRET_PHRASE)

    const val TEST_ACCOUNT_NUMERIC_ID = "4297397359864028267"

    const val DEADLINE = "400"

    const val FEE = ONE_BURST.toString()

    const val TEN_BURST = ONE_BURST * 10
}
