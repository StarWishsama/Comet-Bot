package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.SuspendCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.sessions.SessionUser
import io.github.starwishsama.comet.sessions.commands.roll.RollSession
import io.github.starwishsama.comet.utils.CometUtil.sendMessage
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.at
import net.mamoe.mirai.message.data.messageChainOf
import java.time.LocalDateTime

@CometCommand
class RollCommand : ChatCommand, SuspendCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (event !is GroupMessageEvent) return "本命令仅限群聊使用".sendMessage()

        val session = SessionManager.getSessionByGroup(event.group.id, RollSession::class.java)
        if (session.exists() && session.hasType(RollSession::class.java)) {
            return "该群已经有一个正在进行中的抽奖了!".sendMessage()
        }

        if (args.size < 2) {
            return getHelp().convertToChain()
        } else {
            val rollThing = args[0]
            val rollThingCount = args[1].toIntOrNull() ?: -1
            val rollKeyWord = args.getOrNull(2)
            val rollDelay = args.getOrNull(3)?.toIntOrNull()

            if (rollThingCount == -1 || (args.getOrNull(3) != null && rollDelay == null)) {
                return "请输入有效的物品数量!".sendMessage()
            }

            if (rollDelay != null && rollDelay !in 1..15) {
                return "开奖时间设置错误! 范围为 (0, 15] 分钟".sendMessage()
            }

            val rollSession = RollSession(
                    groupId = event.group.id,
                    rollStarter = event.sender,
                    rollItem = rollThing,
                    keyWord = rollKeyWord ?: "",
                    stopAfterMinute = rollDelay ?: 3,
                    count = rollThingCount
            )

            SessionManager.addSession(rollSession)

            return sendMessage("""
                ${event.senderName} 发起了一个抽奖!
                抽奖物品: ${rollSession.rollItem}
                抽奖人数: ${rollSession.count}
                参与方式: ${if (rollSession.keyWord.isEmpty()) "在群内发送任意消息" else "发送 ${rollSession.keyWord}"}
                将会在 ${rollSession.stopAfterMinute} 分钟后开奖
            """.trimIndent())
        }
    }

    @Suppress("SpellCheckingInspection")
    override fun getProps(): CommandProps = CommandProps(
        "roll",
        arrayListOf("rl", "抽奖"),
        "roll东西",
        "nbot.commands.roll",
        UserLevel.USER
    )

    override fun getHelp(): String = """
        /roll [需roll的东西] [roll的个数] <roll关键词> <几分钟后开始roll>
        关键词不填则在群内发言的自动加入抽奖池,
        抽奖延迟时间不填则自动设为 3 分钟.
    """.trimIndent()

    override fun handleInput(event: MessageEvent, user: BotUser, session: Session) {
        if (session is RollSession && event is GroupMessageEvent) {
            if (LocalDateTime.now().minusMinutes(session.stopAfterMinute.toLong()).isBefore(session.startTime)) {
                generateResult(session, event)
                SessionManager.expireSession(session)
                return
            }

            if (session.rollStarter.id == event.sender.id) return

            if (session.keyWord.isEmpty()) {
                session.addUser(event.sender.id, event.sender.nameCardOrNick, event.sender)
                return
            }

            val message = event.message.contentToString()

            if (session.keyWord == message) {
                session.addUser(event.sender.id, event.sender.nameCardOrNick, event.sender)
            }
        }
    }

    private fun generateResult(session: RollSession, event: GroupMessageEvent) {
        val group = event.bot.getGroup(session.groupId)

        if (group == null) {
            daemonLogger.warning("推送开奖消息失败: 找不到对应的群[${group}]")
        } else {
            var winner = messageChainOf()
            session.getWinningUsers().forEach { su ->
                if (su.member != null) winner = winner.plus(su.member.at())
            }
            GlobalScope.launch {
                group.sendMessage(
                    sendMessage(
                        "由${(session.rollStarter as Member).nameCardOrNick}发起的抽奖开奖了!\n" +
                                "奖品: ${session.rollItem}\n" +
                                "中奖者: "
                    ) + winner
                )
            }
        }
    }

    private fun RollSession.getWinningUsers(): List<SessionUser> {
        val winners = mutableListOf<SessionUser>()

        for (i in 0 until count) {
            winners.add(getRandomUser())
        }

        return winners
    }
}