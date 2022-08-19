package ren.natsuyuk1.comet.api.event.events.group

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.CometEvent
import ren.natsuyuk1.comet.api.user.Group

interface IGroupEvent {
    val group: Group
}

abstract class GroupEvent(override val comet: Comet) : CometEvent(comet), IGroupEvent
