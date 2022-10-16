package ren.natsuyuk1.comet.mirai.event

import ren.natsuyuk1.comet.api.event.events.request.NewFriendRequestEvent
import ren.natsuyuk1.comet.mirai.MiraiComet

fun net.mamoe.mirai.event.events.NewFriendRequestEvent.toCometEvent(comet: MiraiComet): NewFriendRequestEvent =
    NewFriendRequestEventImpl(this, comet)

class NewFriendRequestEventImpl(
    private val origin: net.mamoe.mirai.event.events.NewFriendRequestEvent,
    comet: MiraiComet
) : NewFriendRequestEvent(
    comet,
    eventId = origin.eventId,
    message = origin.message,
    fromId = origin.fromId,
    fromGroupId = origin.fromGroupId,
    fromNick = origin.fromNick
) {
    override suspend fun accept() {
        origin.accept()
    }

    override suspend fun reject(block: Boolean) {
        origin.reject(block)
    }
}
