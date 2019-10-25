package brs.util.sync

import java.util.concurrent.Semaphore

inline class Mutex(val semaphore: Semaphore = Semaphore(1)) {
    inline fun <T> withLock(action: () -> T): T {
        semaphore.acquire()
        try {
            return action()
        } finally {
            semaphore.release()
        }
    }
}