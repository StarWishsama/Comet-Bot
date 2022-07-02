package ren.natsuyuk1.comet.api.event.impl.message

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.AbstractEvent
import ren.natsuyuk1.comet.api.user.Contact
import ren.natsuyuk1.comet.utils.message.MessageWrapper

sealed interface MessageEvent {
    val comet: Comet

    val subject: Contact

    val sender: Contact

    val senderName: String

    val message: MessageWrapper

    val time: Int
}

abstract class AbstractMessageEvent : AbstractEvent(), MessageEvent
