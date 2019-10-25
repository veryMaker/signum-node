package brs.services

import java.util.concurrent.Future

typealias Task = () -> Unit
typealias TaskWithResult<T> = () -> T?
typealias RepeatingTask = () -> Boolean

enum class TaskType {
    IO,
    COMPUTATION
}

interface TaskSchedulerService {
    /**
     * Runs a task immediately. If the scheduler has not started yet, it throws.
     */
    fun run(taskType: TaskType, task: Task)

    fun <T: Any> async(taskType: TaskType, task: TaskWithResult<T>): Future<T?>

    /**
     * Runs a task when start() is called, before starting the scheduler. If the scheduler has already started, it throws.
     */
    fun runBeforeStart(task: Task)

    /**
     * Runs a task once, after the scheduler has been started. If the scheduler has already started, it throws.
     */
    fun runAfterStart(task: Task)

    /**
     * Schedules a task to be repeatedly run forever when the scheduler starts.
     * The task will be run initialDelayMs milliseconds after scheduler start.
     * Once the task has completed, it will be run again after delayMs milliseconds.
     * This process will repeat.
     * If the scheduler has already started, it throws.
     */
    fun scheduleTaskWithDelay(taskType: TaskType, initialDelayMs: Long, delayMs: Long, task: Task)

    /**
     * Schedules a task to be repeatedly run forever when the scheduler starts.
     * The task returns true if successful (meaning it should be run again ASAP)
     * or false if unsuccessful (meaning it should have a delay before running again)
     * If the scheduler has already started, it throws.
     */
    fun scheduleTask(taskType: TaskType, task: RepeatingTask)

    /**
     * Schedules n instances of a task to be simultaneously repeatedly run when the scheduler starts.
     * The task returns true if successful (meaning it should be run again ASAP)
     * or false if unsuccessful (meaning it should have a delay before running again)
     * If the scheduler has already started, it throws.
     */
    fun scheduleTask(taskType: TaskType, numberOfInstances: Int, task: RepeatingTask)

    /**
     * Runs all tasks specified in parallel and returns once all have completed.
     */
    fun awaitTasks(tasksType: TaskType, tasks: Iterable<Task>)

    /**
     * Starts the scheduler - runs any tasks scheduled to be run before start
     * and then starts repeatedly running scheduled tasks.
     * If the scheduler has already started, it throws.
     */
    fun start()

    /**
     * Shutdown the scheduler, stopping all tasks.
     * If the scheduler has not yet started, this is a no-op.
     */
    fun shutdown()
}
