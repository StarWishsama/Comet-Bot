package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.default
import moe.sdl.yac.parameters.options.option
import moe.sdl.yac.parameters.types.int
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.commands.service.BiliBiliService
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.toArgs

val BILIBILI = CommandProperty(
    "bilibili",
    listOf("bili", "哔哩哔哩"),
    "查询哔哩哔哩用户/视频/动态等相关信息",
    """
    /bili user 查询用户信息
    /bili video 查询视频信息
    /bili dynamic 查询用户动态
    /bili sub 订阅用户动态 (含开播信息) 
    /bili unsub 取消订阅用户动态
    """.trimIndent()
)

class BiliBiliCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    val user: CometUser
) : CometCommand(comet, sender, subject, message, user, BILIBILI) {
    init {
        subcommands(
            User(subject, sender, user),
            Dynamic(subject, sender, user),
            Video(subject, sender, user)
        )
    }

    override suspend fun run() {
        if (message.parseToString().toArgs().size == 1) {
            subject.sendMessage(BILIBILI.helpText.toMessageWrapper())
        }
    }

    class User(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, USER) {

        companion object {
            val USER = SubCommandProperty(
                "user",
                listOf("用户", "yh"),
                BILIBILI
            )
        }

        private val name by option("-n", "--name", help = "欲查询用户的名称")
        private val uid by option("-i", "--id", help = "欲查询用户的 UID").int()

        override suspend fun run() {
            if (uid != null) {
                BiliBiliService.processUserSearch(subject, sender, id = uid!!)
            } else if (name != null) {
                BiliBiliService.processUserSearch(subject, sender, keyword = name!!)
            } else {
                subject.sendMessage("请提供欲查询用户的名称或 ID!".toMessageWrapper())
            }
        }
    }

    class Video(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, VIDEO) {

        private val video by argument(help = "视频 av/bv号/链接")

        companion object {
            val VIDEO = SubCommandProperty(
                "video",
                listOf("视频", "vid", "sp"),
                BILIBILI
            )
        }

        override suspend fun run() {
            if (video.isBlank()) {
                subject.sendMessage(BILIBILI.helpText.toMessageWrapper())
            } else {
                BiliBiliService.processVideoSearch(subject, video)
            }
        }
    }

    class Dynamic(
        override val subject: PlatformCommandSender,
        override val sender: PlatformCommandSender,
        override val user: CometUser
    ) : CometSubCommand(subject, sender, user, DYNAMIC) {

        companion object {
            val DYNAMIC = SubCommandProperty(
                "dynamic",
                listOf("动态", "dyna"),
                BILIBILI
            )
        }

        private val dynamicID by argument(help = "欲查询动态的 ID").default("")

        override suspend fun run() {
            if (dynamicID.isEmpty()) {
                subject.sendMessage("请提供欲查询动态的 ID 或链接!".toMessageWrapper())
                return
            } else {
                BiliBiliService.processDynamicSearch(subject, dynamicID)
            }
        }
    }
}
