package io.github.starwishsama.comet.sessions.commands.roll

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.commands.chats.RollCommand
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.sessions.SessionUser
import io.github.starwishsama.comet.utils.CometUtil
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.messageChainOf
import java.time.LocalDateTime

class RollSession(
    override var target: SessionTarget,
    val rollItem: String,
    val stopAfterMinute: Int,
    val keyWord: String,
    val rollStarter: Long,
    val count: Int,
) : Session(target, RollCommand::class.java, silent = true, handle = { e: MessageEvent, _: BotUser, session: Session ->
    handleInput(e, session)
}) {
    fun getRandomUser(): SessionUser {
        val index = RandomUtil.randomInt(users.size)
        val iter = users.iterator()
        var current = 0

        while (iter.hasNext()) {
            val user = iter.next()
            if (current == index) {
                users.remove(user)
                return user
            } else {
                current++
            }
        }

        throw RuntimeException("Cannot found random user in Roll: ${users.size}")
    }

    companion object {
        fun handleInput(event: MessageEvent, session: Session) {
            if (session is RollSession && event is GroupMessageEvent) {
                if (LocalDateTime.now().minusMinutes(session.stopAfterMinute.toLong()).isBefore(session.createdTime)) {
                    generateResult(session, event)
                    SessionHandler.removeSession(session)
                    return
                }

                if (session.rollStarter == event.sender.id) return

                if (session.keyWord.isEmpty()) {
                    session.users.add(SessionUser(event.sender.id))
                    return
                }

                val message = event.message.contentToString()

                if (session.keyWord == message) {
                    session.users.add(SessionUser(event.sender.id))
                }
            }
        }

        private fun generateResult(session: RollSession, event: GroupMessageEvent) {
            val group = event.bot.getGroup(session.target.groupId)

            if (group == null) {
                BotVariables.daemonLogger.warning("推送开奖消息失败: 找不到对应的群[${group}]")
            } else {
                var winner = messageChainOf()
                session.getWinningUsers().forEach { su ->
                    winner = winner.plus(At(su.id))
                }
                GlobalScope.launch {
                    group.sendMessage(
                        CometUtil.toChain(
                            "由${group[session.rollStarter]?.nameCardOrNick}发起的抽奖开奖了!\n" +
                                    "奖品: ${session.rollItem}\n" +
                                    "中奖者: "
                        ) + winner
                    )
                }
            }
        }

        private fun RollSession.getWinningUsers(): List<SessionUser> {
            val winners = mutableListOf<SessionUser>()

            for (i in 0 until count) {
                winners.add(getRandomUser())
            }

            return winners
        }
    }
}