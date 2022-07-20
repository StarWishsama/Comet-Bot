package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.parameters.options.option
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.commands.service.BiliBiliService
import ren.natsuyuk1.comet.utils.message.MessageWrapper
import ren.natsuyuk1.comet.utils.string.StringUtil.toMessageWrapper

val BILIBILI = CommandProperty(
    "bilibili",
    listOf("bili", "哔哩哔哩"),
    "查询哔哩哔哩用户/视频/动态等相关信息",
    """
    /bili user 查询用户信息
    /bili video 查询视频信息
    /bili dynamic 查询用户动态
    /bili subscribe 订阅用户动态 (含开播信息)   
     
    Powered by yabapi 
    """.trimIndent()
)

class BiliBiliCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    val user: CometUser
) : CometCommand(comet, sender, subject, message, user, BILIBILI) {

    override suspend fun run() {
        TODO("Not yet implemented")
    }

    class User(
        private val subject: PlatformCommandSender,
        val user: CometUser
    ) : CliktCommand(name = "user") {
        override fun aliases(): Map<String, List<String>> =
            mapOf(
                "用户" to listOf("user"),
                "yh" to listOf("user")
            )

        override suspend fun run() {
            TODO("Not yet implemented")
        }
    }

    class Dynamic(
        private val subject: PlatformCommandSender,
        val user: CometUser
    ) : CliktCommand(name = "dynamic") {

        private val nameOrUID by option("-n", "--name", "-i", "--id", help = "欲查询用户的名称或 UID")

        override fun aliases(): Map<String, List<String>> =
            mapOf("用户" to listOf("user"))

        override suspend fun run() {
            if (nameOrUID == null) {
                subject.sendMessage("请提供欲查询用户的名称或 UID!".toMessageWrapper())
            }

            BiliBiliService
        }
    }
}
