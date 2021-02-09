package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.GachaManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.gacha.items.ArkNightOperator
import io.github.starwishsama.comet.objects.gacha.pool.ArkNightPool
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.sendMessage
import io.github.starwishsama.comet.utils.GachaUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.uploadAsImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.at
import org.apache.commons.lang3.StringUtils

@CometCommand
@Suppress("SpellCheckingInspection")
class ArkNightCommand : ChatCommand {
    var pool = GachaManager.getPoolsByType<ArkNightPool>()[0]

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (CometUtil.isNoCoolDown(event.sender.id, 30)) {
            if (args.isNotEmpty()) {
                when (args[0]) {
                    "单次寻访", "1", "单抽" -> {
                        return getGachaResult(event, user, 1)
                    }
                    "十连寻访", "10", "十连" -> {
                        return getGachaResult(event, user, 10)
                    }
                    "一井", "300", "来一井" -> {
                        return getGachaResult(event, user, 300)
                    }
                    "pool", "卡池", "卡池信息" -> {
                        if (args.size == 1) {
                            return buildString {
                                append("目前卡池: ${pool.name}\n")
                                append("详细信息: ${pool.description}")
                            }.sendMessage()
                        } else {
                            val poolName = args[1]
                            val pools = GachaManager.getPoolsByType<ArkNightPool>().parallelStream().filter { it.name == poolName }.findFirst()
                            return if (pools.isPresent) {
                                pool = pools.get()
                                "成功修改卡池为: ${pool.name}".sendMessage()
                            } else {
                                "找不到名为 $poolName 的卡池".sendMessage()
                            }
                        }
                    }
                    else -> {
                        return if (StringUtils.isNumeric(args[0])) {
                            val gachaTime: Int = try {
                                args[0].toInt()
                            } catch (e: NumberFormatException) {
                                return getHelp().convertToChain()
                            }

                            return getGachaResult(event, user, gachaTime)
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

    suspend fun getGachaResult(event: MessageEvent, user: BotUser, time: Int): MessageChain {
        val list: List<ArkNightOperator> = pool.getArkDrawResult(user, time)
        return if (GachaUtil.arkPictureIsUsable()) {
            generatePictureGachaResult(pool, event, user, list)
        } else {
            pool.getArkDrawResultAsString(user, list).sendMessage()
        }
    }

    private suspend fun generatePictureGachaResult(pool: ArkNightPool, event: MessageEvent, user: BotUser, ops: List<ArkNightOperator>): MessageChain {
        event.subject.sendMessage("请稍等...")

        return if (ops.isNotEmpty()) {
            // 只获取最后十个
            val result = GachaUtil.combineGachaImage(if (ops.size <= 10) ops else ops.subList(ops.size - 11, ops.size - 1), pool)
            if (result.lostItem.isNotEmpty())
                event.subject.sendMessage(
                    event.message.quote() + sendMessage("由于缺失资源文件, 以下干员无法显示 :(\n" +
                            buildString {
                                result.lostItem.forEach {
                                    append("${it.name},")
                                }
                            }.removeSuffix(","))
                )
            val gachaImage = withContext(Dispatchers.IO) { result.image.uploadAsImage(event.subject) }

            val reply = gachaImage.plus("\n").plus(pool.getArkDrawResultAsString(user, ops))

            if (event is GroupMessageEvent) event.sender.at().plus("\n").plus(reply) else reply
        } else {
            (GachaUtil.overTimeMessage + "\n剩余次数: ${user.commandTime}").convertToChain()
        }
    }
}