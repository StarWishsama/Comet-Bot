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
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import io.github.starwishsama.nbot.util.PictureSearchUtil
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.queryUrl

class PictureSearch : UniversalCommand, WaitableCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(event.sender.id, 90)) {
            if (!SessionManager.isValidSession(event.sender.id)) {
                val session = Session(SessionType.DELAY, this, user.userQQ)
                session.putUser(event.sender.id)
                SessionManager.addSession(session)
            }
            return BotUtil.sendLocalMessage("msg.bot-prefix", "请发送需要搜索的图片").toMirai()
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps(
        "ps",
        arrayListOf("ytst", "st", "搜图", "以图搜图"),
        "以图搜图",
        "nbot.commands.picturesearch",
        UserLevel.USER
    )

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /ytst 以图搜图
    """.trimIndent()

    override suspend fun replyResult(message: MessageEvent, user: BotUser, session: Session) {
        val image = message.message[Image]
        message.reply(run {
            if (image != null) {
                message.reply("请稍等...")
                val result = PictureSearchUtil.sauceNaoSearch(image.queryUrl())
                if (result.similarity >= 60.0) {
                    "相似度:${result.similarity}%\n原图链接:${result.originalUrl}\n".toMirai()
                    /**.plus(stream.uploadAsImage(subject).asMessageChain())*/
                } else {
                    "相似度过低 (${result.similarity}%), 请尝试更换图片重试".toMirai()
                }
            } else {
                "无法识别图片".toMirai()
            }
        })

        SessionManager.expireSession(session)
    }
}