package io.github.starwishsama.comet.api.command.interfaces

import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import net.mamoe.mirai.event.events.MessageEvent

/**
 * 交互式命令
 *
 * 支持接受输入内容并处理.
 */
interface ConversationCommand {
    fun handle(event: MessageEvent, user: BotUser, session: Session)
}