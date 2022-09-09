package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.subcommands
import moe.sdl.yac.parameters.arguments.argument
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.*
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.pusher.CometPushTarget
import ren.natsuyuk1.comet.pusher.CometPushTargetType
import ren.natsuyuk1.comet.pusher.impl.rss.RSSPusher
import ren.natsuyuk1.comet.util.groupAdminChecker
import ren.natsuyuk1.comet.util.toMessageWrapper

val RSS = CommandProperty(
    "rss",
    listOf(),
    "RSS 订阅",
    "/rss sub [URL] 为本群订阅一个 RSS 源\n/rss unsub [URL] 为本群取消订阅 RSS 源\n/rss ls 查询本群已订阅的 RSS 源",
    permissionLevel = UserLevel.ADMIN,
    extraPermissionChecker = groupAdminChecker
)

private val URL_REGEX = "^(http|https)://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?\$".toRegex()
private val URL_PREFIX_REGEX = "^(http|https)$"

class RSSCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    val user: CometUser
): CometCommand(comet, sender, subject, message, user, RSS) {

    init {
        subcommands(Subscribe(sender, subject, user), UnSubscribe(sender, subject, user), List(sender, subject, user))
    }

    override suspend fun run() {
        if (currentContext.invokedSubcommand == null) {
            subject.sendMessage(RSS.helpText.toMessageWrapper())
        }
    }

    class Subscribe(
        override val sender: PlatformCommandSender,
        override val subject: PlatformCommandSender,
        user: CometUser
    ) : CometSubCommand(subject, sender, user, SUBSCRIBE) {
        companion object {
            val SUBSCRIBE = SubCommandProperty(
                "subscribe",
                listOf("sub", "订阅"),
                RSS
            )
        }

        private val url by argument("RSS 源 URL")

        override suspend fun run() {
            if (subject.asGroup() == null) {
                subject.sendMessage("本命令仅限群聊使用!".toMessageWrapper())
            }

            if (!url.matches(URL_REGEX)) {
                subject.sendMessage("请输入有效的 RSS 源链接!".toMessageWrapper())
            }

            if (RSSPusher.subscriber.containsKey(url)) {
                if (RSSPusher.subscriber[url]?.find { it.id == subject.id && it.platform == subject.platform } != null) {
                    subject.sendMessage("你已经订阅过这个 RSS 源了!".toMessageWrapper())
                } else {
                    RSSPusher.subscriber[url]?.add(
                        CometPushTarget(
                            subject.id,
                            CometPushTargetType.GROUP,
                            subject.platform
                        )
                    )
                    subject.sendMessage("成功订阅此 RSS 源!".toMessageWrapper())
                }
            } else {
                RSSPusher.subscriber[url] =
                    mutableListOf(CometPushTarget(subject.id, CometPushTargetType.GROUP, subject.platform))
                subject.sendMessage("成功订阅此 RSS 源!".toMessageWrapper())
            }
        }
    }

    class UnSubscribe(
        override val sender: PlatformCommandSender,
        override val subject: PlatformCommandSender,
        user: CometUser
    ) : CometSubCommand(subject, sender, user, UNSUBSCRIBE) {
        companion object {
            val UNSUBSCRIBE = SubCommandProperty(
                "unsubscribe",
                listOf("unsub", "退订"),
                RSS
            )
        }

        private val url by argument("RSS 源 URL")

        override suspend fun run() {
            if (subject.asGroup() == null) {
                subject.sendMessage("本命令仅限群聊使用!".toMessageWrapper())
            }

            if (!url.matches(URL_REGEX)) {
                subject.sendMessage("请输入有效的 RSS 源链接!".toMessageWrapper())
            }

            if (RSSPusher.subscriber.containsKey(url)) {
                if (RSSPusher.subscriber[url]?.find { it.id == subject.id && it.platform == subject.platform } == null) {
                    subject.sendMessage("本群还未订阅过这个 RSS 源!".toMessageWrapper())
                } else {
                    RSSPusher.subscriber[url]?.removeIf { it.id == subject.id && it.platform == subject.platform }
                    subject.sendMessage("成功取消订阅此 RSS 源!".toMessageWrapper())
                }
            } else {
                subject.sendMessage("本群还未订阅过这个 RSS 源!".toMessageWrapper())
            }
        }
    }

    class List(
        override val sender: PlatformCommandSender,
        override val subject: PlatformCommandSender,
        user: CometUser
    ) : CometSubCommand(subject, sender, user, LIST) {
        companion object {
            val LIST = SubCommandProperty(
                "list",
                listOf("ls", "订阅列表"),
                RSS
            )
        }

        override suspend fun run() {
            if (subject.asGroup() == null) {
                subject.sendMessage("本命令仅限群聊使用!".toMessageWrapper())
            }

            val subRSS = RSSPusher.subscriber
                .filter { sub -> sub.value.find { it.id == subject.id && it.platform == subject.platform } != null }
                .map { it.key }

            if (subRSS.isEmpty()) {
                subject.sendMessage("本群还未订阅过任何 RSS 源!".toMessageWrapper())
            } else {
                subject.sendMessage(buildMessageWrapper {
                    appendText("本群已订阅的 RSS 源 >>", true)
                    subRSS.forEach { k ->
                        appendText("| ${k.replace(URL_PREFIX_REGEX, "")}", true)
                    }
                })
            }
        }
    }
}
