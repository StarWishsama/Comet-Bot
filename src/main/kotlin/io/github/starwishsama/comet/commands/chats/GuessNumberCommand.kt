package io.github.starwishsama.comet.commands.chats

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.sessions.commands.guessnumber.GuessNumberSession
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

@CometCommand
class GuessNumberCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (event is GroupMessageEvent) {
            if (SessionHandler.hasSessionByGroup(event.group.id, this::class.java)) {
                when {
                    args.isEmpty() -> {
                        val answer = RandomUtil.randomInt(0, 100)
                        BotVariables.logger.info("[猜数字] 群 ${event.group.id} 生成的随机数为 $answer")
                        SessionHandler.insertSession(GuessNumberSession(SessionTarget(event.group.id), RandomUtil.randomInt(0, 101)))
                        return CometUtil.toChain("来猜个数字吧! 范围 [0, 100]")
                    }
                    args.size == 2 -> {
                        val min = args[0].toInt()
                        val max = args[1].toInt()
                        if (min <= 0 || max <= 0) {
                            return CometUtil.toChain("不支持负数")
                        }

                        if (min >= max) {
                            return CometUtil.toChain("最小值不能大于等于最大值")
                        }
                        val answer = RandomUtil.randomInt(min, max + 1)
                        BotVariables.logger.info("[猜数字] 群 ${event.group.id} 生成的随机数为 $answer")
                        SessionHandler.insertSession(GuessNumberSession(SessionTarget(event.group.id), answer))
                        return CometUtil.toChain("猜一个数字吧! 范围 [$min, $max]")
                    }
                    else -> {
                        return getHelp().convertToChain()
                    }
                }
            } else {
                return CometUtil.toChain("已经有一个游戏在进行中啦~")
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("guessnumber", arrayListOf("csz"), "猜数字", "nbot.commands.guessnumber", UserLevel.USER)

    override fun getHelp(): String = """
        /csz 猜数字
        /csz [最小值] [最大值] 猜指定范围内的数字
    """.trimIndent()
}