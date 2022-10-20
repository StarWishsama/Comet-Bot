package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.options.option
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.commands.service.ApexService
import ren.natsuyuk1.comet.objects.apex.ApexLegendData
import ren.natsuyuk1.comet.util.toMessageWrapper

val APEX by lazy {
    CommandProperty(
        "apex",
        listOf(),
        "查询 Apex Legends 相关信息",
        """
        /apex bind [Origin 账号名] - 绑定账号
        /apex info 查询绑定账号用户信息
        """.trimIndent(),
        executeConsumePoint = 30
    )
}

class ApexCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    val user: CometUser
) : CometCommand(comet, sender, subject, message, user, APEX) {

    init {
        subcommands(
            Bind(subject, sender, user),
            Info(comet, subject, sender, user),
        )
    }

    override suspend fun run() {
        if (currentContext.invokedSubcommand == null) {
            if (ApexLegendData.isBound(user.id.value)) {
                subject.sendMessage(ApexService.queryUserInfo(user))
            } else {
                subject.sendMessage(property.helpText.toMessageWrapper())
            }
        }
    }

    class Bind(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, BIND) {

        companion object {
            val BIND = SubCommandProperty(
                "bind",
                listOf("绑定"),
                APEX
            )
        }

        private val username by argument()

        override suspend fun run() {
            subject.sendMessage(ApexService.bindAccount(user, username))
        }
    }

    class Info(
        val comet: Comet,
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, INFO) {

        private val username by option(
            "-n",
            "--name",
            help = "要查询的 Apex 账号名称"
        )

        companion object {
            val INFO = SubCommandProperty(
                "info",
                listOf("查询"),
                APEX
            )
        }

        override suspend fun run() {
            if (username == null) {
                subject.sendMessage("🔍 正在获取你的 Apex 玩家信息, 请坐和放宽...".toMessageWrapper())
                subject.sendMessage(ApexService.queryUserInfo(user))
            } else {
                subject.sendMessage("🔍 正在获取 Arcaea 信息, 请坐和放宽...".toMessageWrapper())
                subject.sendMessage(ApexService.queryUserInfo(user, username!!))
            }
        }
    }
}
