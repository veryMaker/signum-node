package brs.taskScheduler

import kotlinx.coroutines.CoroutineScope

typealias Task = suspend CoroutineScope.() -> Unit
typealias NoSuspendTask = () -> Unit // TODO hack. remove
typealias RepeatingTask = suspend CoroutineScope.() -> Boolean
typealias NoSuspendRepeatingTask = () -> Boolean // TODO hack. remove

interface TaskScheduler {
    /**
     * Runs a task immediately. If the scheduler has not started yet, it throws.
     */
    fun run(task: Task)

    /**
     * Runs a task when start() is called, before starting the scheduler. If the scheduler has already started, it throws.
     */
    fun runBeforeStart(task: Task)
    fun runBeforeStartHack(task: NoSuspendTask) // TODO hack. remove

    /**
     * Runs a task once, after the scheduler has been started. If the scheduler has already started, it throws.
     */
    fun runAfterStart(task: Task)

    /**
     * Schedules a task to be repeatedly run forever when the scheduler starts.
     * The task returns true if successful (meaning it should be run again ASAP)
     * or false if unsuccessful (meaning it should have a delay before running again)
     * If the scheduler has already started, it throws.
     */
    fun scheduleTask(task: RepeatingTask)
    fun scheduleTaskHack(task: NoSuspendRepeatingTask) // TODO hack. remove

    /**
     * Schedules n instances of a task to be simultaneously repeatedly run when the scheduler starts.
     * The task returns true if successful (meaning it should be run again ASAP)
     * or false if unsuccessful (meaning it should have a delay before running again)
     * If the scheduler has already started, it throws.
     */
    fun scheduleTask(numberOfInstances: Int, task: RepeatingTask)
    fun scheduleTaskHack(numberOfInstances: Int, task: NoSuspendRepeatingTask) // TODO hack. remove

    /**
     * Starts the scheduler - runs any tasks scheduled to be run before start
     * and then starts repeatedly running scheduled tasks.
     * If the scheduler has already started, it throws.
     */
    fun start()

    /**
     * Shutdown the scheduler, stopping all tasks. TODO should we just wait for every task to finish?
     * If the scheduler has not yet started, this is a no-op.
     */
    fun shutdown()
}