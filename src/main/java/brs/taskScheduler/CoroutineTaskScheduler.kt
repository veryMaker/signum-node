package brs.taskScheduler

import brs.Constants
import brs.DependencyProvider
import brs.util.delegates.Atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean

class CoroutineTaskScheduler(dp: DependencyProvider): TaskScheduler {
    private var started by Atomic(false) // Stays true after shutdown
    private val jobs = mutableListOf<Job>()
    private val scope = CoroutineScope(Dispatchers.Default)

    private val beforeStartTasks = mutableListOf<Task>()
    private val beforeStartTasksLock = Mutex()
    private val afterStartTasks = mutableListOf<Task>()
    private val afterStartTasksLock = Mutex()
    private val scheduledTasks = mutableListOf<RepeatingTask>()
    private val scheduledTasksLock = Mutex()
    private val scheduledParallelTasks = mutableMapOf<RepeatingTask, Int>()
    private val scheduledParallelTasksLock = Mutex()

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

    override suspend fun runBeforeStart(task: Task) {
        requireNotStarted()
        beforeStartTasksLock.withLock {
            beforeStartTasks.add(task)
        }
    }

    override suspend fun runAfterStart(task: Task) {
        requireNotStarted()
        afterStartTasksLock.withLock {
            afterStartTasks.add(task)
        }
    }

    override suspend fun scheduleTask(task: RepeatingTask) {
        requireNotStarted()
        scheduledTasksLock.withLock {
            scheduledTasks.add(task)
        }
    }

    override suspend fun scheduleTask(numberOfInstances: Int, task: RepeatingTask) {
        requireNotStarted()
        scheduledParallelTasksLock.withLock {
            scheduledParallelTasks[task] = numberOfInstances
        }
    }

    private fun runTask(task: Task) {
        jobs.add(scope.launch(block = task))
    }

    private fun taskToTask(task: Task): Task { // TODO catch stuff
        return task
    }

    private fun repeatingTaskToTask(task: RepeatingTask): Task { // TODO catch stuff
        return {
            val stopped = AtomicBoolean(false)
            while (!stopped.get()) {
                if (!task()) {
                    delay(Constants.TASK_FAILURE_DELAY_MS)
                }
            }
            // TODO cancel
        }
    }

    override suspend fun start() {
        requireNotStarted()
        started = true
        // Run before start tasks
        beforeStartTasksLock.withLock {
            val beforeStartJobs = mutableListOf<Job>()
            beforeStartTasks.forEach { beforeStartJobs.add(scope.launch(block = taskToTask(it))) }
            beforeStartJobs.forEach { it.join() }
        }

        // Start regular scheduled tasks
        scheduledTasksLock.withLock {
            scheduledTasks.forEach { runTask(repeatingTaskToTask(it)) }
        }
        // Start parallel scheduled tasks
        scheduledParallelTasksLock.withLock {
            scheduledParallelTasks.forEach { (task, numberOfInstances) ->
                val runner = repeatingTaskToTask(task)
                repeat(numberOfInstances) {
                    runTask(runner)
                }
            }
        }

        // Run after start tasks
        afterStartTasksLock.withLock {
            val afterStartJobs = mutableListOf<Job>()
            beforeStartTasks.forEach { afterStartJobs.add(scope.launch(block = taskToTask(it))) }
            afterStartJobs.forEach { it.join() }
        }
    }

    override fun shutdown() {
        if (!started) return
        jobs.forEach { it.cancel("Task Scheduler Shutting Down") }
        try {
            scope.cancel("Task Scheduler Shutting Down")
        } catch (ignored: IllegalStateException) {}
    }
}