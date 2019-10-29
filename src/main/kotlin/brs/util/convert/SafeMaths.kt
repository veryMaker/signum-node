package brs.util.convert

import kotlin.math.abs

// overflow checking based on https://www.securecoding.cert.org/confluence/display/java/NUM00-J.+Detect+or+prevent+integer+overflow
fun Long.safeAdd(long: Long): Long {
    if (if (long > 0)
            this > Long.MAX_VALUE - long
        else
            this < Long.MIN_VALUE - long
    ) {
        throw ArithmeticException("Integer overflow")
    }
    return this + long
}

fun Long.safeSubtract(long: Long): Long {
    if (if (long > 0)
            this < Long.MIN_VALUE + long
        else
            this > Long.MAX_VALUE + long
    ) {
        throw ArithmeticException("Integer overflow")
    }
    return this - long
}

fun Long.safeMultiply(long: Long): Long {
    if (when {
            long > 0 -> this > Long.MAX_VALUE / long || this < Long.MIN_VALUE / long
            long < -1L -> this > Long.MIN_VALUE / long || this < Long.MAX_VALUE / long
            else -> long == -1L && this == Long.MIN_VALUE
        }
    ) {
        throw ArithmeticException("Integer overflow")
    }
    return this * long
}

fun Long.safeDivide(long: Long): Long {
    if (this == Long.MIN_VALUE && long == -1L) {
        throw ArithmeticException("Integer overflow")
    }
    return this / long
}

fun Long.safeNegate(): Long {
    if (this == Long.MIN_VALUE) {
        throw ArithmeticException("Integer overflow")
    }
    return -this
}

fun Long.safeAbs(): Long {
    if (this == Long.MIN_VALUE) {
        throw ArithmeticException("Integer overflow")
    }
    return abs(this)
}
