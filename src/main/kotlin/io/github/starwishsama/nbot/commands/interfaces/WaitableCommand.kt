package io.github.starwishsama.nbot.commands.interfaces

import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.sessions.Session
import net.mamoe.mirai.message.ContactMessage

interface WaitableCommand {
    suspend fun replyResult(message: ContactMessage, user: BotUser, session: Session)
}