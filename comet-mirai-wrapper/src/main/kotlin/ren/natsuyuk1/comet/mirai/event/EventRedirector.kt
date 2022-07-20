package ren.natsuyuk1.comet.mirai.event

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import ren.natsuyuk1.comet.api.event.EventManager
import ren.natsuyuk1.comet.mirai.MiraiComet

suspend fun Event.redirectToComet(comet: MiraiComet) {
    when (this) {
        is GroupMessageEvent -> {
            EventManager.broadcastEvent(this.toCometEvent(comet))
        }
        is FriendMessageEvent -> {
            EventManager.broadcastEvent(this.toCometEvent(comet))
        }
        is GroupTempMessageEvent -> {
            EventManager.broadcastEvent(this.toCometEvent(comet))
        }
    }
}
