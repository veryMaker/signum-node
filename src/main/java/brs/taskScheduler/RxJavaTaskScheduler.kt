package brs.taskScheduler

import brs.Constants
import brs.util.delegates.Atomic
import brs.util.rxjava.toFuture
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Future

class RxJavaTaskScheduler: TaskScheduler {
    private var started by Atomic(false) // Stays true after shutdown
    private val disposables = CompositeDisposable()

    private val beforeStartTasks = mutableListOf<Task>()
    private val afterStartTasks = mutableListOf<Task>()
    private val scheduledWithDelayTasks = mutableMapOf<Task, Pair<Long, Long>>()
    private val scheduledTasks = mutableListOf<RepeatingTask>()
    private val scheduledParallelTasks = mutableMapOf<RepeatingTask, Int>()

    private fun requireStarted() {
        require(started) { "Task Scheduler has not yet started" }
    }

    private fun requireNotStarted() {
        require(!started) { "Task Scheduler has already started" }
    }

    override fun run(task: Task) {
        requireStarted()
        runTask(taskToTask(task))
    }

    override fun <T : Any> async(task: TaskWithResult<T>): Future<T?> {
        return Maybe.fromCallable(task).subscribeOn(Schedulers.computation())
            .toFuture()
    }

    override fun runBeforeStart(task: Task) {
        requireNotStarted()
        beforeStartTasks.add(task)
    }

    override fun runAfterStart(task: Task) {
        requireNotStarted()
        afterStartTasks.add(task)
    }

    override fun scheduleTaskWithDelay(initialDelayMs: Long, delayMs: Long, task: Task) {
        requireNotStarted()
        scheduledWithDelayTasks[task] = Pair(initialDelayMs, delayMs)
    }

    override fun scheduleTask(task: RepeatingTask) {
        requireNotStarted()
        scheduledTasks.add(task)
    }

    override fun scheduleTask(numberOfInstances: Int, task: RepeatingTask) {
        requireNotStarted()
        scheduledParallelTasks[task] = numberOfInstances
    }

    private fun runTask(task: Completable) {
        disposables.add(task.subscribeOn(Schedulers.io()).subscribe())
    }

    override fun awaitTasks(tasks: Iterable<Task>) {
        Completable.merge(tasks.map { taskToTask(it) })
            .subscribeOn(Schedulers.computation())
            .blockingAwait()
    }

    private fun taskToTask(task: Task): Completable { // TODO catch stuff
        return Completable.fromAction(task)
    }

    private fun delayedTaskToTask(task: Task, initialDelayMs: Long, delayMs: Long): Completable {
        return Completable.create {
            Thread.sleep(initialDelayMs)
            while (!it.isDisposed) {
                task()
                Thread.sleep(delayMs)
            }
        }
    }

    private fun repeatingTaskToTask(task: RepeatingTask): Completable { // TODO catch stuff
        return Completable.create {
            while (!it.isDisposed) {
                if (!task()) {
                    Thread.sleep(Constants.TASK_FAILURE_DELAY_MS)
                }
            }
        }
    }

    override fun start() {
        requireNotStarted()
        started = true
        // Run before start tasks
        awaitTasks(beforeStartTasks)

        // Start regular scheduled tasks
        scheduledTasks.forEach { runTask(repeatingTaskToTask(it)) }

        // Start parallel scheduled tasks
        scheduledParallelTasks.forEach { (task, numberOfInstances) ->
            val runner = repeatingTaskToTask(task)
            repeat(numberOfInstances) {
                runTask(runner)
            }
        }
        // Start delayed scheduled tasks
        scheduledWithDelayTasks.forEach { (task, delays) -> runTask(delayedTaskToTask(task, delays.first, delays.second)) }

        // Run after start tasks
        awaitTasks(afterStartTasks)
    }

    override fun shutdown() {
        if (!started) return
        disposables.dispose()
    }
}