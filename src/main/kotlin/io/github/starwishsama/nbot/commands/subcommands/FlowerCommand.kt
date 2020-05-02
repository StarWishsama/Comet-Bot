package io.github.starwishsama.nbot.commands.subcommands

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.commands.interfaces.WaitableCommand
import io.github.starwishsama.nbot.enums.SessionType
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.Flower
import io.github.starwishsama.nbot.sessions.Session
import io.github.starwishsama.nbot.sessions.SessionManager
import io.github.starwishsama.nbot.util.BotUtil
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import java.math.RoundingMode

class FlowerCommand : UniversalCommand, WaitableCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (args.isEmpty()){
            return getHelp().toMessage().asMessageChain()
        } else {
            return when (args[0]) {
                "lq", "get", "领取" -> {
                    if (user.flower == null) {
                        SessionManager.addSession(Session(SessionType.DELAY, this, user.userQQ))
                        BotUtil.sendLocalMessage("msg.bot-prefix", "请给你的花取个名吧 发送你要取的名字").toMessage().asMessageChain()
                    } else {
                        BotUtil.sendLocalMessage("msg.bot-prefix", "你已经种植了 ${user.flower?.flowerName}").toMessage()
                            .asMessageChain()
                    }
                }
                "gm", "rename", "改名" -> {
                    if (user.flower != null) {
                        if (args.size == 2) {
                            user.flower?.flowerName = args[1]
                            BotUtil.sendLocalMessage("msg.bot-prefix", "成功改名为 ${args[1]}").toMessage().asMessageChain()
                        } else {
                            getHelp().toMessage().asMessageChain()
                        }
                    } else {
                        BotUtil.sendLocalMessage("msg.bot-prefix", "你还没有种植花朵").toMessage().asMessageChain()
                    }
                }
                "sj", "collect", "收集" -> {
                    val flower = user.flower
                    if (flower != null) {
                        when (flower.waterCount) {
                            200 -> {
                                val point = RandomUtil.randomDouble(12.0, 34.0, 2, RoundingMode.HALF_DOWN)
                                flower.waterCount = flower.waterCount - RandomUtil.randomInt(10, 50)
                                BotUtil.sendLocalMessage("msg.bot-prefix", "成功收集 $point 点积分, 水量剩余 ${flower.waterCount}").toMessage().asMessageChain()
                            }
                            in 100 until 200 -> {
                                val point = RandomUtil.randomDouble(8.0, 26.0, 2, RoundingMode.HALF_DOWN)
                                flower.waterCount = flower.waterCount - RandomUtil.randomInt(9, 40)
                                BotUtil.sendLocalMessage("msg.bot-prefix", "成功收集 $point 点积分, 水量剩余 ${flower.waterCount}").toMessage().asMessageChain()
                            }
                            in 50 until 100 -> {
                                val point = RandomUtil.randomDouble(6.0, 17.0, 2, RoundingMode.HALF_DOWN)
                                flower.waterCount = flower.waterCount - RandomUtil.randomInt(8, 38)
                                BotUtil.sendLocalMessage("msg.bot-prefix", "成功收集 $point 点积分, 水量剩余 ${flower.waterCount}").toMessage().asMessageChain()
                            }
                            in 1 until 50 -> {
                                val point = RandomUtil.randomDouble(1.0, 4.0, 2, RoundingMode.HALF_DOWN)
                                flower.waterCount = flower.waterCount - RandomUtil.randomInt(flower.waterCount - 5, flower.waterCount)
                                BotUtil.sendLocalMessage("msg.bot-prefix", "成功收集 $point 点积分, 水量剩余 ${flower.waterCount}").toMessage().asMessageChain()
                            }
                            else -> {
                                BotUtil.sendLocalMessage("msg.bot-prefix", "你的花需要浇水了! 水壶可以在商店里购买.").toMessage().asMessageChain()
                            }
                        }
                    } else {
                        BotUtil.sendLocalMessage("msg.bot-prefix", "你还没有种植花朵").toMessage().asMessageChain()
                    }
                }
                "cx", "info", "查询" -> {
                    if (user.flower != null) {
                        val flower = user.flower
                        BotUtil.sendLocalMessage("msg.bot-prefix", "${flower?.flowerName}\n能量值: ${String.format("%.2f", flower?.energy)}\n能量值可以通过水群获得")
                            .toMessage().asMessageChain()
                    } else {
                        BotUtil.sendLocalMessage("msg.bot-prefix", "你还没有种植花朵").toMessage().asMessageChain()
                    }
                }
                else -> return getHelp().toMessage().asMessageChain()
            }
        }
    }

    override fun getProps(): CommandProps = CommandProps("flower", arrayListOf("hy", "花园"), "nbot.commands.flower", UserLevel.USER)

    override fun getHelp(): String = """
        /hy lq 领取你的绿植
        /hy sj 收集产出积分
        /hy gm 修改绿植名字
        /hy cx 查询绿植状态
    """.trimIndent()

    override suspend fun replyResult(message: ContactMessage, user: BotUser, session: Session) {
        user.flower = Flower(message.message.contentToString())
        message.reply(
            BotUtil.sendLocalMessage("msg.bot-prefix", "成功种植 ${user.flower?.flowerName}").toMessage().asMessageChain()
        )
    }

}