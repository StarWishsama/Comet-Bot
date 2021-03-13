package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class GithubCommand: ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isEmpty()) {
            return getHelp().convertToChain()
        }

        return when (args[0]) {
            "add" -> {
                return if (event is GroupMessageEvent) {
                    handleAddRepo(args[1], event.group.id)
                } else {
                    "该命令仅群聊可用".toChain()
                }
            }
            "rm" -> {
                return if (event is GroupMessageEvent) {
                    handleRemoveRepo(args[1], event.group.id)
                } else {
                    "该命令仅群聊可用".toChain()
                }
            }
            "list" -> {
                return if (event is GroupMessageEvent) {
                    handleListRepo(event.group.id)
                } else {
                    "该命令仅群聊可用".toChain()
                }
            }
            else -> getHelp().convertToChain()
        }
    }

    private fun handleAddRepo(repoName: String, groupId: Long): MessageChain {
        if (!repoName.contains("/")) {
            return "请填写正确的仓库名称! 格式: 用户名/仓库名".toChain()
        }

        val cfg = GroupConfigManager.getConfig(groupId) ?: return "该群聊尚未注册过 Comet!".toChain()
        val repos = cfg.githubRepoSubscribers
        return if (repos.contains(repoName)) {
            "你已经订阅过 $repoName 了".toChain()
        } else {
            repos.add(repoName)
            "订阅 $repoName 成功!\n添加后, 请在对应项目下添加 WebHook 地址 (设置为仅推送事件)".toChain()
        }
    }

    private fun handleRemoveRepo(repoName: String, groupId: Long): MessageChain {
        if (!repoName.contains("/")) {
            return "请填写正确的仓库名称! 格式: 用户名/仓库名".toChain()
        }

        val cfg = GroupConfigManager.getConfig(groupId) ?: return "该群聊尚未注册过 Comet!".toChain()
        val repos = cfg.githubRepoSubscribers
        return if (!repos.contains(repoName)) {
            "你还没订阅过 $repoName".toChain()
        } else {
            repos.remove(repoName)
            "取消订阅 $repoName 成功!\n退订后, 请在对应项目下删除 WebHook 地址".toChain()
        }
    }

    private fun handleListRepo(groupId: Long): MessageChain {
        val cfg = GroupConfigManager.getConfig(groupId) ?: return "该群聊尚未注册过 Comet!".toChain()
        val repos = cfg.githubRepoSubscribers
        return buildString {
            append("已订阅的项目列表:")
            repos.forEach {
                append("$it, ")
            }
        }.removeSuffix(",").trim().toChain()
    }

    override fun getProps(): CommandProps =
        CommandProps(
            "github",
            listOf("gh"),
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