/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.sessions.SessionUser
import io.github.starwishsama.comet.sessions.commands.roll.RollSession
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.TaskUtil
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.messageChainOf
import java.util.concurrent.TimeUnit


object RollCommand : ChatCommand, ConversationCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (event !is GroupMessageEvent) return "本命令仅限群聊使用".toChain()

        if (SessionHandler.hasSessionByGroup(event.group.id, RollSession::class.java)) {
            return "该群已经有一个正在进行中的抽奖了!".toChain()
        }

        if (args.size < 2) {
            return getHelp().convertToChain()
        } else {
            val rollThing = args[0]
            val rollThingCount = args[1].toIntOrNull() ?: -1
            val rollKeyWord = args.getOrNull(2)
            val rollDelay = args.getOrNull(3)?.toIntOrNull()

            if (rollThingCount == -1 || (args.getOrNull(3) != null && rollDelay == null)) {
                return "请输入有效的物品数量!".toChain()
            }

            if (rollDelay != null && rollDelay !in 1..15) {
                return "开奖时间设置错误! 范围为 (0, 15] 分钟".toChain()
            }

            val rollSession = RollSession(
                target = SessionTarget(event.group.id),
                rollStarter = event.sender.id,
                rollItem = rollThing,
                keyWord = rollKeyWord ?: "",
                stopAfterMinute = rollDelay ?: 3,
                count = rollThingCount
            )

            SessionHandler.insertSession(rollSession)
            TaskUtil.schedule(rollSession.stopAfterMinute.toLong(), TimeUnit.MINUTES) {
                runBlocking {
                    generateResult(rollSession, event)
                }
                SessionHandler.removeSession(rollSession)
            }

            return toChain(
                """
                ${event.senderName} 发起了一个抽奖!
                抽奖物品: ${rollSession.rollItem}
                抽奖人数: ${rollSession.count}
                参与方式: ${if (rollSession.keyWord.isEmpty()) "在群内发送任意消息" else "发送 ${rollSession.keyWord}"}
                将会在 ${rollSession.stopAfterMinute} 分钟后开奖
            """.trimIndent()
            )
        }
    }

    @Suppress("SpellCheckingInspection")
    override val props: CommandProps = CommandProps(
        "roll",
        arrayListOf("rl", "抽奖"),
        "roll东西",

        UserLevel.USER
    )

    override fun getHelp(): String = """
        /roll [需roll的东西] [roll的个数] <roll关键词> <几分钟后开始roll>
        关键词不填则在群内发言的自动加入抽奖池,
        抽奖延迟时间不填则自动设为 3 分钟.
    """.trimIndent()

    override suspend fun handle(event: MessageEvent, user: CometUser, session: Session) {
        if (session is RollSession && event is GroupMessageEvent) {
            if (session.rollStarter == event.sender.id) {
                return
            }

            if (session.keyWord.isEmpty()) {
                session.users.add(SessionUser(event.sender.id))
                return
            }

            val message = event.message.contentToString()

            if (session.keyWord == message) {
                session.users.add(SessionUser(event.sender.id))
            }
        }
    }

    private suspend fun generateResult(session: RollSession, event: GroupMessageEvent) {
        val group = event.bot.getGroup(session.target.groupId)

        if (group == null) {
            CometVariables.daemonLogger.warning("推送开奖消息失败: 找不到对应的群[${group}]")
        } else {
            var winnerText = messageChainOf()
            val winners = session.getWinningUsers()

            if (winners.isEmpty()) {
                group.sendMessage(toChain("没有人参加抽奖, 本次抽奖已结束..."))
                return
            }

            winners.forEach { su ->
                winnerText = winnerText.plus(At(su.id)).plus(" ")
            }

            group.sendMessage(
                toChain(
                    "由${group[session.rollStarter]?.nameCardOrNick}发起的抽奖开奖了!\n" +
                            "奖品: ${session.rollItem}\n" +
                            "中奖者: "
                ) + winnerText
            )
        }
    }

    private fun RollSession.getWinningUsers(): List<SessionUser> {
        if (users.isEmpty()) {
            return emptyList()
        }

        val winners = mutableListOf<SessionUser>()

        for (i in 0 until count) {
            winners.add(users.random().also { users.remove(it) })
        }

        return winners
    }
}