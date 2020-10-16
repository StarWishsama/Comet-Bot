package io.github.starwishsama.comet.commands.chats

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.SuspendCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.FileUtil
import kotlinx.coroutines.delay
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.uploadAsImage
import java.io.File

@CometCommand
class RSPCommand : ChatCommand, SuspendCommand {
    private val inProgressPlayer = mutableSetOf<Long>()
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.hasNoCoolDown(event.sender.id)) {
            event.reply("角卷猜拳... 开始! 你要出什么呢?")
            SessionManager.addSession(Session(this, user.id))
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("janken", arrayListOf("猜拳", "石头剪刀布", "rsp", "cq"), "石头剪刀布", "nbot.commands.rsp", UserLevel.USER)

    override fun getHelp(): String = "/cq 石头剪刀布"

    override suspend fun handleInput(event: MessageEvent, user: BotUser, session: Session) {
        if (!inProgressPlayer.contains(user.id)) {
            val player = RockPaperScissors.getType(event.message.contentToString())
            inProgressPlayer.add(user.id)
            if (player != null) {
                val systemInt = RandomUtil.randomInt(RockPaperScissors.values().size)
                val system = RockPaperScissors.values()[systemInt]
                delay(1_500)
                val img = File(FileUtil.getResourceFolder(), system.fileName).uploadAsImage(event.subject)
                event.reply(img)
                when (RockPaperScissors.isWin(player, system)) {
                    -1 -> event.reply(BotUtil.sendMessage("平局! わため出的是${system.cnName[0]}"))
                    0 -> event.reply(BotUtil.sendMessage("你输了! わため出的是${system.cnName[0]}"))
                    1 -> event.reply(BotUtil.sendMessage("你赢了! わため出的是${system.cnName[0]}"))
                    else -> event.reply(BotUtil.sendMessage("这合理吗?"))
                }
            } else {
                event.reply(BotUtil.sendMessage("你的拳法杂乱无章, 这合理吗?"))
            }
            inProgressPlayer.remove(user.id)
            SessionManager.expireSession(session)
        }
    }

    enum class RockPaperScissors(val cnName: Array<String>, val fileName: String) {
        ROCK(arrayOf("石头", "石子", "拳头", "拳", "👊"), "img${File.separator}rock.png"), SCISSORS(arrayOf("剪刀", "✂"), "img${File.separator}scissor.png"), PAPER(arrayOf("布", "包布"), "img${File.separator}paper.png");

        companion object {
            fun getType(name: String): RockPaperScissors? {
                values().forEach {
                    for (s in it.cnName) {
                        if (s == name) return it
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