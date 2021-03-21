package io.github.starwishsama.comet.sessions.commands.guessnumber

import io.github.starwishsama.comet.commands.chats.GuessNumberCommand
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.content
import java.time.Duration
import java.time.LocalDateTime

open class GuessNumberSession(override val target: SessionTarget, val answer: Int) :
    Session(target, GuessNumberCommand(), false) {
    lateinit var usedTime: Duration
    lateinit var lastAnswerTime: LocalDateTime

    override suspend fun handle(event: MessageEvent, user: BotUser, session: Session) {
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
                        sb.append((it as GuessNumberUser).username).append(" ").append(it.guessTime)
                            .append("次\n")
                    }
                    event.subject.sendMessage(sb.toString().trim())
                    SessionHandler.removeSession(session)
                }
                else -> throw RuntimeException("GuessNumber: Impossible answer input: ${answerInInt}, answer: ${trueAnswer}")
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

    fun getGuessNumberUser(id: Long): GuessNumberUser? {
        users.forEach {
            if (it is GuessNumberUser && it.id == id) {
                return it
            }
        }
        return null
    }
}