package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.SessionType
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.sessions.Session
import io.github.starwishsama.nbot.sessions.SessionManager
import io.github.starwishsama.nbot.util.BotUtils
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.*

class PictureSearch : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
       if (BotUtils.isNoCoolDown(message.sender.id, 90)){
           return if (message is GroupMessage) {
               if (SessionManager.isValidSession(message.sender.id)){
                   BotUtils.sendLocalMessage("msg.bot-prefix", "请发送需要搜索的图片").toMessage().asMessageChain()
               } else {
                   val session = Session(message.group.id, SessionType.DELAY)
                   session.putUser(message.sender.id)
                   SessionManager.addSession(session)
                   BotUtils.sendLocalMessage("msg.bot-prefix", "请发送需要搜索的图片").toMessage().asMessageChain()
               }
           } else {
               BotUtils.sendLocalMessage("msg.bot-prefix", "抱歉, 本功能暂时只支持群聊使用").toMessage().asMessageChain()
           }
       }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("ps", arrayListOf("ytst", "st", "搜图", "以图搜图"), "nbot.commands.picturesearch", UserLevel.USER)
}