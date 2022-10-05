package ren.natsuyuk1.comet.api.event.events.group

import ren.natsuyuk1.comet.api.event.events.message.IUserEvent
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.GroupMember

interface IGroupMemberEvent : IGroupEvent, IUserEvent {
    val member: GroupMember
    override val group: Group get() = member.group
    override val user: GroupMember get() = member
}
