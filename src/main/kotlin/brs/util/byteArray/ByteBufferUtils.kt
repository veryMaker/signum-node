package brs.util.byteArray

import java.nio.ByteBuffer

/**
 * Skips [n] bytes. This is safe to use as a better way
 * to write `0` bytes **if the array was newly initialized**.
 * If it is recycled, the previous data will still be present.
 * @param n The number of bytes to skip.
 */
fun ByteBuffer.skip(n: Int) {
    position(position() + n)
}
