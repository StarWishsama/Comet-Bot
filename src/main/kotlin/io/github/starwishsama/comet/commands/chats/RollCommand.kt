package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.sessions.commands.roll.RollSession
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

@CometCommand
class RollCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
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

            return toChain("""
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
}