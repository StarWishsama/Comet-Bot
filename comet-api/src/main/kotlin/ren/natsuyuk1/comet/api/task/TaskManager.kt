package ren.natsuyuk1.comet.api.task

import kotlinx.coroutines.*
import mu.KotlinLogging
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

interface ITaskManager {

    /**
     * Init task manager scope for structured concurrency
     *
     * This method **IS NOT** thread-safe
     */
    fun init(parentContext: CoroutineContext = EmptyCoroutineContext)

    fun close()

    /**
     * Register a simple delayed task
     *
     * @return task job
     */
    fun registerTask(
        delay: Duration,
        task: suspend () -> Unit
    ): Job

    /**
     * Register a simple delayed task
     *
     * @return task job
     */
    fun registerTask(
        delayMillis: Long,
        task: suspend () -> Unit
    ): Job

    fun registerTaskDelayed(
        delay: Duration,
        task: suspend () -> Unit
    ): Job
}

object TaskManager : ITaskManager {
    private var scope = ModuleScope("TaskManager")

    // private val cronJobMap: MutableMap<String, Job> = ConcurrentHashMap()

    /**
     * Init task manager scope for structured concurrency
     *
     * This method **IS NOT** thread-safe
     */
    override fun init(parentContext: CoroutineContext) {
        scope = ModuleScope("TaskManager", parentContext)
    }

    override fun close() {
        scope.cancel("Closing")
    }

    /**
     * Register a simple delayed task
     *
     * @return task job
     */
    override fun registerTask(
        delay: Duration,
        task: suspend () -> Unit
    ): Job = scope.launch {
        while (isActive) {
            task()
            delay(delay)
        }
    }

    /**
     * Register a simple delayed task
     *
     * @return task job
     */
    override fun registerTask(
        delayMillis: Long,
        task: suspend () -> Unit
    ): Job = scope.launch {
        while (isActive) {
            task()
            delay(delayMillis)
        }
    }

    override fun registerTaskDelayed(
        delay: Duration,
        task: suspend () -> Unit
    ): Job = scope.launch {
        while (isActive) {
            delay(delay)
            task()
        }
    }
}
