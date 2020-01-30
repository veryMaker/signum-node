@file:Suppress("NOTHING_TO_INLINE")

package brs.util.biginteger

import java.math.BigInteger

inline infix operator fun BigInteger.plus(other: Long): BigInteger {
    return this + other.toBigInteger()
}

inline infix operator fun BigInteger.plus(other: Int): BigInteger {
    return this + other.toBigInteger()
}

inline infix operator fun Long.plus(other: BigInteger): BigInteger {
    return this.toBigInteger() + other
}

inline infix operator fun Int.plus(other: BigInteger): BigInteger {
    return this.toBigInteger() + other
}

inline infix operator fun BigInteger.minus(other: Long): BigInteger {
    return this - other.toBigInteger()
}

inline infix operator fun BigInteger.minus(other: Int): BigInteger {
    return this - other.toBigInteger()
}

inline infix operator fun Long.minus(other: BigInteger): BigInteger {
    return this.toBigInteger() - other
}

inline infix operator fun Int.minus(other: BigInteger): BigInteger {
    return this.toBigInteger() - other
}

inline infix operator fun BigInteger.times(other: Long): BigInteger {
    return this * other.toBigInteger()
}

inline infix operator fun BigInteger.times(other: Int): BigInteger {
    return this * other.toBigInteger()
}

inline infix operator fun Long.times(other: BigInteger): BigInteger {
    return this.toBigInteger() * other
}

inline infix operator fun Int.times(other: BigInteger): BigInteger {
    return this.toBigInteger() * other
}

inline infix operator fun BigInteger.div(other: Long): BigInteger {
    return this / other.toBigInteger()
}

inline infix operator fun BigInteger.div(other: Int): BigInteger {
    return this / other.toBigInteger()
}

inline infix operator fun Long.div(other: BigInteger): BigInteger {
    return this.toBigInteger() / other
}

inline infix operator fun Int.div(other: BigInteger): BigInteger {
    return this.toBigInteger() / other
}

inline infix operator fun BigInteger.rem(other: Long): BigInteger {
    return this % other.toBigInteger()
}

inline infix operator fun BigInteger.rem(other: Int): BigInteger {
    return this % other.toBigInteger()
}

inline infix operator fun Long.rem(other: BigInteger): BigInteger {
    return this.toBigInteger() % other
}

inline infix operator fun Int.rem(other: BigInteger): BigInteger {
    return this.toBigInteger() % other
}

/**
 * This function is identical to the regular pow,
 * except it is an inline infix function. Recursion
 * is prevented by Kotlin's rule that if a member function
 * and extension function is available with the
 * same name, the member is always called.
 */
inline infix fun BigInteger.pow(exponent: Int): BigInteger {
    return this.pow(exponent)
}

inline infix fun Long.pow(exponent: Int): BigInteger {
    return this.toBigInteger() pow exponent
}

inline infix fun Int.pow(exponent: Int): BigInteger {
    return this.toBigInteger() pow exponent
}

inline infix operator fun BigInteger.compareTo(other: Long): Int {
    return this.compareTo(other.toBigInteger())
}

inline infix operator fun BigInteger.compareTo(other: Int): Int {
    return this.compareTo(other.toBigInteger())
}

inline infix operator fun Long.compareTo(other: BigInteger): Int {
    return this.toBigInteger().compareTo(other)
}

inline infix operator fun Int.compareTo(other: BigInteger): Int {
    return this.toBigInteger().compareTo(other)
}
