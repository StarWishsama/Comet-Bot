package io.github.starwishsama.comet.sessions

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.utils.TaskUtil
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
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
    private val sessions: MutableList<Session> = mutableListOf()

    fun addSession(session: Session): Session {
        sessions.add(session)
        return session
    }

    fun addAutoCloseSession(session: Session, closeAfterMinute: Int) {
        addSession(session)
        TaskUtil.runAsync(closeAfterMinute.toLong(), TimeUnit.MINUTES) {
            session.beforeExpiredAction(session)
            daemonLogger.info("自动关闭会话 ${session::class.java.simpleName + "#" + session.hashCode()}, 结果: ${sessions.remove(session)}")
        }
    }

    fun expireSession(session: Session) {
        daemonLogger.info("关闭会话 ${session::class.java.simpleName + "#" + session.hashCode()}, 结果: ${sessions.remove(session)}")
    }

    @Suppress("unused")
    fun expireSession(id: Long): Boolean {
        if (isValidSessionById(id)) {
            getSession(id).sessionList.forEach {
                sessions.remove(it)
            }
            return true
        }
        return false
    }

    fun isValidSessionById(id: Long): Boolean {
        return getSession(id).exists() || isValidSessionByGroup(id)
    }

    private fun isValidSessionByGroup(groupId: Long): Boolean {
        return getSessionByGroup(groupId).exists()
    }

    private fun getSession(id: Long): SessionGetResult {
        val result = SessionGetResult()

        sessions.forEach { session ->
            if (session.getUserByID(id) != null) {
                result.addSession(session)
            }
        }

        return result
    }

    /**
     * 通过群号获取会话
     *
     * FIXME: 未考虑多个会话情况
     *
     * @param id 群ID
     * @param type 会话类型
     *
     * @return 会话, 若无则为 null
     */
    fun getSessionByGroup(id: Long, type: Class<out Session>? = null): SessionGetResult {
        val result = SessionGetResult()

        for (session in sessions) {
            // 未指定类型时也会返回
            if (session.groupId == id && (session::class == type || type == null)) {
                result.addSession(session)
            }
        }

        return result
    }

    fun getSessionByEvent(event: MessageEvent): SessionGetResult {
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

    fun getSessions(): MutableList<Session> {
        return sessions
    }
}

data class SessionGetResult(val sessionList: MutableList<Session> = mutableListOf()) {
    fun addSession(session: Session) {
        sessionList.add(session)
    }

    fun exists(): Boolean {
        return sessionList.size > 0
    }

    fun hasType(type: Class<out Session>): Boolean {
        sessionList.forEach { session ->
            if (session::class.java == type::class.java) {
                return true
            }
        }

        return false
    }
}