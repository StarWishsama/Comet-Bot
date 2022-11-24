package ren.natsuyuk1.comet.mirai.event

import net.mamoe.mirai.event.events.BotLeaveEvent
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberLeaveEvent
import net.mamoe.mirai.utils.MiraiExperimentalApi
import ren.natsuyuk1.comet.api.event.events.group.GroupJoinEvent
import ren.natsuyuk1.comet.api.event.events.group.GroupLeaveEvent
import ren.natsuyuk1.comet.mirai.MiraiComet
import ren.natsuyuk1.comet.mirai.contact.toCometGroup
import ren.natsuyuk1.comet.mirai.contact.toCometUser
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

@OptIn(MiraiExperimentalApi::class)
fun BotLeaveEvent.toCometEvent(comet: MiraiComet): GroupLeaveEvent {
    val user = bot.asFriend.toCometUser(comet)
    return when (this) {
        is BotLeaveEvent.Active -> {
            GroupLeaveEvent(
                comet,
                group.toCometGroup(comet),
                user
            )
        }

        is BotLeaveEvent.Disband -> {
            GroupLeaveEvent(
                comet,
                group.toCometGroup(comet),
                user
            )
        }

        is BotLeaveEvent.Kick -> {
            GroupLeaveEvent(
                comet,
                group.toCometGroup(comet),
                user
            )
        }
    }
}

fun MemberLeaveEvent.toCometEvent(comet: MiraiComet): GroupLeaveEvent =
    GroupLeaveEvent(
        comet,
        group.toCometGroup(comet),
        member.toCometUser(comet)
    )
