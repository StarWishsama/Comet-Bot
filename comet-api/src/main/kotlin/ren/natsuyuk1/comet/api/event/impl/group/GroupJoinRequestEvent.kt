package ren.natsuyuk1.comet.api.event.impl.group

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.user.Contact

class GroupJoinRequestEvent(
    override val comet: Comet,
    override val group: Contact
) : GroupEvent(comet)
