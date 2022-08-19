package ren.natsuyuk1.comet.api.event.events.message

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.CometEvent
import ren.natsuyuk1.comet.api.user.Contact
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.utils.message.MessageWrapper

sealed interface IMessageEvent {
    val comet: Comet

    val subject: Contact

    val sender: User

    val senderName: String

    val message: MessageWrapper

    val time: Long

    val messageID: Long
}

abstract class MessageEvent(override val comet: Comet) : CometEvent(comet), IMessageEvent
