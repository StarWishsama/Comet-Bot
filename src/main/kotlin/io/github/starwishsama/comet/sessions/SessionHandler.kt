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
import java.util.stream.Collectors

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
        return getSessionsByGroup(groupID).stream().filter { it.creator::class.java == cmd }.count() > 0
    }

    fun getSessionsByGroup(groupID: Long): List<Session> =
        sessionPool.stream().filter { it.target.groupId == groupID }.collect(Collectors.toList())

    fun hasSessionByID(id: Long): Boolean = getSessionsByID(id).isNotEmpty()

    fun hasSessionByID(id: Long, cmd: Class<*>): Boolean {
        return getSessionsByID(id).stream().filter { it.creator::class.java == cmd }.count() > 0
    }

    fun getSessionsByID(id: Long): List<Session> =
        sessionPool.stream().filter { it.target.privateId == id }.collect(Collectors.toList())

    /**
     * 获取所有活跃中的会话列表副本.
     *
     *
     * @return 会话列表副本
     */
    fun getSessions(): MutableSet<Session> = sessionPool.toMutableSet()

    /**
     * 传入消息处理会话
     *
     * @param e 消息事件
     */
    suspend fun handleSessions(e: MessageEvent, u: CometUser): Boolean {
        val time = LocalDateTime.now()
        val target = SessionTarget()

        target.apply {
            if (e is GroupMessageEvent) {
                groupId = e.group.id
            }

            privateId = e.sender.id
        }

        val sessionStream = sessionPool.stream()
            .filter { it.target.groupId == target.groupId || it.target.privateId == target.privateId }

        val sessionToHandle = sessionStream.collect(Collectors.toList())

        if (sessionToHandle.isEmpty()) {
            return false
        }

        for (session in sessionToHandle) {
            if (session.silent || CommandManager.getCommandPrefix(e.message.contentToString()).isEmpty()) {
                if (session.creator is ConversationCommand) {
                    session.creator.handle(e, u, session)
                }
            }
        }

        if (sessionPool.stream()
                .filter { it.target.groupId == target.groupId || it.target.privateId == target.privateId }.count() > 0
        ) {
            CometVariables.logger.debug(
                "[会话] 处理 ${sessionToHandle.count()} 个会话耗时 ${
                    time.getLastingTimeAsString(
                        unit = TimeUnit.SECONDS,
                        msMode = true
                    )
                }"
            )
        }

        return sessionPool.stream()
            .filter { (it.target.groupId == target.groupId || it.target.privateId == target.privateId) && !it.silent }
            .count() > 0
    }
}