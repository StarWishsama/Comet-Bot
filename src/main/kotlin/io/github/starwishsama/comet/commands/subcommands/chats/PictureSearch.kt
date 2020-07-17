package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.SuspendCommand
import io.github.starwishsama.comet.commands.interfaces.UniversalCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.PictureSearchUtil
import io.github.starwishsama.comet.utils.toMirai
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.queryUrl

class PictureSearch : UniversalCommand, SuspendCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(event.sender.id)) {
            if (!SessionManager.isValidSessionById(event.sender.id)) {
                SessionManager.addSession(Session(this, user.userQQ))
            }
            return BotUtil.sendMsgPrefix("请发送需要搜索的图片").toMirai()
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

    override suspend fun handleInput(event: MessageEvent, user: BotUser, session: Session) {
        SessionManager.expireSession(session)
        val image = event.message[Image]
        if (image != null) {
            event.reply("请稍等...")
            val result = PictureSearchUtil.sauceNaoSearch(image.queryUrl())
            if (result.similarity >= 52.5) {
                event.reply("相似度:${result.similarity}%\n原图链接:${result.originalUrl}\n")
            } else {
                event.reply("相似度过低 (${result.similarity}%), 请尝试更换图片重试")
            }
        } else {
            event.reply("请发送图片!")
        }
    }
}