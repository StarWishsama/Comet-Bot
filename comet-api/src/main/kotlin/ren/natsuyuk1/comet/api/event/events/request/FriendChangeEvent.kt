package ren.natsuyuk1.comet.api.event.events.request

import ren.natsuyuk1.comet.api.event.AbstractEvent
import ren.natsuyuk1.comet.api.user.User

class FriendAddEvent(
    val friend: User,
) : AbstractEvent()

class FriendDeleteEvent(
    val friend: User,
) : AbstractEvent()
