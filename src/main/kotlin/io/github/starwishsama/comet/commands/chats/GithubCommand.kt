/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.service.command.GitHubService.getRepoList
import io.github.starwishsama.comet.service.command.GitHubService.subscribeRepo
import io.github.starwishsama.comet.service.command.GitHubService.unsubscribeRepo
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class GithubCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isEmpty()) {
            return getHelp().convertToChain()
        }

        return when (args[0]) {
            "add", "sub" -> subscribeRepo(args, event)
            "rm", "unsub" -> unsubscribeRepo(args, event)
            "list", "ls" -> getRepoList(args, event)
            else -> getHelp().convertToChain()
        }
    }

    override fun getProps(): CommandProps =
        CommandProps(
            "github",
            listOf("gh", "git"),
            "订阅 Github 项目推送动态",
            "nbot.commands.github",
            UserLevel.ADMIN
        )

    override fun getHelp(): String = """
        /gh add [仓库名称] 订阅 Github 项目推送动态
        /gh rm [仓库名称] 取消订阅 Github 项目推送动态
        /gh list 列出所有已订阅 Github 项目动态
    """.trimIndent()
}
