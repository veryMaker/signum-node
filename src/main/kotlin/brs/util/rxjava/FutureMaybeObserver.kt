package brs.util.rxjava

import io.reactivex.Maybe
import io.reactivex.MaybeObserver
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper
import io.reactivex.internal.util.BlockingHelper
import io.reactivex.internal.util.ExceptionHelper.timeoutMessage
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicReference

fun <T> Maybe<T>.toFuture(): FutureMaybeObserver<T> {
    return this.subscribeWith(FutureMaybeObserver())
}

class FutureMaybeObserver<T> : CountDownLatch(1), MaybeObserver<T>, Future<T>, Disposable {
    internal var value: T? = null
    internal var error: Throwable? = null

    private val upstream: AtomicReference<Disposable> = AtomicReference()

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        while (true) {
            val a = upstream.get()
            if (a === this || a === DisposableHelper.DISPOSED) {
                return false
            }

            if (upstream.compareAndSet(a, DisposableHelper.DISPOSED)) {
                a?.dispose()
                countDown()
                return true
            }
        }
    }

    override fun isCancelled(): Boolean {
        return DisposableHelper.isDisposed(upstream.get())
    }

    override fun isDone(): Boolean {
        return count == 0L
    }

    @Throws(InterruptedException::class, ExecutionException::class)
    override fun get(): T? {
        if (count != 0L) {
            BlockingHelper.verifyNonBlocking()
            await()
        }

        if (isCancelled) {
            throw CancellationException()
        }
        val ex = error
        if (ex != null) {
            throw ExecutionException(ex)
        }
        return value
    }

    @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
    override fun get(timeout: Long, unit: TimeUnit): T? {
        if (count != 0L) {
            BlockingHelper.verifyNonBlocking()
            if (!await(timeout, unit)) {
                throw TimeoutException(timeoutMessage(timeout, unit))
            }
        }

        if (isCancelled) {
            throw CancellationException()
        }

        val ex = error
        if (ex != null) {
            throw ExecutionException(ex)
        }
        return value
    }

    override fun onSubscribe(d: Disposable) {
        DisposableHelper.setOnce(this.upstream, d)
    }

    override fun onSuccess(t: T) {
        val a = upstream.get()
        if (a === DisposableHelper.DISPOSED) {
            return
        }
        value = t
        upstream.compareAndSet(a, this)
        countDown()
    }

    override fun onComplete() {
        val a = upstream.get()
        if (a === DisposableHelper.DISPOSED) {
            return
        }
        value = null
        upstream.compareAndSet(a, this)
        countDown()
    }

    override fun onError(t: Throwable) {
        while (true) {
            val a = upstream.get()
            if (a === DisposableHelper.DISPOSED) {
                RxJavaUtils.handleError(t)
                return
            }
            error = t
            if (upstream.compareAndSet(a, this)) {
                countDown()
                return
            }
        }
    }

    override fun dispose() {
        // ignoring as `this` means a finished Disposable only
    }

    override fun isDisposed(): Boolean {
        return isDone
    }
}
