package brs.taskScheduler

import brs.DependencyProvider
import brs.util.delegates.Atomic

class TaskSchedulerImpl(dp: DependencyProvider): TaskScheduler {
    val started by Atomic(false)

    val beforeStartTasks = mutableListOf<Task>()
    val beforeStartTasksLock = Any()
    val afterStartTasks = mutableListOf<Task>()
    val afterStartTasksLock = Any()

    val repeatingTasks = mutableListOf<RepeatingTask>()

    private fun requireStarted() {
        require(started) { "Task Scheduler has not yet started" }
    }

    private fun requireNotStarted() {
        require(!started) { "Task Scheduler has already started" }
    }

    override fun run(task: Task) {
        requireStarted()
        // TODO run task
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
        // TODO schedule the task
    }

    override fun scheduleTask(numberOfInstances: Int, task: RepeatingTask) {
        // TODO schedule the task
        requireNotStarted()
    }

    override fun start() {
        requireNotStarted()
        // TODO start the scheduler
    }

    override fun shutdown() {
        if (!started) return
        // TODO stop the scheduler
    }

    // TODO the below is hacky stuff.
    private fun noSuspendRepeatingToSuspend(noSuspend: NoSuspendRepeatingTask): RepeatingTask = { noSuspend.invoke() }
    private fun noSuspendToSuspend(noSuspend: NoSuspendTask): Task = { noSuspend.invoke() }
    override fun runBeforeStartHack(task: NoSuspendTask) = runBeforeStart(noSuspendToSuspend(task))
    override fun scheduleTaskHack(task: NoSuspendRepeatingTask) = scheduleTask(noSuspendRepeatingToSuspend(task))
    override fun scheduleTaskHack(numberOfInstances: Int, task: NoSuspendRepeatingTask) = scheduleTask(numberOfInstances, noSuspendRepeatingToSuspend(task))
}