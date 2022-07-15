package ren.natsuyuk1.comet.api.event.impl.message

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.user.Contact
import ren.natsuyuk1.comet.utils.message.MessageWrapper

/**
 * 群消息事件.
 */
class GroupMessageEvent(
    override val comet: Comet,
    override val subject: Contact,
    override val sender: Contact,
    override val senderName: String,
    override val message: MessageWrapper,
    override val time: Long,
    override val messageID: Long
) : MessageEvent() {
}
