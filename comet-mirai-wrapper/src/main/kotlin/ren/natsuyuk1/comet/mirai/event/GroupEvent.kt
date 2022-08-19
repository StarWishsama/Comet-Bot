package ren.natsuyuk1.comet.mirai.event

import net.mamoe.mirai.event.events.MemberJoinEvent
import ren.natsuyuk1.comet.api.event.events.group.GroupJoinEvent
import ren.natsuyuk1.comet.mirai.MiraiComet
import ren.natsuyuk1.comet.mirai.contact.toGroupMember

fun MemberJoinEvent.toCometEvent(comet: MiraiComet): GroupJoinEvent {
    return when (this) {
        is MemberJoinEvent.Active -> {
            GroupJoinEvent.Normal(
                member.toGroupMember(comet)
            )
        }

        is MemberJoinEvent.Invite -> {
            GroupJoinEvent.Invite(
                member.toGroupMember(comet),
                invitor.toGroupMember(comet)
            )
        }

        is MemberJoinEvent.Retrieve -> {
            GroupJoinEvent.Retrieve(
                member.toGroupMember(comet)
            )
        }
    }
}
