package io.github.starwishsama.comet.sessions.commands.roll

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.commands.chats.RollCommand
import io.github.starwishsama.comet.sessions.DaemonSession
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionUser
import net.mamoe.mirai.contact.Contact

class RollSession(
    override var groupId: Long,
    val rollItem: String,
    val stopAfterMinute: Int,
    val keyWord: String,
    val rollStarter: Contact,
    val count: Int,
) : Session(groupId, RollCommand()), DaemonSession {
    fun getRandomUser(): SessionUser {
        val index = RandomUtil.randomInt(users.size)
        val user = users[index]
        users.removeAt(index)
        return user
    }
}