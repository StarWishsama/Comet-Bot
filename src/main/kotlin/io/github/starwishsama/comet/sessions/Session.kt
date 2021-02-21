package io.github.starwishsama.comet.sessions

import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import net.mamoe.mirai.contact.Member
import java.time.LocalDateTime
import java.util.*


/**
 * @author Nameless
 */
open class Session(open var groupId: Long = 0, var command: ChatCommand, val beforeExpiredAction: (Session) -> Unit = {}, val startTime: LocalDateTime = LocalDateTime.now()) {
    val users: MutableList<SessionUser> = LinkedList()

    constructor(command: ChatCommand, id: Long) : this(-1, command) {
        addUser(id)
    }

    override fun toString(): String {
        return "Session#${hashCode()} {groupId=$groupId, command=${command.getProps().name}, usersCount=${users.size}}"
    }

    fun addUser(id: Long, name: String = "未知", member: Member? = null) {
        users.add(SessionUser(id, name, member))
    }

    fun getUserByID(id: Long): SessionUser? {
        val result = users.stream().filter { it.userId == id }.findAny()

        return if (result.isPresent) {
            result.get()
        } else {
            null
        }
    }
}