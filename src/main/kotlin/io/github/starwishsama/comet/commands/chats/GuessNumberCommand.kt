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
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.sessions.commands.guessnumber.GuessNumberSession
import io.github.starwishsama.comet.sessions.commands.guessnumber.GuessNumberUser
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.content
import java.time.Duration
import java.time.LocalDateTime


class GuessNumberCommand : ChatCommand, ConversationCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (event is GroupMessageEvent) {
            if (!SessionHandler.hasSessionByGroup(event.group.id, this::class.java)) {
                when {
                    args.isEmpty() -> {
                        val answer = RandomUtil.randomInt(0, 100)
                        CometVariables.logger.info("[猜数字] 群 ${event.group.id} 生成的随机数为 $answer")
                        SessionHandler.insertSession(
                            GuessNumberSession(
                                SessionTarget(event.group.id),
                                RandomUtil.randomInt(0, 101)
                            )
                        )
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
                        CometVariables.logger.info("[猜数字] 群 ${event.group.id} 生成的随机数为 $answer")
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

    override val props: CommandProps =
        CommandProps("guessnumber", arrayListOf("csz"), "猜数字", "nbot.commands.guessnumber", UserLevel.USER)

    override fun getHelp(): String = """
        /csz 猜数字
        /csz [最小值] [最大值] 猜指定范围内的数字
    """.trimIndent()

    override suspend fun handle(event: MessageEvent, user: CometUser, session: Session) {
        val trueAnswer = (session as GuessNumberSession).answer
        session.lastAnswerTime = LocalDateTime.now()
        val answer = event.message.content
        var gnUser = session.getGuessNumberUser(user.id)

        if (answer.isNumeric()) {
            val answerInInt = answer.toInt()

            if (gnUser == null) {
                gnUser = GuessNumberUser(event.sender.id, event.sender.nameCardOrNick)
                session.users.add(gnUser)
            }

            gnUser.guessTime += 1


            when {
                answerInInt > trueAnswer -> {
                    event.subject.sendMessage(CometUtil.toChain("你猜的数字大了"))
                }
                answerInInt < trueAnswer -> {
                    event.subject.sendMessage(CometUtil.toChain("你猜的数字小了"))
                }
                answerInInt == trueAnswer -> {
                    session.usedTime = Duration.between(session.createdTime, LocalDateTime.now())
                    val sb =
                        StringBuilder(CometUtil.sendMessageAsString("${event.sender.nameCardOrNick} 猜对了!\n总用时: ${session.usedTime.seconds}s\n\n"))
                    val list = session.users.sortedBy { (it as GuessNumberUser).guessTime }
                    list.forEach {
                        sb.append("\n" + (it as GuessNumberUser).username).append(" ").append(it.guessTime)
                            .append("次\n")
                    }
                    event.subject.sendMessage(sb.toString().trim())
                    SessionHandler.removeSession(session)
                }
                else -> {
                    throw RuntimeException("GuessNumber: Impossible answer input: ${answerInInt}, answer: ${trueAnswer}")
                }
            }
        } else {
            when (answer) {
                "不玩了", "结束游戏", "退出游戏" -> {
                    SessionHandler.removeSession(session)
                    event.subject.sendMessage(CometUtil.sendMessageAsString("游戏已结束"))
                }
            }
        }
    }
}