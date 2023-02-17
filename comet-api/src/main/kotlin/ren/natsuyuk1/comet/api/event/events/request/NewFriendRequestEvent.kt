package ren.natsuyuk1.comet.api.event.events.request

import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.event.CometEvent
import ren.natsuyuk1.comet.api.user.Group

abstract class NewFriendRequestEvent(
    override val comet: Comet,
    val eventId: Long,
    val message: String,
    val fromId: Long,
    val fromGroupId: Long,
    val fromNick: String,
) : CometEvent(comet) {
    suspend fun getFromGroup(): Group? = comet.getGroup(fromGroupId)

    abstract suspend fun accept()

    abstract suspend fun reject(block: Boolean = false)
}
