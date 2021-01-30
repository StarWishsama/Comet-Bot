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
import io.github.starwishsama.comet.utils.CometUtil
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote

@CometCommand
class RSPCommand : ChatCommand, SuspendCommand {
    /**
     * 储存正在石头剪刀布的用户
     */
    private val inProgressPlayer = mutableSetOf<Long>()

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (CometUtil.isNoCoolDown(event.sender.id)) {
            event.subject.sendMessage("石头剪刀布... 开始! 你要出什么呢?")
            SessionManager.addAutoCloseSession(Session(this, user.id), 1)
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("janken", arrayListOf("猜拳", "石头剪刀布", "rsp", "cq"), "石头剪刀布", "nbot.commands.rsp", UserLevel.USER)

    override fun getHelp(): String = "/cq 石头剪刀布"

    override fun handleInput(event: MessageEvent, user: BotUser, session: Session) {
        if (!inProgressPlayer.contains(user.id)) {
            try {
                val player = RockPaperScissors.getType(event.message.contentToString())
                inProgressPlayer.add(user.id)
                if (player != null) {
                    val systemInt = RandomUtil.randomInt(RockPaperScissors.values().size)
                    val system = RockPaperScissors.values()[systemInt]

                    val gameStatus = RockPaperScissors.isWin(player, system)

                    runBlocking {
                        when (RockPaperScissors.isWin(player, system)) {
                            -1 -> event.subject.sendMessage(event.message.quote() + CometUtil.sendMessage("平局! 我出的是${system.display[0]}"))
                            0 -> event.subject.sendMessage(event.message.quote() + CometUtil.sendMessage("你输了! 我出的是${system.display[0]}"))
                            1 -> event.subject.sendMessage(event.message.quote() + CometUtil.sendMessage("你赢了! 我出的是${system.display[0]}"))
                            else -> event.subject.sendMessage(event.message.quote() + CometUtil.sendMessage("这合理吗?"))
                        }
                    }

                    if (gameStatus in -1..1) {
                        SessionManager.expireSession(session)
                    }
                } else {
                    runBlocking { event.subject.sendMessage(event.message.quote() + CometUtil.sendMessage("你的拳法杂乱无章, 这合理吗?")) }
                }
            } finally {
                inProgressPlayer.remove(user.id)
            }
        }
    }

    enum class RockPaperScissors(val display: Array<String>) {
        ROCK(arrayOf("石头", "石子", "拳头", "拳", "👊")), SCISSORS(arrayOf("剪刀", "✂")), PAPER(arrayOf("布", "包布"));

        companion object {
            fun getType(name: String): RockPaperScissors? {
                values().forEach {
                    for (s in it.display) {
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