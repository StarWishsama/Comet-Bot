package ren.natsuyuk1.comet.api.event.impl.group

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.CometEvent
import ren.natsuyuk1.comet.api.user.Contact

sealed interface IGroupEvent {
    val comet: Comet

    val group: Contact
}

abstract class GroupEvent(override val comet: Comet) : CometEvent(comet), IGroupEvent
