package ren.natsuyuk1.comet.api.event.events.comet

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.CancelableEvent
import ren.natsuyuk1.comet.api.event.CometEvent
import ren.natsuyuk1.comet.api.user.Contact
import ren.natsuyuk1.comet.utils.message.MessageWrapper

/**
 * 一个消息将要被发出的事件.
 *
 * @param comet 发出该消息的 [Comet] 实例
 * @param target 发送消息的对象
 * @param message 将要发出的消息 [MessageWrapper]
 * @param timestamp 发送该消息时的时间戳
 */
class MessagePreSendEvent(
    override val comet: Comet,
    val target: Contact,
    val message: MessageWrapper,
    val timestamp: Long
) : CometEvent(comet), CancelableEvent
