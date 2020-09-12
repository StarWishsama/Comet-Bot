package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.ArkNightOperator
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.DrawUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.upload
import org.apache.commons.lang3.StringUtils
import java.awt.image.BufferedImage


class ArkCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        val overTimeMessage = "今日命令条数已达上限, 请等待条数自动恢复哦~\n" +
                "命令条数现在每小时会恢复100次, 封顶1000次"
        if (BotUtil.hasNoCoolDown(event.sender.id)) {
            if (args.isNotEmpty()) {
                when (args[0]) {
                    "单次寻访" -> {
                        return if (BotVariables.cfg.arkDrawUseImage) {
                            event.reply("请稍等...")
                            val list = DrawUtil.getArkDrawResultToImage(user, 1)
                            if (list.isNotEmpty()) {
                                val image: BufferedImage = DrawUtil.combineArkOpImage(list)
                                val result = image.upload(event.subject)
                                result.asMessageChain()
                            } else {
                                overTimeMessage.convertToChain()
                            }
                        } else {
                            DrawUtil.getArkDrawResult(user, 1).convertToChain()
                        }
                    }
                    "十连寻访" -> {
                        return if (BotVariables.cfg.arkDrawUseImage) {
                            event.reply("请稍等...")
                            val list: List<ArkNightOperator> = DrawUtil.getArkDrawResultToImage(user, 10)
                            if (list.isNotEmpty()) {
                                val image: BufferedImage = DrawUtil.combineArkOpImage(list)
                                val result = image.upload(event.subject)
                                result.asMessageChain()
                            } else {
                                overTimeMessage.convertToChain()
                            }
                        } else {
                            DrawUtil.getArkDrawResult(user, 10).convertToChain()
                        }
                    }
                    else -> {
                        return if (StringUtils.isNumeric(args[0])) {
                            DrawUtil.getArkDrawResult(user, args[0].toInt()).convertToChain()
                        } else {
                            getHelp().convertToChain()
                        }
                    }
                }
            } else {
                return getHelp().convertToChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps(
            "arkNights",
            arrayListOf("ark", "xf", "方舟寻访"),
            "明日方舟寻访模拟器",
            "nbot.commands.arknight",
            UserLevel.USER
        )

    override fun getHelp(): String = """
         ============ 命令帮助 ============
         /ark 单次寻访/十连寻访/[次数]
    """.trimIndent()
}