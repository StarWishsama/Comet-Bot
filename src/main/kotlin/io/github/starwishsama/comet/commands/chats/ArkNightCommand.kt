package io.github.starwishsama.comet.commands.chats


import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.gacha.GachaResult
import io.github.starwishsama.comet.objects.gacha.pool.ArkNightPool
import io.github.starwishsama.comet.service.gacha.GachaService
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.GachaUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.uploadAsImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.at
import org.apache.commons.lang3.StringUtils


@Suppress("SpellCheckingInspection")
class ArkNightCommand : ChatCommand {
    private var pool = GachaService.getPoolsByType<ArkNightPool>()[0]

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (!GachaService.isArkNightUsable()) {
            return "还未下载明日方舟卡池数据, 无法使用".toChain()
        }

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
                    return if (args.size == 1) {
                        buildString {
                            append("目前卡池: ${pool.name}\n")
                            append("详细信息: ${pool.description}")

                            val pools = GachaService.getPoolsByType<ArkNightPool>()

                            append("\n\n")

                            pools.forEach {
                                append(it.name + " ->\n" + it.description + "\n")
                            }
                        }.trim().toChain()
                    } else {
                        val poolName = args[1]
                        val pools =
                            GachaService.getPoolsByType<ArkNightPool>().parallelStream().filter { it.name == poolName }
                                .findFirst()
                        if (pools.isPresent) {
                            pool = pools.get()
                            "成功修改卡池为: ${pool.name}".toChain()
                        } else {
                            "找不到名为 $poolName 的卡池".toChain()
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

    override fun getProps(): CommandProps =
        CommandProps(
            "arknight",
            arrayListOf("ark", "xf", "方舟寻访"),
            "明日方舟寻访模拟器",
            "nbot.commands.arknight",
            UserLevel.USER,
            consumePoint = 15
        )

    override fun getHelp(): String = """
         ============ 命令帮助 ============
         /ark 单次寻访/十连寻访/[次数]
    """.trimIndent()

    suspend fun getGachaResult(event: MessageEvent, user: BotUser, time: Int): MessageChain {
        val gachaResult: GachaResult = pool.getArkDrawResult(user, time)
        return if (GachaUtil.arkPictureIsUsable()) {
            generatePictureGachaResult(pool, event, user, gachaResult)
        } else {
            pool.getArkDrawResultAsString(user, gachaResult).toChain()
        }
    }

    private suspend fun generatePictureGachaResult(
        pool: ArkNightPool,
        event: MessageEvent,
        user: BotUser,
        gachaResult: GachaResult
    ): MessageChain {
        event.subject.sendMessage("请稍等...")

        val ops = gachaResult.items

        return if (ops.isNotEmpty()) {
            // 只获取最后十个
            val result =
                GachaUtil.combineGachaImage(if (ops.size <= 10) ops else ops.subList(ops.size - 11, ops.size - 1), pool)
            if (result.lostItem.isNotEmpty())
                event.subject.sendMessage(
                    event.message.quote() + toChain(
                        "由于缺失资源文件, 以下干员无法显示 :(\n" +
                                buildString {
                                    result.lostItem.forEach {
                                        append("${it.name},")
                                    }
                                }.removeSuffix(",")
                    )
                )
            val gachaImage = withContext(Dispatchers.IO) { result.image.uploadAsImage(event.subject) }

            val reply = gachaImage.plus("\n").plus(pool.getArkDrawResultAsString(user, gachaResult))

            if (event is GroupMessageEvent) event.sender.at().plus("\n").plus(reply) else reply
        } else {
            (GachaUtil.overTimeMessage + "\n剩余次数: ${user.commandTime}").convertToChain()
        }
    }
}