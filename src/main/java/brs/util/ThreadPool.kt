package brs.util

import brs.DependencyProvider
import brs.props.Props
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ThreadPool(private val dp: DependencyProvider) {
    private val backgroundJobs = mutableMapOf<() -> Unit, Long>()
    private val backgroundJobsCores = mutableMapOf<() -> Unit, Long>()
    private val beforeStartJobs = mutableListOf<() -> Unit>()
    private val lastBeforeStartJobs = mutableListOf<() -> Unit>()
    private val afterStartJobs = mutableListOf<() -> Unit>()

    @Synchronized
    fun runBeforeStart(runnable: () -> Unit, runLast: Boolean) {
        check(scheduledThreadPool == null) { "Executor service already started" }
        if (runLast) {
            lastBeforeStartJobs.add(runnable)
        } else {
            beforeStartJobs.add(runnable)
        }
    }

    @Synchronized
    fun runAfterStart(runnable: () -> Unit) {
        afterStartJobs.add(runnable)
    }

    fun scheduleThread(name: String, runnable: () -> Unit, delay: Int) {
        scheduleThread(name, runnable, delay, TimeUnit.SECONDS)
    }

    @Synchronized
    fun scheduleThread(name: String, action: () -> Unit, delay: Int, timeUnit: TimeUnit) {
        check(scheduledThreadPool == null) { "Executor service already started, no new jobs accepted" }
        if (!dp.propertyService.get("brs.disable" + name + "Thread", false)) {
            backgroundJobs[action] = timeUnit.toMillis(delay.toLong())
        } else {
            logger.info("Will not run {} thread", name)
        }
    }

    fun scheduleThreadCores(runnable: () -> Unit, delay: Int) {
        scheduleThreadCores(runnable, delay, TimeUnit.SECONDS)
    }

    @Synchronized
    fun scheduleThreadCores(runnable: () -> Unit, delay: Int, timeUnit: TimeUnit) {
        check(scheduledThreadPool == null) { "Executor service already started, no new jobs accepted" }
        backgroundJobsCores[runnable] = timeUnit.toMillis(delay.toLong())
    }

    @Synchronized
    fun start(timeMultiplier: Int) {
        check(scheduledThreadPool == null) { "Executor service already started" }

        logger.debug("Running {} tasks...", beforeStartJobs.size)
        runAll(beforeStartJobs)
        beforeStartJobs.clear()

        logger.debug("Running {} final tasks...", lastBeforeStartJobs.size)
        runAll(lastBeforeStartJobs)
        lastBeforeStartJobs.clear()

        var cores = dp.propertyService.get(Props.CPU_NUM_CORES)
        if (cores <= 0) {
            logger.warn("Cannot use 0 cores - defaulting to all available")
            cores = Runtime.getRuntime().availableProcessors()
        }
        val totalThreads = backgroundJobs.size + backgroundJobsCores.size * cores
        logger.debug("Starting {} background jobs", totalThreads)
        scheduledThreadPool = Executors.newScheduledThreadPool(totalThreads)
        for ((inner, value) in backgroundJobs) {
            val toRun = {
                try {
                    inner()
                } catch (e: Exception) {
                    logger.warn("Uncaught exception while running background thread " + inner.javaClass.simpleName, e)
                }
            }
            scheduledThreadPool!!.scheduleWithFixedDelay(toRun, 0, Math.max(value / timeMultiplier, 1), TimeUnit.MILLISECONDS)
        }
        backgroundJobs.clear()

        // Starting multicore-Threads:
        for ((key, value) in backgroundJobsCores) {
            for (i in 0 until cores)
                scheduledThreadPool!!.scheduleWithFixedDelay(key, 0, Math.max(value / timeMultiplier, 1), TimeUnit.MILLISECONDS)
        }
        backgroundJobsCores.clear()

        if (logger.isDebugEnabled) {
            logger.debug("Starting {} delayed tasks", afterStartJobs.size)
        }
        val thread = Thread {
            runAll(afterStartJobs)
            afterStartJobs.clear()
        }
        thread.isDaemon = true
        thread.start()
    }

    @Synchronized
    fun shutdown() {
        if (scheduledThreadPool != null) {
            logger.info("Stopping background jobs...")
            shutdownExecutor(scheduledThreadPool!!)
            scheduledThreadPool = null
            logger.info("...Done")
        }
    }

    fun shutdownExecutor(executor: ExecutorService) {
        running.lazySet(false)
        if (!executor.isTerminated) {
            executor.shutdown()
            try {
                executor.awaitTermination(dp.propertyService.get(Props.BRS_SHUTDOWN_TIMEOUT).toLong(), TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }

            if (!executor.isTerminated) {
                logger.error("some threads didn't terminate, forcing shutdown")
                executor.shutdownNow()
            }
        }
    }

    private fun runAll(jobs: List<() -> Unit>) {
        val threads = mutableListOf<Thread>()
        val errors = StringBuffer()
        for (runnable in jobs) {
            val thread = Thread {
                try {
                    runnable()
                } catch (t: Exception) {
                    errors.append(t.message).append('\n')
                    throw t
                }
            }
            thread.isDaemon = true
            thread.start()
            threads.add(thread)
        }
        for (thread in threads) {
            try {
                thread.join()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }

        }
        if (errors.length > 0) {
            throw RuntimeException("Errors running startup tasks:\n$errors")
        }
    }

    companion object {

        val running = AtomicBoolean(true)

        private val logger = LoggerFactory.getLogger(ThreadPool::class.java)

        private var scheduledThreadPool: ScheduledExecutorService? = null
    }

}
