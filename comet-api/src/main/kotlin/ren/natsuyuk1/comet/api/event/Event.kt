/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.event

import kotlinx.atomicfu.atomic

/**
 * Event Interface, if you want to realize your own events,
 * please inherit `AbstractEvent` or `AbstractCancellableEvent`,
 * **DON'T implement** this interface, it's sealed interface.
 *
 * @property [isIntercepted] whether this event has been intercepted
 */
sealed interface Event {

    val isIntercepted: Boolean

    /**
     * Intercept and stop this event broadcasting
     * It could intercept events broadcast to lower priority event listener.
     *
     * @see [EventManager.broadcastEvent]
     * @see [EventPriority]
     */
    fun intercept()
}

/**
 * Cancelable Event Interface
 * If you want to the event could be cancelled, you should implement this interface.
 *
 * @property [isCancelled] whether this event has been cancelled
 */
interface CancelableEvent : Event {

    val isCancelled: Boolean

    /**
     * Cancel this event and the state of event
     * will return to the call site of `broadcastEvent()` method.
     *
     * You must make sure you are cancelling a cancellable event,
     * otherwise, it would throw an exception
     *
     * @see [EventManager.broadcastEvent]
     */
    fun cancel()
}

/**
 * `AbstractEvent` includes interception and cancellation method,
 * which could intercept the event broadcast to lower priority event listener,
 * and also could cancel the event, the final cancellation result
 * will return to the call site of `broadcastEvent()` method.
 *
 * @property [isIntercepted] whether this event has been intercepted
 * @property [isCancelled] whether this event has been cancelled
 *
 * @see [EventManager.broadcastEvent]
 * @see [EventPriority]
 */
abstract class AbstractEvent : Event {

    final override var isIntercepted by atomic(false)
        private set

    @Suppress("MemberVisibilityCanBePrivate")
    var isCancelled: Boolean by atomic(false)
        private set

    /**
     * Intercept and stop this event broadcasting
     * It could intercept events broadcast to lower priority event listener.
     *
     * @see [EventManager.broadcastEvent]
     * @see [EventPriority]
     */
    final override fun intercept() {
        isIntercepted = true
    }

    /**
     * Cancel this event and the state of event
     * will return to the call site of `broadcastEvent()` method.
     *
     * You must make sure you are cancelling a cancellable event,
     * otherwise, it would throw an exception
     *
     * @see [EventManager.broadcastEvent]
     */
    fun cancel() {
        require(this is CancelableEvent) { "Event could not be cancelled" }
        isCancelled = true
    }
}

/**
 * Event broadcast priority
 * Decreasing priority from left to right
 * Same priority event listeners would be called in parallel.
 *
 * @see [EventManager.broadcastEvent]
 */
enum class EventPriority {
    HIGHEST, HIGH, NORMAL, LOW, LOWEST
}
