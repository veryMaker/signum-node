package brs.util

import brs.objects.FluxValues
import brs.services.FluxCapacitorService
import brs.util.crypto.Crypto
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.experimental.xor

class MiningPlot(addr: Long, nonce: Long, blockHeight: Int, fluxCapacitorService: FluxCapacitorService) {
    private val data = ByteArray(PLOT_AND_SEED_SIZE)

    init {
        val baseBuffer = ByteBuffer.wrap(data, PLOT_SIZE, SEED_SIZE)
        baseBuffer.putLong(addr)
        baseBuffer.putLong(nonce)
        val shabal256 = Crypto.shabal256()
        var i = PLOT_SIZE
        while (i > 0) {
            var len = PLOT_AND_SEED_SIZE - i
            if (len > HASH_CAP) {
                len = HASH_CAP
            }
            shabal256.update(data, i, len)
            System.arraycopy(shabal256.digest(), 0, data, i - HASH_SIZE, HASH_SIZE)
            i -= HASH_SIZE
        }
        val finalhash = shabal256.digest(data)
        for (index in 0 until PLOT_SIZE) {
            data[index] = (data[index] xor finalhash[index % HASH_SIZE])
        }
        //PoC2 Rearrangement
        if (fluxCapacitorService.getValue(FluxValues.POC2, blockHeight)) {
            val hashBuffer = ByteArray(HASH_SIZE)
            var revPos = PLOT_SIZE - HASH_SIZE //Start at second hash in last scoop
            var pos = 32
            while (pos < PLOT_SIZE / 2) { //Start at second hash in first scoop
                System.arraycopy(data, pos, hashBuffer, 0, HASH_SIZE) //Copy low scoop second hash to buffer
                System.arraycopy(
                    data,
                    revPos,
                    data,
                    pos,
                    HASH_SIZE
                ) //Copy high scoop second hash to low scoop second hash
                System.arraycopy(hashBuffer, 0, data, revPos, HASH_SIZE) //Copy buffer to high scoop second hash
                revPos -= 64 //move backwards
                pos += 64
            }
        }
    }

    fun getScoop(pos: Int): ByteArray {
        return data.copyOfRange(pos * SCOOP_SIZE, (pos + 1) * SCOOP_SIZE)
    }

    fun hashScoop(shabal256: MessageDigest, pos: Int) {
        shabal256.update(data, pos * SCOOP_SIZE, SCOOP_SIZE)
    }

    companion object {
        private const val SEED_SIZE = 16
        private const val HASH_SIZE = 32
        private const val HASHES_PER_SCOOP = 2
        const val SCOOP_SIZE = HASHES_PER_SCOOP * HASH_SIZE
        private const val SCOOPS_PER_PLOT = 4096 // original 1MB/plot = 16384
        const val PLOT_SIZE = SCOOPS_PER_PLOT * SCOOP_SIZE
        private const val PLOT_AND_SEED_SIZE = PLOT_SIZE + SEED_SIZE
        private const val HASH_CAP = 4096
    }
}
