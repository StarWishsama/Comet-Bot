package io.github.starwishsama.comet.commands.subcommands.chats

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.commands.interfaces.SuspendCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.sessions.commands.guessnumber.GuessNumberSession
import io.github.starwishsama.comet.sessions.commands.guessnumber.GuessNumberUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.isNumeric
import io.github.starwishsama.comet.utils.toMsgChain
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.content
import java.time.Duration
import java.time.LocalDateTime

class GuessNumberCommand : ChatCommand, SuspendCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.id) && event is GroupMessageEvent) {
            val session = SessionManager.getSessionByGroup(event.group.id)
            if (session == null) {
                when {
                    args.isEmpty() -> {
                        val answer = RandomUtil.randomInt(0, 100)
                        BotVariables.logger.verbose("[猜数字] 群 ${event.group.id} 生成的随机数为 $answer")
                        SessionManager.addSession(GuessNumberSession(event.group.id, RandomUtil.randomInt(0, 101)))
                        return BotUtil.sendMessage("来猜个数字吧! 范围 [0, 100]")
                    }
                    args.size == 2 -> {
                        try {
                            val min = args[0].toInt()
                            val max = args[1].toInt()
                            if (min <= 0 || max <= 0) {
                                throw NumberFormatException("不支持负数")
                            }

                            if (min >= max) {
                                throw NumberFormatException("最小值不能大于等于最大值")
                            }
                            val answer = RandomUtil.randomInt(min, max + 1)
                            BotVariables.logger.verbose("[猜数字] 群 ${event.group.id} 生成的随机数为 $answer")
                            SessionManager.addSession(GuessNumberSession(event.group.id, RandomUtil.randomInt(0, 100)))
                            return BotUtil.sendMessage("猜一个数字吧! 范围 [$min, $max]")
                        } catch (e: NumberFormatException) {
                            return BotUtil.sendMessage("${e.message}")
                        }
                    }
                    else -> {
                        return getHelp().toMsgChain()
                    }
                }
            } else {
                return BotUtil.sendMessage("已经有一个游戏在进行中啦~")
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps
            = CommandProps("guessnumber", arrayListOf("csz"), "猜数字", "nbot.commands.guessnumber", UserLevel.USER)

    override fun getHelp(): String = """
        /csz 猜数字
        /csz [最小值] [最大值] 猜指定范围内的数字
    """.trimIndent()

    override suspend fun handleInput(event: MessageEvent, user: BotUser, session: Session) {
        val trueAnswer = (session as GuessNumberSession).answer
        session.lastAnswerTime = LocalDateTime.now()
        val answer = event.message.content
        var gnUser = session.getGNUser(user.id)

        if (answer.isNumeric()) {
            val answerInInt = answer.toInt()

            if (gnUser == null) {
                gnUser = GuessNumberUser(event.sender.id, event.sender.nameCardOrNick)
                session.users += gnUser
            }

            gnUser.guessTime += 1

            when {
                answerInInt > trueAnswer -> {
                    event.reply(BotUtil.sendMessage("你猜的数字大了").contentToString())
                }
                answerInInt < trueAnswer -> {
                    event.reply(BotUtil.sendMessageToString("你猜的数字小了"))
                }
                answerInInt == trueAnswer -> {
                    session.usedTime = Duration.between(session.startTime, LocalDateTime.now())
                    val sb = StringBuilder(BotUtil.sendMessageToString("${event.sender.nameCardOrNick} 猜对了!\n总用时: ${session.usedTime.seconds}s\n\n"))
                    val list = session.users.sortedBy { (it as GuessNumberUser).guessTime }
                    list.forEach {
                        sb.append((it as GuessNumberUser).username).append(" ").append(it.guessTime).append("次\n")
                    }
                    event.reply(sb.toString().trim())
                    SessionManager.expireSession(session)
                }
            }
        } else {
            when (answer) {
                "不玩了", "结束游戏", "退出游戏" -> {
                    SessionManager.expireSession(session)
                    event.reply(BotUtil.sendMessageToString("游戏已结束"))
                }
            }
        }
    }
}