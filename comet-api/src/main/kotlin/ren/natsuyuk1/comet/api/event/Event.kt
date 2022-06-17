/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.api.event

interface Event {
    /**
     * 事件是否已被拦截.
     *
     * 所有事件都可以被拦截, 拦截后低优先级的监听器不会处理到这个事件.
     */
    val isIntercepted: Boolean

    /**
     * 拦截这个事件
     *
     * 当事件被拦截之后, 优先级较低的监听器不会被调用.
     *
     */
    fun intercept()
}

interface CancelableEvent : Event {
    /**
     * 事件是否已被取消
     */
    val isCancelled: Boolean

    /**
     * 取消这个事件
     */
    fun cancel()
}
