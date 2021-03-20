package io.github.starwishsama.comet.sessions

import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.objects.BotUser
import net.mamoe.mirai.event.events.MessageEvent
import java.time.LocalDateTime

open class Session(
    open val target: SessionTarget,
    val creator: ChatCommand,
    open val silent: Boolean = false,
) {
    val users: MutableSet<SessionUser> = mutableSetOf()
    val createdTime: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "Session#${hashCode()} {target=$target, silent=${silent}, usersCount=${users.size}}"
    }

    open fun handle(event: MessageEvent, user: BotUser, session: Session) {
    }
}