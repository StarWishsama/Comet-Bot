/*
 * Copyright (c) 2022- Sorapointa
 *
 * 此源代码的使用受 Apache-2.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the Apache-2.0 License which can be found through the following link.
 *
 * https://github.com/Sorapointa/Sorapointa/blob/master/LICENSE
 */

package ren.natsuyuk1.comet.api.event

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.Yaml
import ren.natsuyuk1.comet.api.config.provider.PersistDataFile
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.file.configDirectory
import java.io.File
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KClass

private val logger = mu.KotlinLogging.logger {}

/**
 * Must call `init()` before any registration and `broadcastEvent(event)`
 *
 * @see [EventManager.init]
 */
object EventManager {

    private var eventScope = ModuleScope("EventManager")

    private val registeredListener = ConcurrentLinkedQueue<PriorityEntry>()
    private val registeredBlockListener = ConcurrentLinkedQueue<PriorityEntry>()
    private val registeredChannelListener = ConcurrentLinkedQueue<PriorityChannelEntry>()

    private val blockListenerTimeout
        get() = EventManagerConfig.data.blockListenerTimeout

    private val waitingAllBlockListenersTimeout
        get() = EventManagerConfig.data.waitingAllBlockListenersTimeout

    /**
     * Get original event flow from broadcasting
     * Flow would receive the event data in parallel
     *
     * You could **NOT CANCEL** or **INTERCEPT** any event in parallel listener.
     *
     * @param priority optional, set the priority of this listener
     * @return [Flow]
     */
    fun getEventFlow(
        priority: EventPriority = EventPriority.NORMAL,
    ): Flow<Event> {
        return registeredChannelListener.first { it.priority == priority }.channel.receiveAsFlow()
    }

    /**
     * Register a parallel listener
     * All registered listeners will be called in parallel.
     *
     * You could **NOT CANCEL** or **INTERCEPT** any event in parallel listener.
     *
     * @param priority optional, set the priority of this listener
     * @param listener lambda block of your listener with the all event of parameter
     */
    fun registerEventListener(
        priority: EventPriority = EventPriority.NORMAL,
        listener: suspend (Event) -> Unit,
    ) {
        registeredListener.first { it.priority == priority }.listeners.add(listener)
    }

    /**
     * Register a block listener
     * All registered block listeners will be called in serial.
     *
     * You could cancel or intercept listened event in serial listener.
     *
     * @param priority optional, set the priority of this listener
     * @param listener lambda block of your listener with all type of event of parameter
     */
    fun registerBlockEventListener(
        priority: EventPriority = EventPriority.NORMAL,
        listener: suspend (Event) -> Unit,
    ) {
        registeredBlockListener.first { it.priority == priority }.listeners.add(listener)
    }

    /**
     * Broadcast event, and return the cancel state of this event
     *
     * @param event the event will be broadcasted
     * @return [Boolean] that represents the cancel state of this event
     */

    suspend fun broadcastEvent(event: Event): Boolean {
        var isCancelled by atomic(false)
        var isIntercepted by atomic(false)
        val eventName = event::class.simpleName
        logger.trace { "Broadcasting event $eventName\n$event" }
        registeredListener.forEach { (priority, pListener) ->
            eventScope.launch {
                pListener.forEach { listener ->
                    listener(event)
                }
                registeredChannelListener
                    .first { it.priority == priority }.channel
                    .send(event)
            }
            val blockListeners = registeredBlockListener.first { it.priority == priority }.listeners
            if (blockListeners.size > 0) {
                withTimeout(waitingAllBlockListenersTimeout) {
                    blockListeners.map { listener ->
                        eventScope.launch {
                            withTimeout(blockListenerTimeout) {
                                listener(event)
                            }
                            isIntercepted = event.isIntercepted || isIntercepted
                            if (event is CancelableEvent) isCancelled = event.isCancelled || isCancelled
                        }
                    }.joinAll()
                }
            }
            if (isIntercepted) {
                logger.debug { "Event $eventName has been intercepted" }
                return isCancelled
            }
        }
        if (isCancelled) logger.debug { "Broadcasted event $eventName, has been cancelled" }
        return isCancelled
    }

    /**
     * Initialize the queue of listener in priority order
     * and coroutine context for structured concurrency
     *
     * This method **IS NOT** thread-safe
     */
    fun init(parentContext: CoroutineContext = EmptyCoroutineContext) {
        eventScope = ModuleScope("EventManager", parentContext)
        initAllListeners()
    }

    /**
     * Initialize the queue of listener in priority order
     *
     * This method **IS NOT** thread-safe
     */
    fun initAllListeners() {
        registeredListener.clear()
        registeredBlockListener.clear()
        EventPriority.values().forEach {
            registeredListener.add(PriorityEntry(it, ConcurrentLinkedQueue()))
            registeredBlockListener.add(PriorityEntry(it, ConcurrentLinkedQueue()))
            registeredChannelListener.add(PriorityChannelEntry(it, getInitChannel()))
        }
    }

    fun cancelAll() {
        eventScope.parentJob.cancel()
    }

    private fun getInitChannel() = Channel<Event>(64)
}

/**
 * Register a parallel temp listener to capture
 * the next event that conform with the requirement of `filter`
 *
 * You could NOT CANCEL or INTERCEPT any event in parallel listener.
 *
 * Generic type [T] is what event would you listen.
 *
 * Use `inline` with `listener` would lose precise stacktrace for exception
 *
 * @param timeoutMs timeout time in milliseconds.
 * @param priority optional, set the priority of this listener
 * @param filter lambda block of your filter for specific event detail
 * @return [Event] return a caputered event
 */
suspend inline fun <reified T : Event> nextEvent(
    timeoutMs: Long = 1000,
    priority: EventPriority = EventPriority.NORMAL,
    noinline filter: (T) -> Boolean = { true },
) = withTimeout(timeoutMs) {
    EventManager.getEventFlow(priority).firstOrNull { if (it is T) filter(it) else false }
}

/**
 * Register a parallel listener
 * All registered listeners will be called in parallel.
 *
 * You could NOT CANCEL or INTERCEPT any event in parallel listener.
 *
 * Generic type [T] is what event would you listen.
 *
 * Use `inline` with `listener` would lose precise stacktrace for exception
 *
 * @param priority optional, set the priority of this listener
 * @param listener lambda block of your listener with the specific event of parameter
 */
inline fun <reified T : Event> registerListener(
    priority: EventPriority = EventPriority.NORMAL,
    noinline listener: suspend (T) -> Unit,
) {
    EventManager.registerEventListener(priority) {
        if (it is T) {
            listener(it)
        }
    }
}

fun registerListener(
    eventClass: KClass<out Event>,
    priority: EventPriority = EventPriority.NORMAL,
    listener: suspend (Event) -> Unit,
) {
    EventManager.registerEventListener(priority) {
        if (it::class == eventClass) {
            listener(it)
        }
    }
}

/**
 * Register a block listener
 * All registered block listeners will be called in serial.
 *
 * You could cancel or intercept listened event in serial listener.
 *
 * Generic type [T] is what event would you listen.
 *
 * Use `inline` with `listener` would lose precise stacktrace for exception
 *
 * @param priority optional, set the priority of this listener
 * @param listener lambda block of your listener with the specific event of parameter
 */
inline fun <reified T : Event> registerBlockListener(
    priority: EventPriority = EventPriority.NORMAL,
    noinline listener: suspend (T) -> Unit,
) {
    EventManager.registerBlockEventListener(priority) {
        if (it is T) {
            listener(it)
        }
    }
}

/**
 * Broadcast event in a quick way
 *
 * Use `inline` would lose precise stacktrace for exception
 *
 * @param ifNotCancel lambda block with the action if broadcasted event has **NOT** been cancelled
 */
suspend inline fun <T : Event, R> T.broadcastEvent(
    noinline ifNotCancel: suspend (T) -> R,
): R? {
    val isCancelled = EventManager.broadcastEvent(this)
    return if (!isCancelled) ifNotCancel(this) else null
}

/**
 * Broadcast event in a quick way
 *
 * Use `inline` for lambda function would lose precise stacktrace for exception
 *
 * @param ifCancelled lambda block with the action if broadcasted event has been cancelled
 */
suspend inline fun <T : Event, R> T.broadcastEventIfCancelled(
    noinline ifCancelled: suspend (T) -> R,
): R? {
    val isCancelled = EventManager.broadcastEvent(this)
    return if (isCancelled) ifCancelled(this) else null
}

/**
 * Broadcast event in a quick way
 *
 * Use `inline` for lambda function would lose precise stacktrace for exception
 *
 * @param ifNotCancel lambda block with the action if broadcasted event has **NOT** been cancelled
 */
suspend inline fun <T : Event, R> T.broadcastEvent(
    noinline ifNotCancel: suspend (T) -> R,
    noinline ifCancelled: suspend (T) -> R,
): R {
    val isCancelled = EventManager.broadcastEvent(this)
    return if (!isCancelled) ifNotCancel(this) else ifCancelled(this)
}

/**
 * Broadcast event in quick way
 */
suspend inline fun <T : Event> T.broadcast() {
    EventManager.broadcastEvent(this)
}

object EventManagerConfig : PersistDataFile<EventManagerConfig.Data>(
    File(configDirectory, "eventManagerConfig.yaml"),
    Data.serializer(),
    Data(),
    Yaml,
) {

    @kotlinx.serialization.Serializable
    data class Data(
        @Comment("Single event listener timeout in ms")
        val blockListenerTimeout: Long = 1000 * 30L,
        @Comment("All event listeners timeout in ms")
        val waitingAllBlockListenersTimeout: Long = 3 * blockListenerTimeout,
    )
}

internal data class PriorityEntry(
    val priority: EventPriority,
    val listeners: ConcurrentLinkedQueue<suspend (Event) -> Unit>,
)

internal data class PriorityChannelEntry(
    val priority: EventPriority,
    val channel: Channel<Event>,
)
