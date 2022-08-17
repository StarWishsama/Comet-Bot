package ren.natsuyuk1.comet.api.event.impl.message

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.utils.message.MessageWrapper

class PrivateMessageEvent(
    override val comet: Comet,
    override val sender: User,
    override val subject: User,
    override val senderName: String,
    override val message: MessageWrapper,
    override val time: Long,
    override val messageID: Long
) : MessageEvent(comet)
