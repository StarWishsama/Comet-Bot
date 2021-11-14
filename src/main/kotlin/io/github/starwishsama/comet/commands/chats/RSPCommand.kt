/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import java.time.LocalDateTime


class RSPCommand : ChatCommand, ConversationCommand {
    /**
     * 储存正在石头剪刀布的用户
     */
    private val inProgressPlayer = mutableSetOf<Long>()

    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (!SessionHandler.hasSessionByID(event.sender.id, this::class.java)) {
            SessionHandler.insertSession(Session(SessionTarget(0, event.sender.id), this, false))
        }

        return "石头剪刀布... 开始! 你要出什么呢?".toChain()
    }

    override val props: CommandProps =
        CommandProps("janken", arrayListOf("猜拳", "石头剪刀布", "rsp", "cq"), "石头剪刀布", UserLevel.USER)

    override fun getHelp(): String = "/cq 石头剪刀布"

    override suspend fun handle(event: MessageEvent, user: CometUser, session: Session) {
        if (LocalDateTime.now().minusMinutes(1L).isAfter(session.createdTime)) {
            SessionHandler.removeSession(session)
            return
        }

        if (!inProgressPlayer.contains(user.id)) {
            try {
                val player = RockPaperScissors.getType(event.message.contentToString())
                inProgressPlayer.add(user.id)
                if (player != null) {
                    val systemInt = RandomUtil.randomInt(RockPaperScissors.values().size)
                    val system = RockPaperScissors.values()[systemInt]

                    val gameStatus = RockPaperScissors.isWin(player, system)

                    when (RockPaperScissors.isWin(player, system)) {
                        -1 -> event.subject.sendMessage(event.message.quote() + toChain("平局! 我出的是${system.display[0]}"))
                        0 -> event.subject.sendMessage(event.message.quote() + toChain("你输了! 我出的是${system.display[0]}"))
                        1 -> event.subject.sendMessage(event.message.quote() + toChain("你赢了! 我出的是${system.display[0]}"))
                        else -> event.subject.sendMessage(event.message.quote() + toChain("这合理吗?"))
                    }

                    if (gameStatus in -1..1) {
                        SessionHandler.removeSession(session)
                    }
                } else {
                    event.subject.sendMessage(event.message.quote() + toChain("你的拳法杂乱无章, 这合理吗?"))
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