package ren.natsuyuk1.comet.mirai.event

import net.mamoe.mirai.event.events.GroupMessageEvent

fun GroupMessageEvent.toCometEvent(): ren.natsuyuk1.comet.api.event.impl.message.GroupMessageEvent {
    return ren.natsuyuk1.comet.api.event.impl.message.GroupMessageEvent()
}
