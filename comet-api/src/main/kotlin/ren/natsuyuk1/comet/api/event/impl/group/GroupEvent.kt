package ren.natsuyuk1.comet.api.event.impl.group

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.AbstractEvent
import ren.natsuyuk1.comet.api.user.Contact

sealed interface IGroupEvent {
    val comet: Comet

    val group: Contact
}

abstract class GroupEvent : AbstractEvent(), IGroupEvent
