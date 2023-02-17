package ren.natsuyuk1.comet.commands

import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.options.option
import mu.KotlinLogging
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.UserLevel
import ren.natsuyuk1.comet.api.user.group.GroupPermission
import ren.natsuyuk1.comet.util.groupAdminChecker
import ren.natsuyuk1.comet.util.toMessageWrapper
import ren.natsuyuk1.comet.util.yac.user
import ren.natsuyuk1.comet.utils.math.TimeUtil

private val logger = KotlinLogging.logger {}

val MUTE = CommandProperty(
    "mute",
    listOf("禁言", "jy"),
    "禁言指定用户",
    """
    /mute [用户] 禁言一个非管理员的用户
    
    参数列表:
    [用户] 支持 @/ID
    -t 时间, 默认为 1 分钟
    格式为 xdxhxmxs, 例如 1d2h 代表禁言一天两小时
    """.trimIndent(),
    permissionLevel = UserLevel.ADMIN,
    extraPermissionChecker = groupAdminChecker,
)

class MuteCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    val user: CometUser,
) : CometCommand(comet, sender, subject, message, user, MUTE) {
    private val id by argument("禁言用户").user(message)

    private val time by option("--time", "-t")

    override suspend fun run() {
        if (subject !is Group) {
            subject.sendMessage("无法在群聊外禁言哦".toMessageWrapper())
            return
        }

        if (subject.getBotPermission() < GroupPermission.ADMIN) {
            subject.sendMessage("Comet 没有权限禁言!".toMessageWrapper())
            return
        }

        if (id == 0L) {
            subject.sendMessage("输入的禁言对象有误, 检查一下吧".toMessageWrapper())
            return
        }

        val target = subject.getMember(id)

        if (target == null) {
            subject.sendMessage("找不到你要禁言的对象, 检查一下吧".toMessageWrapper())
            return
        }

        val actualTime = time?.let { TimeUtil.parseTextTime(it) } ?: 60

        if (actualTime !in 1..2592000) {
            subject.sendMessage("请输入合理的禁言时间, 范围在 [1秒, 30天]".toMessageWrapper())
            return
        }

        try {
            target.mute(actualTime)
            subject.sendMessage("禁言成功".toMessageWrapper())
        } catch (e: Exception) {
            subject.sendMessage("禁言失败, 可能是 Bot 网炸了捏".toMessageWrapper())
            logger.warn(e) { "禁言 ${target.platform} 平台中群聊 ${target.group.id} 的成员 ${target.id} 失败" }
        }
    }
}
