package ren.natsuyuk1.comet.api.event.events.group

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.events.message.IUserEvent
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.User

class GroupLeaveEvent(
    override val comet: Comet,
    /**
     * 群聊
     */
    override val group: Group,
    /**
     * 退出的用户
     */
    override val user: User,
) : GroupEvent(comet), IUserEvent
