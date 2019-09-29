package brs.taskScheduler

import brs.Constants
import brs.DependencyProvider
import brs.util.delegates.Atomic
import kotlinx.coroutines.*

class TaskSchedulerImpl(dp: DependencyProvider): TaskScheduler {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var started by Atomic(false) // Stays true after shutdown

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
        scope.launch(block = task)
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

    private fun runRepeatingTask(task: RepeatingTask): suspend CoroutineScope.() -> Unit = {
        while (true) {
            if (!task()) delay(Constants.TASK_FAILURE_DELAY_MS)
        }
        // TODO cancellation?
    }

    override fun start() = runBlocking {
        requireNotStarted()
        started = true
        // TODO locks?
        // Run before start tasks
        val beforeStartJobs = mutableListOf<Job>()
        beforeStartTasks.forEach {
            beforeStartJobs.add(scope.launch(block = it))
        }
        beforeStartJobs.forEach {
            it.join()
        }

        // Start regular scheduled tasks
        scheduledTasks.forEach {
            scope.launch(block = runRepeatingTask(it))
        }
        // Start parallel scheduled tasks
        scheduledParallelTasks.forEach {(task, numberOfInstances) ->
            val runner = runRepeatingTask(task)
            repeat(numberOfInstances) {
                scope.launch(block = runner)
            }
        }

        // Run after start tasks
        val afterStartJobs = mutableListOf<Job>()
        afterStartTasks.forEach {
            afterStartJobs.add(scope.launch(block = it))
        }
        afterStartJobs.forEach {
            it.join()
        }
    }

    override fun shutdown() {
        if (!started) return
        try {
            scope.cancel()
        } catch (ignored: IllegalStateException) {}
    }
}