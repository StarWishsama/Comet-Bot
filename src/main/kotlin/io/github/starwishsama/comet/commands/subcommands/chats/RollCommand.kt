package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.commands.interfaces.SuspendCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.sessions.SessionUser
import io.github.starwishsama.comet.sessions.commands.roll.RollSession
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.TaskUtil
import io.github.starwishsama.comet.utils.convertToChain
import io.github.starwishsama.comet.utils.limitStringSize
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.getGroupOrNull
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.messageChainOf
import java.util.concurrent.TimeUnit

class RollCommand : ChatCommand, SuspendCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (!BotUtil.isNoCoolDown(user.id)) return EmptyMessageChain
        if (event !is GroupMessageEvent) return BotUtil.sendMessage("本命令仅限群聊使用")

        val session = SessionManager.getSessionByGroup(event.group.id)
        if (session != null && session is RollSession) return BotUtil.sendMessage("该群已经有一个正在进行中的抽奖了!")
        if (args.size < 2) {
            return getHelp().convertToChain()
        } else {
            val rollThing = args[0]
            val rollThingCount = args[1].toIntOrNull() ?: -1
            val rollKeyWord = args.getOrNull(2)
            val rollDelay = args.getOrNull(3)?.toIntOrNull()

            if (rollThingCount == -1 || (args.getOrNull(3) != null && rollDelay == null)) return BotUtil.sendMessage("请输入有效的数量!")
            if (rollDelay != null && rollDelay >= 900) return BotUtil.sendMessage("开奖时间不得大于等于15分钟")

            val sessionToAdd = RollSession(
                    groupId = event.group.id,
                    rollStarter = event.sender,
                    rollItem = rollThing,
                    keyWord = rollKeyWord ?: "",
                    stopAfterMinute = rollDelay ?: 5,
                    count = rollThingCount
            )

            SessionManager.addSession(sessionToAdd)
            TaskUtil.runAsync({
                SessionManager.expireSession(sessionToAdd)
                val group = bot.getGroupOrNull(sessionToAdd.groupId) ?: return@runAsync
                var winner = messageChainOf()
                sessionToAdd.getWinningUsers().forEach {
                    if (it.member != null) winner = winner.plus(At(it.member))
                }
                runBlocking {
                    group.sendMessage(
                            BotUtil.sendMessage(
                                    "由${(sessionToAdd.rollStarter as Member).nameCardOrNick.limitStringSize(10)}发起的抽奖开奖了!\n" +
                                            "奖品: ${sessionToAdd.rollItem}\n" +
                                            "中奖者: " + winner)
                    )
                }
            }, sessionToAdd.stopAfterMinute.toLong(), TimeUnit.MINUTES)

            return BotUtil.sendMessage("""
                ${event.senderName} 发起了一个抽奖!
                抽奖物品: $rollThing
                抽奖人数: $rollThingCount
                参与方式: ${if (rollKeyWord == null) "在群内发送任意消息" else "发送 $rollKeyWord"}
                将会在 ${rollDelay ?: 5} 分钟后开奖
            """.trimIndent())
        }
    }

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
        抽奖延迟时间不填则自动设为 5 分钟.
    """.trimIndent()

    override suspend fun handleInput(event: MessageEvent, user: BotUser, session: Session) {
        if (session is RollSession && event is GroupMessageEvent) {
            if (session.rollStarter.id == event.sender.id) return

            if (session.keyWord.isEmpty()) {
                session.putUser(event.sender.id, event.sender.nameCardOrNick, event.sender)
                return
            }

            val message = event.message.contentToString()

            if (session.keyWord == message) {
                session.putUser(event.sender.id, event.sender.nameCardOrNick, event.sender)
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