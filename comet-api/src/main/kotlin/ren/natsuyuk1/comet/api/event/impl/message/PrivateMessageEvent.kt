package ren.natsuyuk1.comet.api.event.impl.message

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.user.Contact
import ren.natsuyuk1.comet.utils.message.MessageWrapper

class PrivateMessageEvent(
    override val comet: Comet,
    override val sender: Contact,
    override val subject: Contact = sender,
    override val senderName: String,
    override val message: MessageWrapper,
    override val time: Long,
    override val messageID: Long
) : MessageEvent(comet)
