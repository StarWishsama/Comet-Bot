package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.commands.interfaces.WaitableCommand
import io.github.starwishsama.nbot.enums.SessionType
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.sessions.Session
import io.github.starwishsama.nbot.sessions.SessionManager
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.PictureSearchUtil
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.*

class PictureSearch : UniversalCommand, WaitableCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
       if (BotUtil.isNoCoolDown(message.sender.id, 90)){
           return if (message is GroupMessage) {
               if (SessionManager.isValidSession(message.sender.id)){
                   BotUtil.sendLocalMessage("msg.bot-prefix", "请发送需要搜索的图片").toMessage().asMessageChain()
               } else {
                   val session = Session(message.group.id, SessionType.DELAY, this)
                   session.putUser(message.sender.id)
                   SessionManager.addSession(session)
                   BotUtil.sendLocalMessage("msg.bot-prefix", "请发送需要搜索的图片").toMessage().asMessageChain()
               }
           } else {
               BotUtil.sendLocalMessage("msg.bot-prefix", "抱歉, 本功能暂时只支持群聊使用").toMessage().asMessageChain()
           }
       }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("ps", arrayListOf("ytst", "st", "搜图", "以图搜图"), "nbot.commands.picturesearch", UserLevel.USER)
    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /ytst 以图搜图
    """.trimIndent()

    override suspend fun replyResult(message: ContactMessage, user: BotUser, session: Session) {
        try {
            val image = message[Image]
            message.reply(run {
                if (image.isContentNotEmpty()) {
                    message.reply("请稍等...")
                    val result = PictureSearchUtil.sauceNaoSearch(image.queryUrl())
                    if (result.similarity >= 60.0) {
                        /**val map = mutableMapOf<String, String>()
                        map["user-agent"] =
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36"
                        val stream = HttpRequest.get(result.picUrl)
                        .setFollowRedirects(true)
                        .timeout(150_000)
                        .addHeaders(map)
                        .execute().bodyStream()*/
                        "相似度:${result.similarity}%\n原图链接:${result.originalUrl}\n".toMessage()
                                .asMessageChain()/**.plus(stream.uploadAsImage(subject).asMessageChain())*/
                    } else {
                        "相似度过低 (${result.similarity}%), 请尝试更换图片重试".toMessage().asMessageChain()
                    }
                } else {
                    EmptyMessageChain
                }
            })
        } catch (e: NoSuchElementException){
            message.reply("无法识别图片")
        }
    }
}