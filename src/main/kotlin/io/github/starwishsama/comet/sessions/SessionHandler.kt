package io.github.starwishsama.comet.sessions

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.StringUtil.getLastingTimeAsString
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.streams.toList

object SessionHandler {
    private val sessionPool: MutableSet<Session> = Collections.synchronizedSet(mutableSetOf())

    /**
     * 插入一个会话.
     *
     * @param session 会话
     * @return 添加状态
     */
    fun insertSession(session: Session): Boolean {
        BotVariables.daemonLogger.verbose("创建会话 ${session::class.java.simpleName + "#" + session.hashCode()}")
        return sessionPool.add(session)
    }

    fun removeSession(session: Session): Boolean {
        BotVariables.daemonLogger.verbose("移除会话 ${session::class.java.simpleName + "#" + session.hashCode()}")
        return sessionPool.remove(session)
    }

    fun hasSessionByGroup(groupID: Long): Boolean = getSessionsByGroup(groupID).isNotEmpty()

    fun hasSessionByGroup(groupID: Long, cmd: Class<*>): Boolean {
        return getSessionsByGroup(groupID).stream().filter { it.creator::class.java == cmd }.count() > 0
    }

    fun getSessionsByGroup(groupID: Long): List<Session> = sessionPool.stream().filter { it.target.groupId == groupID }.toList()

    fun hasSessionByID(id: Long): Boolean = getSessionsByID(id).isNotEmpty()

    fun hasSessionByID(id: Long, cmd: Class<*>): Boolean {
        return getSessionsByID(id).stream().filter { it.creator::class.java == cmd }.count() > 0
    }

    fun getSessionsByID(id: Long): List<Session> = sessionPool.stream().filter { it.target.privateId == id }.toList()

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
    fun handleSessions(e: MessageEvent): Boolean {
        val time = LocalDateTime.now()
        val target = SessionTarget()

        target.apply {
            if (e is GroupMessageEvent) {
                groupId = e.group.id
            }

            privateId = e.sender.id
        }

        val sessionToHandle = sessionPool.stream().filter { it.target.groupId == target.groupId || it.target.privateId == target.privateId }

        sessionToHandle.forEach {
            if (it.silent || CommandExecutor.getCommandPrefix(e.message.contentToString()).isEmpty()) {
                if (it.creator is ConversationCommand) {
                    it.creator.handle(e, BotUser.getUserOrRegister(e.sender.id), it)
                } else {
                    it.handle(e, BotUser.getUserOrRegister(e.sender.id), it)
                }
            }
        }

        if (sessionPool.stream().filter { it.target.groupId == target.groupId || it.target.privateId == target.privateId }.count() > 0) {
            BotVariables.logger.debug(
                "[会话] 处理 ${sessionToHandle.count()} 个会话耗时 ${time.getLastingTimeAsString(unit = TimeUnit.SECONDS, msMode = true)}"
            )
        }

        return sessionPool.stream().filter { it.target.groupId == target.groupId || it.target.privateId == target.privateId && !it.silent }.count() > 0
    }
}