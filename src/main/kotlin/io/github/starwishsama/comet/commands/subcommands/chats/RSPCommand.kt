package io.github.starwishsama.comet.commands.subcommands.chats

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.FileUtil
import kotlinx.coroutines.delay
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.uploadAsImage
import java.io.File

class RSPCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(event.sender.id)) {
            if (args.isNotEmpty()) {
                val player = RockPaperScissors.getType(args[0])
                return if (player != null) {
                    val systemInt = RandomUtil.randomInt(RockPaperScissors.values().size)
                    val system = RockPaperScissors.values()[systemInt]
                    event.reply("角卷猜拳... 开始!")
                    event.reply("请稍等...wtm是弱弱回线")
                    val gif = File(FileUtil.getChildFolder("img"), system.fileName).uploadAsImage(event.subject)
                    event.reply(gif)
                    delay(1_500)
                    return when (RockPaperScissors.isWin(player, system)) {
                        -1 -> BotUtil.sendMessage("平局! わため出的是${system.cnName}")
                        0 -> BotUtil.sendMessage("你输了! わため出的是${system.cnName}")
                        1 -> BotUtil.sendMessage("你赢了! わため出的是${system.cnName}")
                        else -> BotUtil.sendMessage("这合理吗?")
                    }
                } else {
                    BotUtil.sendMessage("你的拳法杂乱无章, 这合理吗?")
                }
            } else {
                return BotUtil.sendMessage(getHelp())
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("janken", arrayListOf("猜拳", "石头剪刀布", "rsp", "cq"), "石头剪刀布", "nbot.commands.rsp", UserLevel.USER)

    override fun getHelp(): String = "/cq [石头/剪刀/布] 石头剪刀布"

    enum class RockPaperScissors(val cnName: String, val fileName: String) {
        ROCK("石头", "rock.png"), SCISSORS("剪刀", "scissor.png"), PAPER("布", "paper.png");

        companion object {
            fun getType(name: String): RockPaperScissors? {
                values().forEach {
                    if (it.cnName == name) {
                        return it
                    }
                }
                return null
            }

            /**
             * -1 平局 0 输 1 胜
             */
            fun isWin(player: RockPaperScissors, system: RockPaperScissors): Int {
                if (player == system) return -1
                return when (player) {
                    ROCK -> if (system != PAPER) 1 else 0
                    SCISSORS -> if (system != ROCK) 1 else 0
                    PAPER -> if (system != SCISSORS) 1 else 0
                }
            }
        }
    }
}