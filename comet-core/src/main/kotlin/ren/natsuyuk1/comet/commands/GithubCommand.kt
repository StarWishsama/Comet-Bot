package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.long
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.commands.service.GithubCommandService
import ren.natsuyuk1.comet.objects.github.data.GithubRepoData
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.message.MessageWrapper

val GITHUB = CommandProperty(
    "github",
    listOf("gh", "git"),
    "查询, 订阅 GitHub 仓库相关信息",
    "/github sub [仓库名] 订阅仓库\n" +
        "/github unsub [仓库名] 取消订阅仓库\n" +
        "/github info [仓库名/URL] 查询仓库信息\n" +
        "/github setting [仓库名] 修改仓库订阅事件\n" +
        "/github list 查询已订阅的仓库"
)

class GithubCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    val user: CometUser
) : CometCommand(comet, sender, subject, message, user, GITHUB) {

    init {
        subcommands(
            Subscribe(subject, sender, user),
            UnSubscribe(subject, sender, user),
            Info(subject, sender, user),
            Setting(message, subject, sender, user),
            List(subject, sender, user)
        )
    }

    override suspend fun run() {
        val subcommand = currentContext.invokedSubcommand
        if (subcommand == null) {
            subject.sendMessage(GITHUB.helpText.toMessageWrapper())
        }
    }

    class Subscribe(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, SUBSCRIBE) {
        companion object {
            val SUBSCRIBE = SubCommandProperty("subscribe", listOf("sub", "订阅"), GITHUB, UserLevel.ADMIN)
        }

        private val repoName by argument(help = "GitHub 仓库名称").default("")

        private val groupID by option("-g", "--group", help = "群号").long()

        override suspend fun run() {
            if (repoName.isBlank()) {
                subject.sendMessage("请输入 GitHub 仓库名!".toMessageWrapper())
                return
            }

            if (subject !is Group && groupID == null) {
                subject.sendMessage("请提供欲订阅 GitHub 仓库动态的群号!".toMessageWrapper())
                return
            }

            GithubCommandService.processSubscribe(subject, sender, user, groupID ?: subject.id, repoName)
        }
    }

    class UnSubscribe(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, UNSUBSCRIBE) {
        companion object {
            val UNSUBSCRIBE = SubCommandProperty("unsubscribe", listOf("unsub", "订阅"), GITHUB, UserLevel.ADMIN)
        }

        private val repoName by argument(help = "GitHub 仓库名称").default("")

        private val groupID by option("-g", "--group", help = "群号").long()

        override suspend fun run() {
            if (repoName.isBlank()) {
                subject.sendMessage("请输入 GitHub 仓库名!".toMessageWrapper())
                return
            }

            if (subject !is Group && groupID == null) {
                subject.sendMessage("请提供欲订阅 GitHub 仓库动态的群号!".toMessageWrapper())
                return
            }

            GithubCommandService.processUnsubscribe(subject, groupID ?: subject.id, repoName)
        }
    }

    class Info(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, INFO) {
        companion object {
            val INFO = SubCommandProperty("info", listOf("cx", "查询"), GITHUB)
        }

        private val repoName by argument(help = "GitHub 仓库名称/链接").default("")

        override suspend fun run() {
            if (repoName.isBlank()) {
                subject.sendMessage("请输入 GitHub 仓库名称或链接!".toMessageWrapper())
            } else {
                GithubCommandService.fetchRepoInfo(subject, repoName)
            }
        }
    }

    class Setting(
        val message: MessageWrapper,
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, SETTING) {

        private val add by option("-a", "--add").flag(default = false)
        private val remove by option("-r", "--remove").flag(default = false)
        private val groupID by option("-g", "--group", help = "群号").long()
        private val repoName by argument(help = "GitHub 仓库名称")
        private val branch by option("-b", "--branch", help = "分支名")
        private val eventName by option("-e", "--event", help = "事件名称")

        companion object {
            val SETTING = SubCommandProperty("setting", listOf("st", "设置"), GITHUB)
        }

        override suspend fun run() {
            if (subject !is Group && groupID == null) {
                subject.sendMessage("请提供欲订阅 GitHub 仓库动态的群号!".toMessageWrapper())
                return
            }

            if (!GithubRepoData.exists(repoName, groupID ?: subject.id)) {
                subject.sendMessage("找不到你要查询的仓库, 可能是没有订阅过?".toMessageWrapper())
                return
            }

            if (add) {
                if (!GithubRepoData.exists(repoName, groupID ?: subject.id)) {
                    subject.sendMessage("找不到你要查询的仓库, 可能是没有订阅过?".toMessageWrapper())
                    return
                }

                val repo = GithubRepoData.find(repoName) ?: return

                if (branch != null) {
                    val target = repo.subscribers.find { it.id == groupID }

                    val result = if (target == null) {
                        GithubRepoData.Data.GithubRepo.GithubRepoSubscriber(
                            groupID ?: subject.id
                        ).also {
                            it.subscribeBranch.add(branch!!)
                            repo.subscribers.add(it)
                        }
                        true
                    } else {
                        target.subscribeBranch.add(branch!!)
                    }

                    if (result) {
                        subject.sendMessage("成功订阅分支 $branch".toMessageWrapper())
                    } else {
                        subject.sendMessage("已经订阅过分支 $branch 了".toMessageWrapper())
                    }
                } else if (eventName != null) {
                    when (repo.subscribers.find { it.id == groupID }?.subscribeEvent?.add(eventName!!)) {
                        true -> {
                            subject.sendMessage("成功订阅事件 $eventName".toMessageWrapper())
                        }
                        false -> {
                            subject.sendMessage("已经订阅过事件 $eventName 了".toMessageWrapper())
                        }
                        else -> {
                            subject.sendMessage("在订阅事件时发生了异常".toMessageWrapper())
                        }
                    }
                }
            } else if (remove) {
                if (GithubRepoData.exists(repoName, groupID ?: subject.id)) {
                    subject.sendMessage("找不到你要查询的仓库, 可能是没有订阅过?".toMessageWrapper())
                    return
                }

                val repo = GithubRepoData.find(repoName) ?: return

                if (branch != null) {
                    val result = repo.subscribers.find { it.id == groupID }?.subscribeBranch?.remove(branch)

                    if (result == true) {
                        subject.sendMessage("已取消订阅分支 $branch".toMessageWrapper())
                    } else {
                        subject.sendMessage("在取消订阅分支时发生了异常".toMessageWrapper())
                    }
                } else if (eventName != null) {
                    val result = repo.subscribers.find { it.id == groupID }?.subscribeEvent?.remove(eventName)

                    if (result == true) {
                        subject.sendMessage("已取消订阅事件 $branch".toMessageWrapper())
                    } else {
                        subject.sendMessage("在取消订阅事件时发生了异常".toMessageWrapper())
                    }
                }
            } else {
                GithubCommandService.fetchRepoSetting(subject, repoName, groupID ?: subject.id)
            }
        }
    }

    class List(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, LIST) {
        companion object {
            val LIST = SubCommandProperty("list", listOf("ls", "列表"), GITHUB)
        }

        private val groupID by option("-g", "--group", help = "群号").long()

        override suspend fun run() {
            if (subject !is Group && groupID == null) {
                subject.sendMessage("请提供欲订阅 GitHub 仓库动态的群号!".toMessageWrapper())
                return
            }

            GithubCommandService.fetchSubscribeRepos(subject, groupID ?: subject.id)
        }
    }
}
