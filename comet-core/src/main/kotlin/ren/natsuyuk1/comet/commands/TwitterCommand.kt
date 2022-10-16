package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.options.option
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.network.thirdparty.twitter.TwitterAPI
import ren.natsuyuk1.comet.network.thirdparty.twitter.toMessageWrapper
import ren.natsuyuk1.comet.util.groupAdminChecker
import ren.natsuyuk1.comet.util.toMessageWrapper

val TWITTER = CommandProperty(
    "twitter",
    listOf("twit", "推特"),
    "查询推特用户、推文等信息",
    """
    /twit user 查询用户
    /twit tweet 查询推文    
    """.trimIndent(),
    permissionLevel = UserLevel.ADMIN,
    extraPermissionChecker = groupAdminChecker
)

class TwitterCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    user: CometUser
) : CometCommand(comet, sender, subject, message, user, TWITTER) {
    init {
        subcommands(
            User(sender, subject, user),
            Tweet(sender, subject, user)
        )
    }

    override suspend fun run() {
        if (currentContext.invokedSubcommand == null) {
            subject.sendMessage(property.helpText.toMessageWrapper())
        }
    }

    class User(
        override val sender: PlatformCommandSender,
        override val subject: PlatformCommandSender,
        user: CometUser
    ) : CometSubCommand(sender, subject, user, USER) {
        companion object {
            val USER = SubCommandProperty("user", listOf("用户", "yh"), TWITTER)
        }

        val username by argument("推特用户名")

        override suspend fun run() {
            val user = TwitterAPI.fetchUser(username)

            if (user == null) {
                subject.sendMessage("找不到对应的用户捏, 检查下用户名格式吧".toMessageWrapper())
                return
            }

            subject.sendMessage(user.toMessageWrapper())
        }
    }

    class Tweet(
        override val sender: PlatformCommandSender,
        override val subject: PlatformCommandSender,
        user: CometUser
    ) : CometSubCommand(sender, subject, user, TWEET) {
        companion object {
            val TWEET = SubCommandProperty("tweet", listOf("推文", "tw"), TWITTER)
        }

        val username by option("-u", "--user", help = "推特用户名")
        val id by option("-i", "--id", help = "推文 ID")

        override suspend fun run() {
            if (id != null) {
                val resp = TwitterAPI.fetchTweet(id!!)
                if (resp == null) {
                    subject.sendMessage("找不到对应 ID ($id) 的推文".toMessageWrapper())
                } else {
                    resp.tweet?.toMessageWrapper(resp.includes)?.let { subject.sendMessage(it) }
                }
            } else if (username != null) {
                val user = TwitterAPI.fetchUserByUsername(username!!)

                if (user == null) {
                    subject.sendMessage("找不到对应的用户捏, 检查下用户名格式吧".toMessageWrapper())
                    return
                }

                val resp = TwitterAPI.fetchTimeline(user.id)

                if (resp == null) {
                    subject.sendMessage("找不到对应 ID ($id) 的推文".toMessageWrapper())
                } else {
                    resp.tweets?.firstOrNull()?.toMessageWrapper(resp.includes)?.let { subject.sendMessage(it) }
                }
            } else {
                subject.sendMessage("请填写用户名或推文 ID 其中之一".toMessageWrapper())
            }
        }
    }
}
