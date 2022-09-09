package ren.natsuyuk1.comet.api.event.events.message

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember

/**
 * 群消息事件.
 */
class GroupMessageEvent(
    override val comet: Comet,
    override val subject: Group,
    override val sender: GroupMember,
    override val senderName: String,
    override val message: MessageWrapper,
    override val time: Long,
    override val messageID: Long
) : MessageEvent(comet)
