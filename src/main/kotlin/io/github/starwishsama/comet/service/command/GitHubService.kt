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

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.thirdparty.github.GithubApi
import io.github.starwishsama.comet.file.DataFiles
import io.github.starwishsama.comet.objects.config.GithubRepos
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.getContext
import io.github.starwishsama.comet.utils.writeString
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.yamlkt.Yaml

object GitHubService {
    val repos: GithubRepos

    init {
        DataFiles.githubRepoData.init()
        val context = DataFiles.githubRepoData.file.getContext()
        repos = if (context.isEmpty()) {
            GithubRepos()
        } else {
            Yaml.Default.decodeFromString(GithubRepos.serializer(), context)
        }
    }

    fun subscribeRepo(args: List<String>, event: MessageEvent): MessageChain {
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

        return if (repos.contains(authorAndRepo[0], authorAndRepo[1])) {
            "你已经订阅过 $repoName 了".toChain()
        } else {
            if (GithubApi.isRepoExists(
                    authorAndRepo[0],
                    authorAndRepo[1]
                ) || (GithubApi.isUserExists(authorAndRepo[0]) && authorAndRepo[1] == "*")
            ) {
                repos.add(id, authorAndRepo[0], authorAndRepo[1], repoSecret)
                if (repoSecret.isEmpty() || isGroup) {
                    "订阅 $repoName 成功!\n添加后, 请在对应项目下添加 WebHook 地址: ${BotVariables.cfg.webHookAddress}".toChain()
                } else {
                    "订阅 $repoName 成功!\n添加后, 请在对应项目下添加 WebHook 地址: ${BotVariables.cfg.webHookAddress}\nSecret 为 $repoSecret".toChain()
                }
            } else {
                "仓库 $repoName 找不到或者没有权限访问!".toChain()
            }
        }
    }

    fun unsubscribeRepo(args: List<String>, event: MessageEvent): MessageChain {
        val repoName = args[1]

        if (!repoName.contains("/")) {
            return "请填写正确的仓库名称! 格式: 用户名/仓库名".toChain()
        }

        val isGroup = event is GroupMessageEvent

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

    fun getRepoList(args: List<String>, event: MessageEvent): MessageChain {
        val isGroup = event is GroupMessageEvent

        val id = if (isGroup) {
            (event as GroupMessageEvent).group.id
        } else {
            args[2].toLongOrNull() ?: return "请填写正确的群号!".toChain()
        }


        return if (repos.isEmpty()) {
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

    fun saveData() {
        DataFiles.githubRepoData.file.writeString(Yaml.encodeToString(repos))
    }
}
