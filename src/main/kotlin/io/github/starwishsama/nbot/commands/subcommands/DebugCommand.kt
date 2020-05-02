package io.github.starwishsama.nbot.commands.subcommands

import cn.hutool.http.HttpRequest
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.file.DataSetup
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.message.uploadAsImage
import java.io.IOException

class DebugCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (args.isNotEmpty() && BotUtil.isNoCoolDown(message.sender.id)) {
            when (args[0]) {
                "image" -> {
                    val map = mutableMapOf<String, String>()
                    map["user-agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36"
                    val stream = HttpRequest.get("https://i.loli.net/2020/04/14/INmrZVhiyK5dgbk.jpg")
                            .setFollowRedirects(true)
                            .timeout(150_000)
                            .addHeaders(map)
                            .execute().bodyStream()
                    return stream.uploadAsImage(message.subject).asMessageChain()
                }
                "reload" -> {
                    return try {
                        DataSetup.reload()
                        BotUtil.sendLocalMessage("msg.bot-prefix", "重载成功.").toMirai()
                    } catch (e: IOException) {
                        BotUtil.sendLocalMessage("msg.bot-prefix", "在重载时发生了异常.").toMirai()
                    }
                }
                "help" -> return getHelp().toMessage().asMessageChain()
                else -> return "Bot > 命令不存在\n${getHelp()}".toMirai()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps {
        return CommandProps("debug", null, "nbot.commands.debug", UserLevel.ADMIN)
    }

    override fun getHelp(): String = "直接开 IDE 看会死掉吗"
}
