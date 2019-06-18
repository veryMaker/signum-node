package brs.util

import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream

class CountingOutputStream(out: OutputStream) : FilterOutputStream(out) {

    var count: Long = 0
        private set

    @Throws(IOException::class)
    override fun write(b: Int) {
        count += 1
        super.write(b)
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray) {
        count += b.size.toLong()
        super.write(b)
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
        count += len.toLong()
        super.write(b, off, len)
    }
}
