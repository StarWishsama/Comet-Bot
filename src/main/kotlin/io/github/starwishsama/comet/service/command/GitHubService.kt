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
import io.github.starwishsama.comet.CometVariables.comet
import io.github.starwishsama.comet.api.thirdparty.github.GithubApi
import io.github.starwishsama.comet.commands.chats.GithubCommand
import io.github.starwishsama.comet.file.GithubRepoData
import io.github.starwishsama.comet.i18n.LocalizationManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.config.GithubRepos
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.sessions.Session
import io.github.starwishsama.comet.sessions.SessionHandler
import io.github.starwishsama.comet.sessions.SessionTarget
import io.github.starwishsama.comet.utils.CometUtil.toMessageChain
import io.github.starwishsama.comet.utils.getContext
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.isOperator
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
        val (isGroup, id) = getCurrentGroup(event, args)

        if (id == null) {
            return "请填写正确的群号!".toMessageChain()
        }

        if (!checkPermission(user, event.sender, id)) {
            return LocalizationManager.getLocalizationText("message.no-permission").toMessageChain()
        }

        if (!isGroup && args.size < 3) {
            return "正确的命令: /github add [仓库名称] [群号] (仓库 Secret [可选])".toMessageChain()
        } else if (args.size < 2) {
            return "正确的命令: /github add [仓库名称] (仓库 Secret [可选])".toMessageChain()
        }

        val repoName = args[1]
        val repoSecret = if (!isGroup && args.size == 4) {
            args[3]
        } else if (args.size == 3) {
            args[2]
        } else {
            ""
        }

        if (!repoName.contains("/")) {
            return if (isGroup) {
                "请填写正确的仓库名称! 格式: 用户名/仓库名".toMessageChain()
            } else {
                "正确的命令: /github add [仓库名称] [群号] (仓库 Secret [可选])\n请填写正确的仓库名称! 格式: 用户名/仓库名".toMessageChain()
            }
        }

        if (!isGroup && comet.getBot().getGroup(id) == null) {
            return "机器人不在你指定的群内, 无法推送信息, 请先邀请机器人加入对应群聊.".toMessageChain()
        }

        val authorAndRepo = repoName.split("/")

        val repo = repos.contains(authorAndRepo[0], authorAndRepo[1], id)

        return if (repo != null) {
            "你已经订阅过 ${repo.getFullName()} 了".toMessageChain()
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
                    subscribeSuccessText.toMessageChain()
                } else {
                    "${subscribeSuccessText}\nSecret 为 $repoSecret".toMessageChain()
                }
            } else {
                "仓库 $repoName 找不到或者没有权限访问!".toMessageChain()
            }
        }
    }

    fun unsubscribeRepo(user: CometUser, args: List<String>, event: MessageEvent): MessageChain {
        val (isGroup, id) = getCurrentGroup(event, args)

        if (id == null) {
            return "请填写正确的群号!".toMessageChain()
        }

        if (!checkPermission(user, event.sender, id)) {
            return LocalizationManager.getLocalizationText("message.no-permission").toMessageChain()
        }

        if (!isGroup && args.size < 3) {
            return "正确的命令: /github rm [仓库名称] [群号]".toMessageChain()
        } else if (args.size < 2) {
            return "正确的命令: /github rm [仓库名称]".toMessageChain()
        }

        val repoName = args[1]

        if (!repoName.contains("/")) {
            return if (isGroup) {
                "请填写正确的仓库名称! 格式: 用户名/仓库名".toMessageChain()
            } else {
                "正确的命令: /github rm [仓库名称] [群号]\n请填写正确的仓库名称! 格式: 用户名/仓库名".toMessageChain()
            }
        }

        if (!isGroup && comet.getBot().getGroup(id) == null) {
            return "机器人不在你指定的群内.".toMessageChain()
        }

        val authorAndRepo = repoName.split("/")

        return if (repos.remove(id, authorAndRepo[0], authorAndRepo[1])) {
            "取消订阅 $repoName 成功!\n退订后, 请在对应项目下删除 WebHook 地址".toMessageChain()
        } else {
            "你还没订阅过 $repoName".toMessageChain()
        }
    }

    fun getRepoList(user: CometUser, args: List<String>, event: MessageEvent): MessageChain {
        val (isGroup, id) = getCurrentGroup(event, args)

        if (id == null) {
            return "请填写正确的群号!".toMessageChain()
        }

        if (!checkPermission(user, event.sender, id)) {
            return LocalizationManager.getLocalizationText("message.no-permission").toMessageChain()
        }

        if (!isGroup && comet.getBot().getGroup(id) == null) {
            return "机器人不在你指定的群内.".toMessageChain()
        }

        return if (repos.repos.none { it.repoTarget.contains(id) }) {
            "还没订阅过任何项目".toMessageChain()
        } else {
            buildString {
                append("已订阅的项目列表:")
                repos.repos.filter { it.repoTarget.contains(id) }.forEach {
                    append("$it, ")
                }
            }.removeSuffix(", ").trim().toMessageChain()
        }
    }

    fun modifyRepo(
        user: CometUser,
        args: List<String>,
        event: MessageEvent,
        session: Session? = null
    ): MessageChain {
        if (!user.compareLevel(UserLevel.ADMIN) && !user.hasPermission("nbot.commands.github")) {
            return LocalizationManager.getLocalizationText("message.no-permission").toMessageChain()
        }

        if (session == null) {
            if (args.size < 2) {
                return "正确的命令: /github modify [仓库名称]".toMessageChain()
            }

            val repoName = args[1]

            val repo = repos.repos.filter { it.getFullName() == repoName }.toMutableList().also { repo ->
                if (repo.isEmpty()) {
                    repo.addAll(repos.repos.filter { it.repoAuthor == repoName.split("/")[0] && it.repoName == "*" })
                }
            }

            return if (repo.isEmpty()) {
                "找不到你想修改的 Github 仓库哟".toMessageChain()
            } else {
                val createdSession = Session(SessionTarget(targetId = event.sender.id), GithubCommand)
                SessionHandler.insertSession(createdSession)

                editorCache[createdSession] = repo[0]

                """
                已进入仓库编辑模式
                输入 加群 [群号] / add [群号] 以添加订阅
                输入 删群 [群号] / rm [群号] 以取消订阅
                输入 退订 / unsub 以删除此仓库
                输入 退出 / exit 退出编辑模式
                """.trimIndent().toMessageChain()
            }
        } else {
            return handleModifyMode(args, session)
        }
    }

    fun lookupRepo(args: List<String>, event: MessageEvent): MessageChain {
        if (!args[1].contains("/")) {
            "请填写正确的仓库名称! 格式: 用户名/仓库名".toMessageChain()
        }

        val repoName = args[1].split("/")

        return GithubApi.getRepoInfoPicture(repoName[0], repoName[1]).toMessageChain(event.subject)
    }

    fun addBranchFilter(args: List<String>, event: MessageEvent, user: CometUser): MessageChain {
        val (isGroup, id) = getCurrentGroup(event, args)

        if (id == null) {
            return "请填写正确的群号!".toMessageChain()
        }

        if (!checkPermission(user, event.sender, id)) {
            return LocalizationManager.getLocalizationText("message.no-permission").toMessageChain()
        }

        if (!isGroup && args.size < 3) {
            return "正确的命令: /github filter [仓库名称] [群号] add [规则]".toMessageChain()
        } else if (args.size < 2) {
            return "正确的命令: /github filter [仓库名称] add [规则]".toMessageChain()
        }

        val repoName = args[1]

        val filter = if (isGroup) args[4] else args[3]

        if (!repoName.contains("/")) {
            return if (isGroup) {
                "请填写正确的仓库名称! 格式: 用户名/仓库名".toMessageChain()
            } else {
                "正确的命令: /github filter [仓库名称] add [规则]\n请填写正确的仓库名称! 格式: 用户名/仓库名".toMessageChain()
            }
        }

        if (!isGroup && comet.getBot().getGroup(id) == null) {
            return "机器人不在你指定的群内, 无法推送信息, 请先邀请机器人加入对应群聊.".toMessageChain()
        }

        val authorAndRepo = repoName.split("/")

        val repo = repos.repos.find { it.repoAuthor == authorAndRepo[0] && it.repoName == authorAndRepo[1] && it.repoTarget.contains(id) }

        return if (repo == null) {
            "你还没有订阅过 $repoName".toMessageChain()
        } else {
            val filterList = repo.branchFilter

            if (filterList.contains(filter)) {
                "你已经添加过对应规则: $filter".toMessageChain()
            } else {
                filterList.add(filter)
                "已成功添加过滤规则 $filter".toMessageChain()
            }
        }
    }

    fun removeBranchFilter(args: List<String>, event: MessageEvent, user: CometUser): MessageChain {
        val (isGroup, id) = getCurrentGroup(event, args)

        if (id == null) {
            return "请填写正确的群号!".toMessageChain()
        }

        if (!checkPermission(user, event.sender, id)) {
            return LocalizationManager.getLocalizationText("message.no-permission").toMessageChain()
        }

        if (!isGroup && args.size < 3) {
            return "正确的命令: /github filter [仓库名称] [群号] remove [规则]".toMessageChain()
        } else if (args.size < 2) {
            return "正确的命令: /github filter [仓库名称] remove [规则]".toMessageChain()
        }

        val repoName = args[1]

        val filter = if (isGroup) args[4] else args[3]

        if (!repoName.contains("/")) {
            return if (isGroup) {
                "请填写正确的仓库名称! 格式: 用户名/仓库名".toMessageChain()
            } else {
                "正确的命令: /github filter [仓库名称] remove [规则]\n请填写正确的仓库名称! 格式: 用户名/仓库名".toMessageChain()
            }
        }

        if (!isGroup && comet.getBot().getGroup(id) == null) {
            return "机器人不在你指定的群内, 无法推送信息, 请先邀请机器人加入对应群聊.".toMessageChain()
        }

        val authorAndRepo = repoName.split("/")

        val repo = repos.repos.find { it.repoAuthor == authorAndRepo[0] && it.repoName == authorAndRepo[1] && it.repoTarget.contains(id) }

        return if (repo == null) {
            "你还没有订阅过 $repoName".toMessageChain()
        } else {
            val filterList = repo.branchFilter

            if (filterList.contains(filter)) {
                filterList.remove(filter)
                "已成功删除过滤规则: $filter".toMessageChain()
            } else {
                "你还没有添加过对应规则 $filter".toMessageChain()
            }
        }
    }

    private fun getCurrentGroup(event: MessageEvent, input: List<String>): Pair<Boolean, Long?> {
        val isGroup = event is GroupMessageEvent

        val id = if (isGroup) {
            (event as GroupMessageEvent).group.id
        } else {
            if (input.size == 2) {
                null
            } else {
                input[2].toLongOrNull()
            }
        }

        return Pair(isGroup, id)
    }

    private fun handleModifyMode(args: List<String>, session: Session): MessageChain {
        val currentRepo = editorCache[session]

        if (currentRepo == null) {
            SessionHandler.removeSession(session)
            return "已退出编辑模式".toMessageChain()
        }

        when (args[0]) {
            "加群", "add" -> {
                return if (args.size == 1) {
                    "输入 加群 [群号] / add [群号] 以添加订阅".toMessageChain()
                } else {
                    val id = args[1].toLongOrNull() ?: return "请输入正确的群号!".toMessageChain()

                    if (comet.getBot().getGroup(id) != null) {
                        currentRepo.repoTarget.add(id)

                        "添加订阅群聊 ($id) 成功!".toMessageChain()
                    } else {
                        "你要添加的群聊 ($id) 不存在!".toMessageChain()
                    }
                }
            }
            "删群", "rm" -> {
                return if (args.size == 1) {
                    "输入 删群 [群号] / rm [群号] 以添加订阅".toMessageChain()
                } else {
                    val id = args[1].toLongOrNull() ?: return "请输入正确的群号!".toMessageChain()

                    if (comet.getBot().getGroup(id) != null) {
                        currentRepo.repoTarget.remove(id)

                        "取消订阅群聊 ($id) 成功!".toMessageChain()
                    } else {
                        "你要删除的群聊 ($id) 不存在!".toMessageChain()
                    }
                }
            }
            "退订", "unsub" -> {
                return "退订状态: ${repos.repos.remove(currentRepo)}".toMessageChain()
            }
            "退出", "exit" -> {
                SessionHandler.removeSession(session)
                editorCache.remove(session)
                return "已退出编辑模式".toMessageChain()
            }
            else -> {
                return """
                输入 加群 [群号] / add [群号] 以添加订阅
                输入 删群 [群号] / rm [群号] 以取消订阅
                输入 退订 / unsub 以删除此仓库
                输入 退出 / exit 退出编辑模式
                """.trimIndent().toMessageChain()
            }
        }
    }

    private fun checkPermission(user: CometUser, sender: Contact, groupId: Long = 0): Boolean {
        return when {
            user.compareLevel(UserLevel.ADMIN) || user.hasPermission("nbot.commands.github") -> true
            sender is Member -> sender.isOperator()
            groupId != 0L -> comet.getBot().getGroup(groupId)?.getMember(user.id)?.isOperator() == true
            else -> false
        }
    }
}
