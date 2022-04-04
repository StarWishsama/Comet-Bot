/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.sessions

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.command.CommandManager
import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

object SessionHandler {
    private val sessionPool: MutableSet<Session> = Collections.synchronizedSet(mutableSetOf())

    /**
     * 插入一个会话.
     *
     * @param session 会话
     * @return 添加状态
     */
    fun insertSession(session: Session): Boolean {
        CometVariables.daemonLogger.verbose("创建会话 ${session::class.java.simpleName + "#" + session.hashCode()}")
        return sessionPool.add(session)
    }

    fun removeSession(session: Session): Boolean {
        CometVariables.daemonLogger.verbose("移除会话 ${session::class.java.simpleName + "#" + session.hashCode()}")
        return sessionPool.remove(session)
    }

    fun hasSessionByGroup(groupID: Long): Boolean = getSessionsByGroup(groupID).isNotEmpty()

    fun hasSessionByGroup(groupID: Long, cmd: Class<*>): Boolean {
        return getSessionsByGroup(groupID).any { it.creator::class.java == cmd }
    }

    private fun getSessionsByGroup(groupID: Long): List<Session> =
        sessionPool.filter { it.target.groupId == groupID }

    fun hasSessionByID(id: Long): Boolean = getSessionsByID(id).isNotEmpty()

    fun hasSessionByID(id: Long, cmd: Class<*>): Boolean {
        return getSessionsByID(id).any { it.creator::class.java == cmd }
    }

    fun getSessionsByID(id: Long): List<Session> =
        sessionPool.filter { it.target.targetId == id }

    /**
     * 获取所有活跃中的会话列表副本.
     *
     *
     * @return 会话列表副本
     */
    fun getSessions(): MutableSet<Session> = Collections.unmodifiableSet(sessionPool)

    /**
     * 传入消息处理会话
     *
     * @param e 消息事件
     */
    suspend fun handleSessions(e: MessageEvent, user: CometUser): Boolean {
        val time = LocalDateTime.now()

        val groupId = if (e is GroupMessageEvent) e.group.id else 0L
        val senderId = e.sender.id

        val sessions = sessionPool.filter { it.target.isTargetFor(groupId, senderId) }

        if (sessions.isEmpty()) {
            return false
        }

        /** Handle [ConversationCommand] */
        for (session in sessions) {
            if (session.silent || CommandManager.getCommandPrefix(e.message.contentToString()).isEmpty()) {
                session.creator.handle(e, user, session)
                session.update()
            }
        }

        CometVariables.logger.debug(
            "[会话] 处理 ${sessions.size} 个会话耗时 ${
                time.getLastingTimeAsString(
                    unit = TimeUnit.SECONDS,
                    msMode = true
                )
            }"
        )

        return sessions.any { !it.silent }
    }
}