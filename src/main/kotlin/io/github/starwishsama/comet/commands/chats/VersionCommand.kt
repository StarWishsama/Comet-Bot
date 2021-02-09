package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BuildConfig
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import kotlin.time.ExperimentalTime

@CometCommand
class VersionCommand : ChatCommand {
    @ExperimentalTime
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (CometUtil.isNoCoolDown(event.sender.id)) {
            return ("彗星 Bot " + BuildConfig.version +
                    "\n运行时长 ${CometUtil.getRunningTime()}" +
                    "\n构建时间: ${BuildConfig.buildTime}" +
                    "\nMade with ❤ & Mirai ${BuildConfig.miraiVersion}").convertToChain()
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps {
        return CommandProps("version", arrayListOf("v", "版本", "bot"), "查看版本号", "nbot.commands.version", UserLevel.USER)
    }

    override fun getHelp(): String = ""
}