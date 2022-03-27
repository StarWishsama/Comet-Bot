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
import io.github.starwishsama.comet.api.command.interfaces.ConversationCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.service.command.GitHubService.addBranchFilter
import io.github.starwishsama.comet.service.command.GitHubService.getRepoList
import io.github.starwishsama.comet.service.command.GitHubService.lookupRepo
import io.github.starwishsama.comet.service.command.GitHubService.modifyRepo
import io.github.starwishsama.comet.service.command.GitHubService.removeBranchFilter
import io.github.starwishsama.comet.service.command.GitHubService.subscribeRepo
import io.github.starwishsama.comet.service.command.GitHubService.unsubscribeRepo
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object GithubCommand : ChatCommand, ConversationCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isEmpty()) {
            return getHelp().convertToChain()
        }

        return when (args[0]) {
            "add", "sub" -> subscribeRepo(user, args, event)
            "rm", "unsub" -> unsubscribeRepo(user, args, event)
            "list", "ls" -> getRepoList(user, args, event)
            "md", "modify" -> modifyRepo(user, args, event)
            "lookup", "cx" -> lookupRepo(args, event)
            "filter" -> {
                if (args.size == 2) {
                    when (args[1]) {
                        "add", "tj" -> addBranchFilter(args, event, user)
                        "remove", "rm", "sc" -> removeBranchFilter(args, event, user)
                        else -> getHelp().convertToChain()
                    }
                } else {
                    getHelp().convertToChain()
                }
            }
            else -> getHelp().convertToChain()
        }
    }

    override val props: CommandProps =
        CommandProps(
            "github",
            listOf("gh", "git"),
            "订阅 Github 项目推送动态",
            UserLevel.USER,
        )

    override fun getHelp(): String = """
        /gh add [仓库名称] 订阅 Github 项目推送动态
        /gh rm [仓库名称] 取消订阅 Github 项目推送动态
        /gh list 列出所有已订阅 Github 项目动态
        /gh md [仓库名称] 编辑 Github 仓库推送状态
        /gh cx [仓库名称] 查看 Github 仓库简略详情
        /gh filter [仓库名称] add/remove [规则] 添加推送分支过滤规则
    """.trimIndent()

    override suspend fun handle(event: MessageEvent, user: CometUser, session: Session) {
        event.subject.sendMessage(modifyRepo(user, event.message.contentToString().split(" "), event, session))
    }
}
