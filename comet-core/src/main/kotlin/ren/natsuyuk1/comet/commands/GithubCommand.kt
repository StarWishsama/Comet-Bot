package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.long
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.commands.service.GithubCommandService
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.toArgs
import ren.natsuyuk1.comet.utils.string.StringUtil.toMessageWrapper

val GITHUB = CommandProperty(
    "github",
    listOf("gh", "git"),
    "查询, 订阅 GitHub 仓库相关信息",
    "/github sub [仓库名] 订阅仓库\n" +
        "/github unsub [仓库名] 取消订阅仓库\n" +
        "/github info [仓库名/URL] 查询仓库信息\n" +
        "/github setting [仓库名] 修改仓库订阅事件"
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
            Info(comet, subject, sender, user)
        )
    }

    override suspend fun run() {
        if (message.parseToString().toArgs().size == 1) {
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
        val comet: Comet,
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
                GithubCommandService.fetchRepoInfo(comet, subject, repoName)
            }
        }
    }

    class Setting(
        val comet: Comet,
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, SETTING) {
        companion object {
            val SETTING = SubCommandProperty("setting", listOf("st", "设置"), GITHUB)
        }

        private val repoName by argument(help = "GitHub 仓库名称").default("")

        private val groupID by option("-g", "--group", help = "群号").long()

        override suspend fun run() {
            if (repoName.isBlank()) {
                subject.sendMessage("请输入 GitHub 仓库名称或链接!".toMessageWrapper())
            }

            if (subject !is Group && groupID == null) {
                subject.sendMessage("请提供欲订阅 GitHub 仓库动态的群号!".toMessageWrapper())
                return
            }

            GithubCommandService.fetchRepoInfo(comet, subject, repoName)
        }
    }
}
