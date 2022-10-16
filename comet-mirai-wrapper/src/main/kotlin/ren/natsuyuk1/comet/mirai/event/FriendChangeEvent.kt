package ren.natsuyuk1.comet.mirai.event

import net.mamoe.mirai.event.events.FriendAddEvent
import net.mamoe.mirai.event.events.FriendDeleteEvent
import ren.natsuyuk1.comet.mirai.MiraiComet
import ren.natsuyuk1.comet.mirai.contact.toCometUser

fun FriendAddEvent.toCometEvent(comet: MiraiComet): ren.natsuyuk1.comet.api.event.events.request.FriendAddEvent =
    ren.natsuyuk1.comet.api.event.events.request.FriendAddEvent(friend.toCometUser(comet))

fun FriendDeleteEvent.toCometEvent(comet: MiraiComet): ren.natsuyuk1.comet.api.event.events.request.FriendDeleteEvent =
    ren.natsuyuk1.comet.api.event.events.request.FriendDeleteEvent(friend.toCometUser(comet))
