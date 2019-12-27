package brs

import brs.util.Observable
import io.mockk.every
import io.mockk.just
import io.mockk.runs

inline fun <T, reified E: Enum<E>> Observable<T, E>.mockAddListener() {
    every { addListener(any(), any()) } just runs
}
