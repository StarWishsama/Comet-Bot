package io.github.starwishsama.comet.sessions

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.utils.TaskUtil
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 管理运行中产生的会话
 *
 * @author Nameless
 */
object SessionManager {
    /**
     * 会话列表
     */
    private val sessions: MutableMap<Session, LocalDateTime> = HashMap()

    init {
        TaskUtil.runScheduleTaskAsync(3, 3, TimeUnit.MINUTES) {
            val timeNow = LocalDateTime.now()
            sessions.forEach { (session, time) ->
                if (time.plusMinutes(3).isAfter(timeNow)) {
                    this.expireSession(session)
                }
            }
        }
    }

    fun addSession(session: Session): Session {
        return addSession(session, LocalDateTime.now())
    }

    fun addAutoCloseSession(session: Session, closeAfterMinute: Int) {
        addSession(session)
        TaskUtil.runAsync(closeAfterMinute.toLong(), TimeUnit.MINUTES) {
            daemonLogger.info("自动关闭会话 ${session::class.java.simpleName + "#" + session.hashCode()} 中")
            session.beforeExpiredAction
            sessions.remove(session)
        }
    }

    private fun addSession(session: Session, time: LocalDateTime): Session {
        sessions[session] = time
        return session
    }

    fun expireSession(session: Session) {
        sessions.remove(session)
    }

    @Suppress("unused")
    fun expireSession(id: Long): Boolean {
        if (isValidSessionById(id)) {
            sessions.remove(getSession(id))
            return true
        }
        return false
    }

    fun isValidSessionById(id: Long): Boolean {
        return getSession(id) != null || isValidSessionByGroup(id)
    }

    private fun isValidSessionByGroup(groupId: Long): Boolean {
        return getSessionByGroup(groupId) != null
    }

    private fun getSession(id: Long): Session? {
        if (sessions.isNotEmpty()) {
            for (session in sessions) {
                if (session.key.getUserByID(id) != null) {
                    return session.key
                }
            }
        }
        return null
    }

    fun getSessionByGroup(id: Long, type: Class<out Session>? = null): Session? {
        for (session in sessions) {
            if (session.key.groupId == id && (session::class == type || type == null)) {
                return session.key
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
                    getSession(event.sender.id)
                }
            }
            else -> getSession(event.sender.id)
        }
    }

    fun getSessions(): Map<Session, LocalDateTime> {
        return sessions
    }
}