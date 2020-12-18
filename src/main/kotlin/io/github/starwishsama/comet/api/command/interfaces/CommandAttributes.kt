package io.github.starwishsama.comet.api.command.interfaces

import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import net.mamoe.mirai.event.events.MessageEvent

/**
 * 阻塞命令
 *
 * 用于接收用户输入, 利用 [Session] 接收用户输入的内容
 *
 * @author Nameless
 */
interface SuspendCommand {
    /** 处理用户输入内容 */
    suspend fun handleInput(event: MessageEvent, user: BotUser, session: Session)
}

/**
 * 用于标记不可被禁用的命令
 */
interface UnDisableableCommand