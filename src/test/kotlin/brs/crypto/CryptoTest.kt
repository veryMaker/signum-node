package brs.crypto

import brs.common.TestConstants
import brs.util.convert.parseHexString
import brs.util.convert.toHexString
import brs.util.crypto.*
import burst.kit.crypto.BurstCrypto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.nio.charset.StandardCharsets

@RunWith(JUnit4::class)
class CryptoTest {

    private fun stringToBytes(string: String): ByteArray {
        return string.toByteArray(StandardCharsets.UTF_8)
    }

    @Test
    fun testCryptoSha256() {
        val sha256 = Crypto.sha256()
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", sha256.digest(stringToBytes("")).toHexString())
        assertEquals("e806a291cfc3e61f83b98d344ee57e3e8933cccece4fb45e1481f1f560e70eb1", sha256.digest(stringToBytes("Testing")).toHexString())
        assertEquals("6de732f18e99e18ac25c609d6942f06f6ed7ab3f261ca46668d3a0e19fbc9e80", sha256.digest(stringToBytes("Burstcoin!")).toHexString())
        assertEquals("d059c5e6b6715f1e1dd83295e804d4f5fbc560cd10befde400434d19afdf4cfe", sha256.digest(stringToBytes("Burst Apps Team")).toHexString())
    }

    @Test
    fun testCryptoShabal256() {
        val shabal256 = Crypto.shabal256()
        assertEquals("aec750d11feee9f16271922fbaf5a9be142f62019ef8d720f858940070889014", shabal256.digest(stringToBytes("")).toHexString())
        assertEquals("10e237979a7233aa6a9377ff6a4b2541f890f67107fe0c89008fdd2c48e4cfe5", shabal256.digest(stringToBytes("Testing")).toHexString())
        assertEquals("9beec9e237da7542a045b89c709b5d423b22faa99d5f01abab67261e1a9de6b8", shabal256.digest(stringToBytes("Burstcoin!")).toHexString())
        assertEquals("4d92fb90793baaefabf4691cdcf4f1332ccd51c4a74f509a4b9a338eddb39e09", shabal256.digest(stringToBytes("Burst Apps Team")).toHexString())
    }

    @Test
    fun testCryptoRipemd160() {
        val ripemd160 = Crypto.ripeMD160()
        assertEquals("9c1185a5c5e9fc54612808977ee8f548b2258d31", ripemd160.digest(stringToBytes("")).toHexString())
        assertEquals("01743c6e71742ed72d6c51537f1790a462b82c82", ripemd160.digest(stringToBytes("Testing")).toHexString())
        assertEquals("9b7e20c53c6e77ed8d9768d8a5a813d02c0a0d6a", ripemd160.digest(stringToBytes("Burstcoin!")).toHexString())
        assertEquals("b089c88c2f81e87326c22b2df66dca6857f690a0", ripemd160.digest(stringToBytes("Burst Apps Team")).toHexString())
    }

    @Test
    fun testCryptoGetPublicKey() {
        assertEquals("18656ba6d7862cb11d995afe20bf8761edee05ec28aa29bfbcf31ebd19dede71", Crypto.getPublicKey("").toHexString())
        assertEquals("d28b42565bf16008158d2750b686722343a9a4d58b22c45deae3bb89135c3d66", Crypto.getPublicKey("Testing").toHexString())
        assertEquals("cea5e981451125a7b7675af9154ce56a973d62f817bd25fa250820b32349c32e", Crypto.getPublicKey("Burstcoin!").toHexString())
        assertEquals("25fa4a7c542f042ad86102addfd45272bbec1b350e94a483c4dc93f9d123f408", Crypto.getPublicKey("Burst Apps Team").toHexString())
    }

    @Test
    fun testCryptoGetPrivateKey() {
        assertEquals("e0b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", Crypto.getPrivateKey("").toHexString())
        assertEquals("e806a291cfc3e61f83b98d344ee57e3e8933cccece4fb45e1481f1f560e70e71", Crypto.getPrivateKey("Testing").toHexString())
        assertEquals("68e732f18e99e18ac25c609d6942f06f6ed7ab3f261ca46668d3a0e19fbc9e40", Crypto.getPrivateKey("Burstcoin!").toHexString())
        assertEquals("d059c5e6b6715f1e1dd83295e804d4f5fbc560cd10befde400434d19afdf4c7e", Crypto.getPrivateKey("Burst Apps Team").toHexString())
    }

    @Test
    fun testCryptoSign() {
        assertEquals("f6c3cace87e022565c1d547c4a13d216d765cb4aec098ed36ef758a33b11b3008573b8d81155939dc5677f4b222a3d3943c6e2f139cba3f82f5137d61b9a79fd", stringToBytes("").signUsing(TestConstants.TEST_SECRET_PHRASE).toHexString())
        assertEquals("aa140db8d96a058b6d9488aa4d3771b0da3ad8d2bcfba64d8b3b117c1a73910efec6917aa986027784c46acfe645b400edfd04a77ad50af77e10d169b470d64b", stringToBytes("Testing").signUsing(TestConstants.TEST_SECRET_PHRASE).toHexString())
        assertEquals("e4b7bcd76ebccbf6bf3d4ad1c72c6e0b3e902f041a38e37f8aac09ea5a43310d78a90061eb9c12f6d32174ce19bc4105223df38035f2cfc0ebb069591e1e1a01", stringToBytes("Burstcoin!").signUsing(TestConstants.TEST_SECRET_PHRASE).toHexString())
        assertEquals("46ab525630fd4f2266d78309a04153dd7d69c8d3c77765956eff1b86cc4e5a0d8d0d8df1cfe8300617551361b99f54b5db7afbd8ffa0a21ddcdac2cfdec57b71", stringToBytes("Burst Apps Team").signUsing(TestConstants.TEST_SECRET_PHRASE).toHexString())
    }

    @Test
    fun testCryptoVerify() {
        val publicKey = Crypto.getPublicKey(TestConstants.TEST_SECRET_PHRASE)
        assertTrue(stringToBytes("").verifySignature("f6c3cace87e022565c1d547c4a13d216d765cb4aec098ed36ef758a33b11b3008573b8d81155939dc5677f4b222a3d3943c6e2f139cba3f82f5137d61b9a79fd".parseHexString(), publicKey, true))
        assertTrue(stringToBytes("Testing").verifySignature("aa140db8d96a058b6d9488aa4d3771b0da3ad8d2bcfba64d8b3b117c1a73910efec6917aa986027784c46acfe645b400edfd04a77ad50af77e10d169b470d64b".parseHexString(), publicKey, true))
        assertTrue(stringToBytes("Burstcoin!").verifySignature("e4b7bcd76ebccbf6bf3d4ad1c72c6e0b3e902f041a38e37f8aac09ea5a43310d78a90061eb9c12f6d32174ce19bc4105223df38035f2cfc0ebb069591e1e1a01".parseHexString(), publicKey, true))
        assertTrue(stringToBytes("Burst Apps Team").verifySignature("46ab525630fd4f2266d78309a04153dd7d69c8d3c77765956eff1b86cc4e5a0d8d0d8df1cfe8300617551361b99f54b5db7afbd8ffa0a21ddcdac2cfdec57b71".parseHexString(), publicKey, true))
    }

    @Test
    fun testCryptoRsEncode() {
        val burstCrypto = BurstCrypto.getInstance()
        assertEquals("23YP-M8H9-FA5W-5CX9B", burstCrypto.getBurstAddressFromPassphrase("").burstID.signedLongId.rsEncode())
        assertEquals("BTKQ-5ST6-6HAL-HKVYW", burstCrypto.getBurstAddressFromPassphrase("Testing").burstID.signedLongId.rsEncode())
        assertEquals("4KFW-N4LS-7UVW-8AUZJ", burstCrypto.getBurstAddressFromPassphrase("Burstcoin!").burstID.signedLongId.rsEncode())
        assertEquals("T7XD-7M3X-MB9F-38DU8", burstCrypto.getBurstAddressFromPassphrase("Burst Apps Team").burstID.signedLongId.rsEncode())
    }

    @Test
    fun testCryptoRsDecode() {
        val burstCrypto = BurstCrypto.getInstance()
        assertEquals(burstCrypto.getBurstAddressFromPassphrase("").burstID.signedLongId, "23YP-M8H9-FA5W-5CX9B".rsDecode())
        assertEquals(burstCrypto.getBurstAddressFromPassphrase("Testing").burstID.signedLongId, "BTKQ-5ST6-6HAL-HKVYW".rsDecode())
        assertEquals(burstCrypto.getBurstAddressFromPassphrase("Burstcoin!").burstID.signedLongId, "4KFW-N4LS-7UVW-8AUZJ".rsDecode())
        assertEquals(burstCrypto.getBurstAddressFromPassphrase("Burst Apps Team").burstID.signedLongId, "T7XD-7M3X-MB9F-38DU8".rsDecode())
    }
}
