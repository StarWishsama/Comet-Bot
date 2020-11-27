package io.github.starwishsama.comet.sessions

import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import net.mamoe.mirai.contact.Member
import java.util.*


/**
 * @author Nameless
 */
open class Session(open var groupId: Long = 0, var command: ChatCommand, val beforeExpired: Session.() -> Unit = {}) {
    val users: MutableList<SessionUser> = LinkedList()

    constructor(command: ChatCommand, id: Long) : this(0, command) {
        putUser(id)
    }

    override fun toString(): String {
        return "Session#${hashCode()} {groupId=$groupId, command=${command.getProps().name}, users=${users}}"
    }

    fun putUser(id: Long, name: String = "未知", member: Member? = null) {
        users.add(SessionUser(id, name, member))
    }

    fun getUserById(id: Long): SessionUser? {
        if (users.isNotEmpty()) {
            for (user in users) {
                if (user.userId == id) {
                    return user
                }
            }
        }
        return null
    }
}