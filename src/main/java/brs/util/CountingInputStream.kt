package brs.util

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

class CountingInputStream(`in`: InputStream) : FilterInputStream(`in`) {

    var count: Long = 0
        private set

    @Throws(IOException::class)
    override fun read(): Int {
        val read = super.read()
        if (read >= 0) {
            count += 1
        }
        return read
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val read = super.read(b, off, len)
        if (read >= 0) {
            count += read.toLong()
        }
        return read
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        val skipped = super.skip(n)
        if (skipped >= 0) {
            count += skipped
        }
        return skipped
    }

}
