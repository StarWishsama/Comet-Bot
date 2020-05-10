package io.github.starwishsama.nbot.commands.interfaces

import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.sessions.Session
import net.mamoe.mirai.message.MessageEvent

interface WaitableCommand {
    suspend fun replyResult(event: MessageEvent, user: BotUser, session: Session)
}