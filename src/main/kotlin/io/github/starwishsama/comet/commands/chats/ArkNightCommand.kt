package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.draw.items.ArkNightOperator
import io.github.starwishsama.comet.objects.draw.pool.ArkNightPool
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.DrawUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.upload
import org.apache.commons.lang3.StringUtils

@CometCommand
@Suppress("SpellCheckingInspection")
class ArkNightCommand : ChatCommand {
    private val pool = ArkNightPool()

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.hasNoCoolDown(event.sender.id)) {
            if (args.isNotEmpty()) {
                when (args[0]) {
                    "卡池", "kc", "pool" -> {
                        // @TODO
                    }
                    "单次寻访", "1", "单抽" -> {
                        return if (BotVariables.cfg.arkDrawUseImage) {
                            event.reply("请稍等...")
                            val list = pool.getArkDrawResult(user)
                            if (list.isNotEmpty()) {
                                val result = DrawUtil.combineArkOpImage(list)
                                if (result.lostOps.isNotEmpty())
                                    event.quoteReply(BotUtil.sendMessage("由于缺失资源文件, 以下干员无法显示 :(\n" +
                                            buildString {
                                                result.lostOps.forEach {
                                                    append("${it.name},")
                                                }
                                            }.removeSuffix(",")))
                                val gachaImage = withContext(Dispatchers.Default) { result.image.upload(event.subject) }
                                gachaImage.asMessageChain()
                            } else {
                                (DrawUtil.overTimeMessage + "\n剩余次数: ${user.commandTime}").convertToChain()
                            }
                        } else {
                            pool.getArkDrawResultAsString(user, 1).convertToChain()
                        }
                    }
                    "十连寻访", "10", "十连" -> {
                        return if (BotVariables.cfg.arkDrawUseImage) {
                            event.reply("请稍等...")
                            val list: List<ArkNightOperator> = pool.getArkDrawResult(user, 10)
                            if (list.isNotEmpty()) {
                                val result = DrawUtil.combineArkOpImage(list)
                                if (result.lostOps.isNotEmpty())
                                    event.quoteReply(BotUtil.sendMessage("由于缺失资源文件, 以下干员无法显示 :(\n" +
                                            buildString {
                                                result.lostOps.forEach {
                                                    append("${it.name},")
                                                }
                                            }.removeSuffix(",")))
                                val gachaImage = withContext(Dispatchers.IO) { result.image.upload(event.subject) }
                                gachaImage.asMessageChain()
                            } else {
                                (DrawUtil.overTimeMessage + "\n剩余次数: ${user.commandTime}").convertToChain()
                            }
                        } else {
                            pool.getArkDrawResultAsString(user, 10).convertToChain()
                        }
                    }
                    else -> {
                        return if (StringUtils.isNumeric(args[0])) {
                            val gachaTime: Int = try {
                                args[0].toInt()
                            } catch (e: NumberFormatException) {
                                return getHelp().convertToChain()
                            }
                            pool.getArkDrawResultAsString(user, gachaTime).convertToChain()
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
                "arknight",
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