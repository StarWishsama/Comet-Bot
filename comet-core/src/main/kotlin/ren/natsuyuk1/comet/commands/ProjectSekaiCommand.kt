package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.int
import moe.sdl.yac.parameters.types.long
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.commands.service.ProjectSekaiService
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.objects.MusicDifficulty
import ren.natsuyuk1.comet.network.thirdparty.projectsekai.toMessageWrapper
import ren.natsuyuk1.comet.objects.config.FeatureConfig
import ren.natsuyuk1.comet.objects.pjsk.ProjectSekaiUserData
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiCharts
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiMusic
import ren.natsuyuk1.comet.service.image.ProjectSekaiImageService
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.math.NumberUtil.formatDigests

val PROJECTSEKAI by lazy {
    CommandProperty(
        "projectsekai",
        listOf("pjsk", "啤酒烧烤"),
        "查询 Project Sekai: Colorful Stage 相关信息",
        " /pjsk bind -i [账号 ID] - 绑定账号\n" +
            "/pjsk event (排名) 查询当前活动信息\n" +
            "/pjsk pred 查询当前活动结束预测分数\n" +
            "/pjsk info 查询账号信息\n" +
            "/pjsk chart 查询歌曲谱面\n" +
            "/pjsk music 查询歌曲信息"
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
        if (FeatureConfig.data.projectSekai) {
            subcommands(
                Bind(subject, sender, user),
                Event(subject, sender, user),
                Prediction(subject, sender, user),
                Info(subject, sender, user),
                Best30(subject, sender, user),
                Chart(subject, sender, user),
                Music(subject, sender, user)
            )
        }
    }

    override suspend fun run() {
        if (!FeatureConfig.data.projectSekai) {
            subject.sendMessage("抱歉, Project Sekai 功能未被启用.".toMessageWrapper())
            return
        }

        if (currentContext.invokedSubcommand == null) {
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
            subject.sendMessage(ProjectSekaiService.b30(user))
        }
    }

    class Chart(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, CHART) {

        companion object {
            val CHART = SubCommandProperty(
                "chart",
                listOf("谱面", "谱面预览"),
                PROJECTSEKAI
            )
        }

        private val musicName by argument("歌曲名称")
        private val difficulty by option("-d", "--diff", "--difficulty", help = "歌曲难度")

        override suspend fun run() {
            val diff = if (difficulty == null) {
                MusicDifficulty.MASTER
            } else {
                when (difficulty!!.lowercase()) {
                    "ma", "master", "大师" -> MusicDifficulty.MASTER
                    "ex", "expert", "专家" -> MusicDifficulty.EXPERT
                    else -> {
                        subject.sendMessage("抱歉, 谱面预览只支持查看 EX 及以上等级的谱子.".toMessageWrapper())
                        return
                    }
                }
            }

            val pair = ProjectSekaiMusic.fuzzyGetMusicInfo(musicName)

            if (pair == null) {
                subject.sendMessage("找不到你想要搜索的歌曲哦".toMessageWrapper())
                return
            } else {
                ProjectSekaiCharts.getCharts(pair.first, diff)
            }

            subject.sendMessage("请稍后, 获取谱面中...".toMessageWrapper())

            val (image, msg) = ProjectSekaiImageService.drawCharts(pair.first, diff)

            if (image == null) {
                subject.sendMessage("获取谱面失败, $msg".toMessageWrapper())
            } else {
                subject.sendMessage(
                    buildMessageWrapper {
                        appendTextln("搜索准确度: ${pair.second}")
                        val bpmText = if (!pair.first.bpms.isNullOrEmpty()) {
                            buildString {
                                pair.first.bpms?.forEach { bi ->
                                    append("${bi.bpm.formatDigests(0)} - ")
                                }
                            }.removeSuffix(" - ")
                        } else {
                            pair.first.bpm.formatDigests(0)
                        }
                        appendTextln("BPM: $bpmText")
                        appendElement(image)
                    }
                )
            }
        }
    }

    class Music(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, MUSIC) {

        companion object {
            val MUSIC = SubCommandProperty(
                "music",
                listOf("查音乐", "音乐"),
                PROJECTSEKAI
            )
        }

        private val musicName by argument("歌曲名称")

        override suspend fun run() {
            val info = ProjectSekaiMusic.fuzzyGetMusicInfo(musicName)

            subject.sendMessage(info?.first?.toMessageWrapper() ?: "找不到你想要搜索的歌曲哦".toMessageWrapper())
        }
    }
}
