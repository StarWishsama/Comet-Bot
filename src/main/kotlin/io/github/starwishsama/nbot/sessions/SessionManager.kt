package io.github.starwishsama.nbot.sessions

import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import java.util.*


/**
 * 管理运行中产生的会话
 *
 * @author Nameless
 */
object SessionManager {
    /**
     * 会话列表
     */
    private val sessions: MutableList<Session> = LinkedList()

    fun addSession(session: Session) {
        sessions.add(session)
    }

    fun expireSession(session: Session) {
        sessions.remove(session)
    }

    fun expireSession(id: Long): Boolean {
        if (isValidSessionById(id)) {
            sessions.remove(getSession(id))
            return true
        }
        return false
    }

    fun isValidSessionById(id: Long): Boolean {
        return getSession(id) != null
    }

    private fun isValidSessionByGroup(groupId: Long): Boolean {
        return getSessionByGroup(groupId) != null
    }

    private fun getSession(id: Long): Session? {
        if (sessions.isNotEmpty()) {
            for (session in sessions) {
                if (session.getUserById(id) != null) {
                    return session
                }
            }
        }
        return null
    }

    fun getSessionByGroup(id: Long): Session? {
        for (session in sessions) {
            if (session.groupId == id) {
                return session
            }
        }
        return null
    }

    fun getSessionByEvent(event: MessageEvent): Session? {
        return when (event) {
            is GroupMessageEvent -> {
                if (isValidSessionByGroup(event.group.id)) {
                    getSessionByGroup(event.group.id)
                } else {
                    null
                }
            }
            else -> getSession(event.sender.id)
        }
    }

    fun getSessions(): List<Session> {
        return sessions
    }
}