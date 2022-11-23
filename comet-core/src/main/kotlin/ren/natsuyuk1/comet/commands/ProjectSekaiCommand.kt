package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.flag
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.int
import moe.sdl.yac.parameters.types.long
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.commands.service.ProjectSekaiService
import ren.natsuyuk1.comet.objects.config.FeatureConfig
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiData
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.util.toMessageWrapper

val PROJECTSEKAI by lazy {
    CommandProperty(
        "projectsekai",
        listOf("pjsk", "啤酒烧烤"),
        "查询 Project Sekai: Colorful Stage 相关信息",
        """
        /pjsk bind -i [账号 ID] - 绑定账号
        
        /pjsk event (排名) 查询当前活动信息
        
        /pjsk pred 查询当前活动结束预测分数    
        
        /pjsk info 查询账号信息
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

    init {
        subcommands(
            Bind(subject, sender, user),
            Event(subject, sender, user),
            Prediction(subject, sender, user),
            Info(subject, sender, user),
            Best30(subject, sender, user)
        )
    }

    private val refreshCache by option("--refresh", "-r").flag()

    override suspend fun run() {
        if (!FeatureConfig.data.projectSekai) {
            subject.sendMessage("抱歉, Project Sekai 功能未被启用.".toMessageWrapper())
            return
        }

        if (currentContext.invokedSubcommand == null) {
            if (refreshCache) {
                if (user.userLevel >= UserLevel.ADMIN) {
                    ProjectSekaiData.updateEventInfo()
                    subject.sendMessage("成功刷新活动信息".toMessageWrapper())
                } else {
                    subject.sendMessage("你没有权限".toMessageWrapper())
                }

                return
            }

            if (ProjectSekaiUserData.isBound(user.id.value)) {
                subject.sendMessage(ProjectSekaiService.queryUserEventInfo(user, 0))
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
                PROJECTSEKAI
            )
        }

        private val userID by option(
            "-i",
            "--id",
            help = "要绑定的世界计划账号 ID"
        ).long().default(-1)

        override suspend fun run() {
            if (!FeatureConfig.data.projectSekai) {
                subject.sendMessage("抱歉, Project Sekai 功能未被启用.".toMessageWrapper())
                return
            }

            if (userID == -1L || userID.toString().length != 18) {
                subject.sendMessage("请正确填写你的世界计划账号 ID! 例如 /pjsk bind -i 210043933010767872".toMessageWrapper())
                return
            }

            subject.sendMessage(ProjectSekaiService.bindAccount(user, userID))
        }
    }

    class Info(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, INFO) {

        companion object {
            val INFO = SubCommandProperty(
                "info",
                listOf("查询"),
                PROJECTSEKAI
            )
        }

        override suspend fun run() {
            if (!FeatureConfig.data.projectSekai) {
                subject.sendMessage("抱歉, Project Sekai 功能未被启用.".toMessageWrapper())
                return
            }

            subject.sendMessage(ProjectSekaiService.queryUserInfo(user))
        }
    }

    class Event(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, EVENT) {

        companion object {
            val EVENT = SubCommandProperty(
                "event",
                listOf("活动排名", "活排"),
                PROJECTSEKAI
            )
        }

        private val position by argument("排名位置", "欲查询的指定排名").int().default(0)

        override suspend fun run() {
            if (!FeatureConfig.data.projectSekai) {
                subject.sendMessage("抱歉, Project Sekai 功能未被启用.".toMessageWrapper())
                return
            }

            subject.sendMessage(ProjectSekaiService.queryUserEventInfo(user, position))
        }
    }

    class Prediction(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, PREDICTION) {

        companion object {
            val PREDICTION = SubCommandProperty(
                "pred",
                listOf("prediction", "预测", "预测线"),
                PROJECTSEKAI
            )
        }

        override suspend fun run() {
            subject.sendMessage(ProjectSekaiService.fetchPrediction())
        }
    }

    class Best30(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, BEST30) {

        companion object {
            val BEST30 = SubCommandProperty(
                "best30",
                listOf("b30"),
                PROJECTSEKAI
            )
        }

        override suspend fun run() {
            if (!FeatureConfig.data.projectSekai) {
                subject.sendMessage("抱歉, Project Sekai 功能未被启用.".toMessageWrapper())
                return
            }

            subject.sendMessage(ProjectSekaiService.b30(user))
        }
    }
}
