package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.int
import moe.sdl.yac.parameters.types.long
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.commands.service.ProjectSekaiService
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.toArgs
import ren.natsuyuk1.comet.utils.string.StringUtil.toMessageWrapper

val PROJECTSEKAI by lazy {
    CommandProperty(
        "projectsekai",
        listOf("pjsk", "啤酒烧烤"),
        "查询 Project Sekai: Colorful Stage 相关信息",
        """
        /pjsk bind -i [账号 ID] - 绑定账号
        /pjsk event (排名) 查询当前活动信息
        /pjsk pred 查询当前活动结束预测分数    
        """.trimIndent()
    )
}

class ProjectSekaiCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    val user: CometUser
) : CometCommand(comet, sender, subject, message, user, PROJECTSEKAI) {
    override suspend fun run() {
        if (message.parseToString().toArgs().size == 1) {
            subject.sendMessage(PROJECTSEKAI.helpText.toMessageWrapper())
        }
    }

    class Bind(
        private val subject: PlatformCommandSender,
        val user: CometUser
    ) : CliktCommand() {
        private val userID by option(
            "-i", "--id",
            help = "要绑定的世界计划账号 ID"
        ).long().default(-1)

        override suspend fun run() {
            if (userID == -1L || userID.toString().length != 18) {
                subject.sendMessage("请正确填写你的世界计划账号 ID! 例如 /pjsk bind -i 210043933010767872".toMessageWrapper())
                return
            }

            subject.sendMessage(ProjectSekaiService.bindAccount(user, userID))
        }
    }

    class Info(
        private val sender: PlatformCommandSender,
        private val subject: PlatformCommandSender,
        val user: CometUser
    ) : CliktCommand() {
        override suspend fun run() {
            TODO()
        }
    }

    class Event(
        private val subject: PlatformCommandSender,
        val user: CometUser
    ) : CliktCommand() {
        private val position by argument("排名位置", "欲查询的指定排名").int().default(0)

        override suspend fun run() {
            subject.sendMessage(ProjectSekaiService.lookupUserInfo(user, position))
        }
    }

    class Prediction(private val subject: PlatformCommandSender) :
        CliktCommand(name = "pred") {
        override suspend fun run() {
            subject.sendMessage(ProjectSekaiService.fetchPrediction())
        }
    }
}
