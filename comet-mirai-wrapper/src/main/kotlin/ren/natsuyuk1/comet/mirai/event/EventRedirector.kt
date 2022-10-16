package ren.natsuyuk1.comet.mirai.event

import mu.KotlinLogging
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.*
import ren.natsuyuk1.comet.api.event.EventManager
import ren.natsuyuk1.comet.mirai.MiraiComet

private val logger = KotlinLogging.logger {}

suspend fun Event.redirectToComet(comet: MiraiComet) {
    logger.debug { "正在尝试转换 Mirai 事件 $this" }

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

        is MemberJoinEvent -> {
            EventManager.broadcastEvent(this.toCometEvent(comet))
        }

        is NewFriendRequestEvent -> {
            EventManager.broadcastEvent(this.toCometEvent(comet))
        }

        is FriendAddEvent -> {
            EventManager.broadcastEvent(this.toCometEvent(comet))
        }

        is FriendDeleteEvent -> {
            EventManager.broadcastEvent(this.toCometEvent(comet))
        }
    }
}
