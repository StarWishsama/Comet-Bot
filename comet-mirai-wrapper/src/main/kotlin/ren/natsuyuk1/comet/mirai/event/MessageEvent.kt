package ren.natsuyuk1.comet.mirai.event

import net.mamoe.mirai.event.events.GroupMessageEvent
import ren.natsuyuk1.comet.mirai.MiraiComet
import ren.natsuyuk1.comet.mirai.contact.toCometGroup
import ren.natsuyuk1.comet.mirai.contact.toGroupMember
import ren.natsuyuk1.comet.mirai.util.toMessageWrapper

fun GroupMessageEvent.toCometEvent(comet: MiraiComet): ren.natsuyuk1.comet.api.event.impl.message.GroupMessageEvent {
    return ren.natsuyuk1.comet.api.event.impl.message.GroupMessageEvent(
        comet = comet,
        subject = this.subject.toCometGroup(comet),
        sender = this.sender.toGroupMember(comet),
        senderName = this.senderName,
        message = this.message.toMessageWrapper(false),
        time = this.time
    )
}
