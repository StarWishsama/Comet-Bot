package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.GroupCommand
import io.github.starwishsama.nbot.enums.UserLevel
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.*

class DebugCommand : GroupCommand {
    override suspend fun executeGroup(message: GroupMessage): MessageChain {
        val msg = message.message.toString().split(" ")
        if (msg.size > 1) {
            when (msg[1]) {
                "up" -> {
                    if (msg[2].isNotEmpty()) {
                        val searchResult = BotInstance().client.appAPI.searchUser(keyword = msg[2]).await()
                        return if (searchResult.data.items.isNotEmpty()) {
                            val item = searchResult.data.items[0]
                            (item.title + "\n粉丝数: " + item.fans + "\n最近投递的视频: " + item.avItems[0].title).toMessage()
                                .toChain()
                        } else {
                            "Bot > 账号不存在".toMessage().toChain()
                        }
                    }
                }
                else -> return "Bot > 命令不存在\n请注意: 这里的命令随时会被删除.".toMessage().toChain()
            }
        }
        return "".toMessage().toChain()
    }

    override fun getProps(): CommandProps {
        return CommandProps("debug", null, "nbot.commands.debug", UserLevel.ADMIN)
    }
}