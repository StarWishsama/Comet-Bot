package ren.natsuyuk1.comet.api.task

import com.cronutils.model.Cron
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import java.util.concurrent.ConcurrentHashMap
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
     * Simple run a suspend task
     *
     * @return task job
     */
    fun run(task: suspend () -> Unit): Job

    fun runLater(task: suspend () -> Unit, delay: Duration): Job

    /**
     * Register a simple delayed task
     *
     * @return task job
     */
    fun registerTask(
        delay: Duration,
        task: suspend () -> Unit,
    ): Job

    /**
     * Register a simple delayed task
     *
     * @return task job
     */
    fun registerTask(
        delayMillis: Long,
        task: suspend () -> Unit,
    ): Job

    fun registerTaskDelayed(
        delay: Duration,
        task: suspend () -> Unit,
    ): Job

    fun registerTask(
        delay: suspend () -> Unit,
        task: suspend () -> Unit,
    ): Job

    /**
     * Register a task
     *
     * @return If this job has been registered already, it would return null
     */
    fun registerTask(
        id: String,
        cron: String,
        task: suspend () -> Unit,
    ): Job?

    /**
     * Register a task
     *
     * @return If this job has been registered already, it would return null
     */
    fun registerTask(
        id: String,
        cron: Cron,
        task: suspend () -> Unit,
    ): Job?
}

object TaskManager : ITaskManager {
    private var scope = ModuleScope("TaskManager")
    private val cronJobMap: MutableMap<String, Job> = ConcurrentHashMap()

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
     * Simple run a suspend task
     *
     * @return task job
     */
    override fun run(task: suspend () -> Unit): Job = scope.launch { task() }

    override fun runLater(task: suspend () -> Unit, delay: Duration): Job =
        scope.launch {
            delay(delay)
            task()
        }

    /**
     * Register a simple delayed task
     *
     * @return task job
     */
    override fun registerTask(
        delay: Duration,
        task: suspend () -> Unit,
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
        task: suspend () -> Unit,
    ): Job = scope.launch {
        while (isActive) {
            task()
            delay(delayMillis)
        }
    }

    override fun registerTaskDelayed(
        delay: Duration,
        task: suspend () -> Unit,
    ): Job = scope.launch {
        while (isActive) {
            delay(delay)
            task()
        }
    }

    override fun registerTask(
        delay: suspend () -> Unit,
        task: suspend () -> Unit,
    ): Job =
        scope.launch {
            while (isActive) {
                delay()
                task()
            }
        }

    /**
     * Register a task
     *
     * @return If this job has been registered already, it would return null
     */
    override fun registerTask(
        id: String,
        cron: String,
        task: suspend () -> Unit,
    ): Job? = registerTask(id, parseCron(cron), task)

    /**
     * Register a task
     *
     * @return If this job has been registered already, it would return null
     */
    override fun registerTask(
        id: String,
        cron: Cron,
        task: suspend () -> Unit,
    ): Job? {
        if (cronJobMap[id] != null) {
            logger.warn { "Conflicted cron task id '$id', return null..." }
            return null
        }

        return scope.launch {
            transaction {
                val found = CronTask.findById(id) ?: CronTask.new(id) { this.cron = cron }
                found.cron = cron
            }

            while (isActive) {
                val beforeTime = Clock.System.now()
                val taskData = transaction {
                    CronTask.findById(id) ?: error("Failed to get task data for id $id")
                }

                delay(taskData.nextExecution(beforeTime) - beforeTime)

                listOf(
                    launch {
                        task()
                    },
                    launch {
                        transaction {
                            taskData.lastExecution = Clock.System.now()
                        }
                    },
                ).joinAll()
            }
        }.also {
            cronJobMap[id] = it
        }
    }
}
