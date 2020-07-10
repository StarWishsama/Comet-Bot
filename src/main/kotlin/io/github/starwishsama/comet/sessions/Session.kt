package io.github.starwishsama.comet.sessions

import io.github.starwishsama.comet.commands.interfaces.UniversalCommand
import java.util.*


/**
 * @author Nameless
 */
open class Session(open var groupId: Long = 0, var command: UniversalCommand) {
    var users: List<SessionUser> = LinkedList<SessionUser>()

    constructor(command: UniversalCommand, id: Long) : this(0, command) {
        putUser(id)
    }

    private fun putUser(id: Long) {
        users = users + SessionUser(id)
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