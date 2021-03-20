package io.github.starwishsama.comet.sessions

import io.github.starwishsama.comet.objects.BotUser
import net.mamoe.mirai.event.events.MessageEvent
import java.time.LocalDateTime

open class Session(
    open val target: SessionTarget,
    open val creator: Class<*>,
    open val silent: Boolean = false,
    open val handle: (MessageEvent, BotUser, Session) -> Unit = { me: MessageEvent, bu: BotUser, session: Session -> }
) {
    val users: MutableSet<SessionUser> = mutableSetOf()
    val createdTime: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "Session#${hashCode()} {target=$target, silent=${silent}, usersCount=${users.size}}"
    }
}