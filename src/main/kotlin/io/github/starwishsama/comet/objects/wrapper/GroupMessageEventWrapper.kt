package io.github.starwishsama.comet.objects.wrapper

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source

class GroupMessageEventWrapper(override val senderName: String,
                               /**
                                * 发送方权限.
                                */
                               val permission: MemberPermission,
                               override val sender: Member,
                               override val message: MessageChain,
                               override val time: Int
) : MessageEventWrapper() {
    init {
        val source = message[MessageSource] ?: error("Cannot find MessageSource from message")
        check(source is OnlineMessageSource.Incoming.FromGroup) { "source provided to a GroupMessage must be an instance of OnlineMessageSource.Incoming.FromGroup" }
    }

    val group: Group get() = sender.group

    override val bot: Bot get() = sender.bot

    override val subject: Group get() = group

    override val source: OnlineMessageSource.Incoming.FromGroup get() = message.source as OnlineMessageSource.Incoming.FromGroup

    override fun toString(): String =
            "GroupMessageEvent(group=${group.id}, senderName=$senderName, sender=${sender.id}, permission=${permission.name}, message=$message)"
}