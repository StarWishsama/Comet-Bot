package io.github.starwishsama.nbot.sessions

import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.SessionType
import java.util.*


/**
 * @author Nameless
 */
open class Session(var groupId: Long = 0, var type: SessionType, var command: UniversalCommand) {
    private var users: List<SessionUser> = LinkedList<SessionUser>()

    constructor(type: SessionType, command: UniversalCommand, id: Long) : this(0, type, command) {
        putUser(id)
    }

    fun putUser(id: Long) {
        users = users.plusElement(SessionUser(id))
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