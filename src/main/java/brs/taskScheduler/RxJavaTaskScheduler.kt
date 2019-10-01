package brs.taskScheduler

import brs.Constants
import brs.DependencyProvider
import brs.util.delegates.Atomic
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean

class RxJavaTaskScheduler(dp: DependencyProvider): TaskScheduler {
    private var started by Atomic(false) // Stays true after shutdown
    private val disposables = CompositeDisposable()

    private val beforeStartTasks = mutableListOf<Task>()
    private val beforeStartTasksLock = Any()
    private val afterStartTasks = mutableListOf<Task>()
    private val afterStartTasksLock = Any()
    private val scheduledTasks = mutableListOf<RepeatingTask>()
    private val scheduledTasksLock = Any()
    private val scheduledParallelTasks = mutableMapOf<RepeatingTask, Int>()
    private val scheduledParallelTasksLock = Any()

    private fun requireStarted() {
        require(started) { "Task Scheduler has not yet started" }
    }

    private fun requireNotStarted() {
        require(!started) { "Task Scheduler has already started" }
    }

    override fun run(task: Task) {
        requireStarted()
        runTask(taskToCompletable(task))
    }

    override fun runBeforeStart(task: Task) {
        requireNotStarted()
        synchronized(beforeStartTasksLock) {
            beforeStartTasks.add(task)
        }
    }

    override fun runAfterStart(task: Task) {
        requireNotStarted()
        synchronized(afterStartTasksLock) {
            afterStartTasks.add(task)
        }
    }

    override fun scheduleTask(task: RepeatingTask) {
        requireNotStarted()
        synchronized(scheduledTasksLock) {
            scheduledTasks.add(task)
        }
    }

    override fun scheduleTask(numberOfInstances: Int, task: RepeatingTask) {
        requireNotStarted()
        synchronized(scheduledParallelTasksLock) {
            scheduledParallelTasks[task] = numberOfInstances
        }
    }

    private fun runTask(task: Completable) {
        disposables.add(task.subscribeOn(Schedulers.computation()).subscribe())
    }

    private fun taskToCompletable(task: Task): Completable { // TODO catch stuff
        return Completable.fromAction { runBlocking(block = task) }
    }

    private fun repeatingTaskToCompletable(task: RepeatingTask): Completable { // TODO catch stuff
        return Completable.create {
            runBlocking {
                val stopped = AtomicBoolean(false)
                while (!stopped.get()) {
                    if (!task()) {
                        delay(Constants.TASK_FAILURE_DELAY_MS)
                    }
                }
                it.setCancellable {
                    stopped.set(true)
                }
            }
        }
    }

    override fun start() {
        requireNotStarted()
        started = true
        // Run before start tasks
        synchronized(beforeStartTasksLock) {
            Completable.merge(beforeStartTasks.map { taskToCompletable(it) })
                .subscribeOn(Schedulers.io())
                .blockingAwait()
        }

        // Start regular scheduled tasks
        synchronized(scheduledTasksLock) {
            scheduledTasks.forEach { runTask(repeatingTaskToCompletable(it)) }
        }
        // Start parallel scheduled tasks
        synchronized(scheduledParallelTasksLock) {
            scheduledParallelTasks.forEach { (task, numberOfInstances) ->
                val runner = repeatingTaskToCompletable(task)
                repeat(numberOfInstances) {
                    runTask(runner)
                }
            }
        }

        // Run after start tasks
        synchronized(afterStartTasksLock) {
            Completable.merge(afterStartTasks.map { taskToCompletable(it) })
                .subscribeOn(Schedulers.io())
                .blockingAwait()
        }
    }

    override fun shutdown() {
        if (!started) return
        disposables.dispose()
    }
}