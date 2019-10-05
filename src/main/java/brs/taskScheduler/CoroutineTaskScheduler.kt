package brs.taskScheduler

import brs.Constants
import brs.util.delegates.Atomic
import kotlinx.coroutines.*

class CoroutineTaskScheduler(): TaskScheduler {
    private var started by Atomic(false) // Stays true after shutdown
    private val jobs = mutableListOf<Job>()
    private val scope = CoroutineScope(Dispatchers.Default)

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

    private fun runTask(task: Task) {
        jobs.add(scope.launch(block = task))
    }

    private fun taskToTask(task: Task): Task { // TODO catch stuff
        return task
    }

    private fun delayedTaskToTask(task: Task, initialDelayMs: Long, delayMs: Long): Task {
        return {
            val stopped by Atomic(false)
            delay(initialDelayMs)
            while (!stopped) {
                task()
                delay(delayMs)
            }
            // TODO cancel
        }
    }

    private fun repeatingTaskToTask(task: RepeatingTask): Task { // TODO catch stuff
        return {
            val stopped by Atomic(false)
            while (!stopped) {
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
        val beforeStartJobs = mutableListOf<Job>()
        beforeStartTasks.forEach { beforeStartJobs.add(scope.launch(block = taskToTask(it))) }
        beforeStartJobs.forEach { it.join() }

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
        val afterStartJobs = mutableListOf<Job>()
        beforeStartTasks.forEach { afterStartJobs.add(scope.launch(block = taskToTask(it))) }
        afterStartJobs.forEach { it.join() }
    }

    override fun shutdown() {
        if (!started) return
        jobs.forEach { it.cancel("Task Scheduler Shutting Down") }
        try {
            scope.cancel("Task Scheduler Shutting Down")
        } catch (ignored: IllegalStateException) {}
    }
}