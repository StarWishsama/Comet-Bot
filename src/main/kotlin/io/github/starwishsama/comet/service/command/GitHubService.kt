/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.service.command

import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.thirdparty.github.GithubApi
import io.github.starwishsama.comet.file.GithubRepoData
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.config.GithubRepos
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.getContext
import net.mamoe.mirai.contact.isAdministrator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.yamlkt.Yaml.Default

object GitHubService {
    val repos: GithubRepos

    private val editorCache = mutableMapOf<Session, GithubRepos.GithubRepo>()

    init {
        GithubRepoData.check()
        val context = GithubRepoData.file.getContext()
        repos = if (context.isEmpty()) {
            GithubRepos()
        } else {
            Default.decodeFromString(GithubRepos.serializer(), context)
        }
    }

    fun subscribeRepo(user: CometUser, args: List<String>, event: MessageEvent): MessageChain {
        if (!user.hasPermission("nbot.commands.github") && !user.compareLevel(UserLevel.ADMIN) && (event is GroupMessageEvent && event.sender.isAdministrator())) {
            return CometVariables.localizationManager.getLocalizationText("message.no-permission").toChain()
        }

        val isGroup = event is GroupMessageEvent

        if (!isGroup && args.size < 3) {
            return "正确的命令: /github add [仓库名称] [群号] (仓库 Secret [可选])".toChain()
        } else if (args.size < 2) {
            return "正确的命令: /github add [仓库名称] (仓库 Secret [可选])".toChain()
        }

        val repoName = args[1]
        val repoSecret = if (!isGroup && args.size == 4) {
            args[3]
        } else if (args.size == 3) {
            args[2]
        } else {
            ""
        }

        val id = if (isGroup) {
            (event as GroupMessageEvent).group.id
        } else {
            args[2].toLongOrNull() ?: return "请填写正确的群号!".toChain()
        }

        if (!repoName.contains("/")) {
            return "请填写正确的仓库名称! 格式: 用户名/仓库名".toChain()
        }

        val authorAndRepo = repoName.split("/")

        val repo = repos.contains(authorAndRepo[0], authorAndRepo[1], id)

        return if (repo != null) {
            "你已经订阅过 ${repo.getFullName()} 了".toChain()
        } else {
            if (GithubApi.isRepoExists(
                    authorAndRepo[0],
                    authorAndRepo[1]
                ) || (GithubApi.isUserExists(authorAndRepo[0]) && authorAndRepo[1] == "*")
            ) {
                repos.add(id, authorAndRepo[0], authorAndRepo[1], repoSecret)

                val subscribeSuccessText =
                    "订阅 $repoName 成功!\n添加后, 请在对应项目下添加 WebHook 地址: ${CometVariables.cfg.webHookAddress}"

                if (repoSecret.isEmpty() || isGroup) {
                    subscribeSuccessText.toChain()
                } else {
                    "${subscribeSuccessText}\nSecret 为 $repoSecret".toChain()
                }
            } else {
                "仓库 $repoName 找不到或者没有权限访问!".toChain()
            }
        }
    }

    fun unsubscribeRepo(user: CometUser, args: List<String>, event: MessageEvent): MessageChain {
        if (!user.hasPermission("nbot.commands.github") && !user.compareLevel(UserLevel.ADMIN) && (event is GroupMessageEvent && event.sender.isAdministrator())) {
            return CometVariables.localizationManager.getLocalizationText("message.no-permission").toChain()
        }

        val isGroup = event is GroupMessageEvent

        if (!isGroup && args.size < 3) {
            return "正确的命令: /github rm [仓库名称] [群号]".toChain()
        } else if (args.size < 2) {
            return "正确的命令: /github rm [仓库名称]".toChain()
        }

        val repoName = args[1]

        if (!repoName.contains("/")) {
            return "请填写正确的仓库名称! 格式: 用户名/仓库名".toChain()
        }

        val id = if (isGroup) {
            (event as GroupMessageEvent).group.id
        } else {
            args[2].toLongOrNull() ?: return "请填写正确的群号!".toChain()
        }

        val authorAndRepo = repoName.split("/")

        return if (repos.remove(id, authorAndRepo[0], authorAndRepo[1])) {
            "取消订阅 $repoName 成功!\n退订后, 请在对应项目下删除 WebHook 地址".toChain()
        } else {
            "你还没订阅过 $repoName".toChain()
        }
    }

    fun getRepoList(user: CometUser, args: List<String>, event: MessageEvent): MessageChain {
        if (!user.hasPermission("nbot.commands.github") && !user.compareLevel(UserLevel.ADMIN) && (event is GroupMessageEvent && event.sender.isAdministrator())) {
            return CometVariables.localizationManager.getLocalizationText("message.no-permission").toChain()
        }

        val isGroup = event is GroupMessageEvent

        val id = if (isGroup) {
            (event as GroupMessageEvent).group.id
        } else {
            if (args.size == 1) {
                return "请填写正确的群号!".toChain()
            }

            args[1].toLongOrNull() ?: return "请填写正确的群号!".toChain()
        }


        return if (repos.repos.count { it.repoTarget.contains(id) } < 1) {
            "还没订阅过任何项目".toChain()
        } else {
            buildString {
                append("已订阅的项目列表:")
                repos.repos.filter { it.repoTarget.contains(id) }.forEach {
                    append("$it, ")
                }
            }.removeSuffix(", ").trim().toChain()
        }
    }

    fun modifyRepo(
        user: CometUser,
        args: List<String>,
        event: MessageEvent,
        command: ChatCommand,
        session: Session? = null
    ): MessageChain {
        if (!user.hasPermission("nbot.commands.github") && !user.compareLevel(UserLevel.ADMIN) && (event is GroupMessageEvent && event.sender.isAdministrator())) {
            return CometVariables.localizationManager.getLocalizationText("message.no-permission").toChain()
        }

        if (session == null) {
            if (args.size < 2) {
                return "正确的命令: /github modify [仓库名称]".toChain()
            } else if (args.isEmpty()) {
                return "正确的命令: /github add [仓库名称] (仓库 Secret [可选])".toChain()
            }

            val repoName = args[1]

            val repo = repos.repos.filter { it.getFullName() == repoName }.toMutableList().also { repo ->
                if (repo.isEmpty()) {
                    repo.addAll(repos.repos.filter { it.repoAuthor == repoName.split("/")[0] && it.repoName == "*" })
                }
            }

            return if (repo.isEmpty()) {
                "找不到你想修改的 Github 仓库哟".toChain()
            } else {
                val createdSession = Session(SessionTarget(privateId = event.sender.id), command)
                SessionHandler.insertSession(createdSession)

                editorCache[createdSession] = repo[0]

                """
                已进入仓库编辑模式
                输入 加群 [群号] / add [群号] 以添加订阅
                输入 删群 [群号] / rm [群号] 以取消订阅
                输入 退订 / unsub 以删除此仓库
                输入 退出 / exit 退出编辑模式
                """.trimIndent().toChain()
            }
        } else {
            return handleModifyMode(args, session)
        }
    }

    fun lookupRepo(args: List<String>, event: MessageEvent): MessageChain {
        val repoName = args[1].split("/")

        return GithubApi.getRepoInfoPicture(repoName[0], repoName[1]).toMessageChain(event.subject)
    }

    private fun handleModifyMode(args: List<String>, session: Session): MessageChain {
        val currentRepo = editorCache[session]

        if (currentRepo == null) {
            SessionHandler.removeSession(session)
            return "已退出编辑模式".toChain()
        }

        when (args[0]) {
            "加群", "add" -> {
                return if (args.size == 1) {
                    "输入 加群 [群号] / add [群号] 以添加订阅".toChain()
                } else {
                    val id = args[1].toLongOrNull() ?: return "请输入正确的群号!".toChain()

                    if (CometVariables.comet.getBot().getGroup(id) != null) {
                        currentRepo.repoTarget.add(id)

                        "添加订阅群聊 ($id) 成功!".toChain()
                    } else {
                        "你要添加的群聊 ($id) 不存在!".toChain()
                    }
                }
            }
            "删群", "rm" -> {
                return if (args.size == 1) {
                    "输入 删群 [群号] / rm [群号] 以添加订阅".toChain()
                } else {
                    val id = args[1].toLongOrNull() ?: return "请输入正确的群号!".toChain()

                    if (CometVariables.comet.getBot().getGroup(id) != null) {
                        currentRepo.repoTarget.remove(id)

                        "取消订阅群聊 ($id) 成功!".toChain()
                    } else {
                        "你要删除的群聊 ($id) 不存在!".toChain()
                    }
                }
            }
            "退订", "unsub" -> {
                return "退订状态: ${repos.repos.remove(currentRepo)}".toChain()
            }
            "退出", "exit" -> {
                SessionHandler.removeSession(session)
                editorCache.remove(session)
                return "已退出编辑模式".toChain()
            }
            else -> {
                return """
                输入 加群 [群号] / add [群号] 以添加订阅
                输入 删群 [群号] / rm [群号] 以取消订阅
                输入 退订 / unsub 以删除此仓库
                输入 退出 / exit 退出编辑模式
                """.trimIndent().toChain()
            }
        }
    }
}
