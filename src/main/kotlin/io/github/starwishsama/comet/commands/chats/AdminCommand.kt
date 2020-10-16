package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.api.command.interfaces.UnDisableableCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.ClockInManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.asMessageChain
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@CometCommand
class AdminCommand : ChatCommand, UnDisableableCommand {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.hasNoCoolDown(event.sender.id)) {
            if (args.isEmpty()) {
                return (BotUtil.getLocalMessage("msg.bot-prefix") + "命令不存在, 使用 /admin help 查看更多").convertToChain()
            } else {
                when (args[0]) {
                    "clockin", "dk", "打卡" -> {
                        return if (event is GroupMessageEvent) {
                            clockIn(args, event)
                        } else {
                            BotUtil.sendMessage("该命令只能在群聊使用")
                        }
                    }
                    "showdata", "打卡数据", "dksj" -> {
                        return if (event is GroupMessageEvent) {
                            val data = ClockInManager.getNearestClockIn(event.group.id)
                            data?.viewData()?.text?.let { PlainText(it).asMessageChain() }
                                ?: BotUtil.sendMessage("本群没有正在进行的打卡")
                        } else {
                            BotUtil.sendMessage("该命令只能在群聊使用")
                        }
                    }
                    "help", "帮助" -> return getHelp().convertToChain()
                    "permlist", "权限列表", "qxlb" -> return permList(user, args, event)
                    "permadd", "添加权限", "tjqx" -> return permAdd(user, args, event)
                    "give", "增加次数" -> return giveCommandUseTime(event, args)
                    else -> return BotUtil.sendMessage("命令不存在, 使用 /admin help 查看更多")
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("admin", arrayListOf("管理", "管", "gl"), "机器人管理员命令", "nbot.commands.admin", UserLevel.ADMIN)

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /admin dk (开始时间) [结束时间] 创建一个打卡
        /admin dksj 查看最近一次打卡的数据
        /admin permadd [用户] [权限名] 给一个用户添加权限
        /admin give [用户] [命令条数] 给一个用户添加命令条数
    """.trimIndent()

    override fun hasPermission(user: BotUser, e: MessageEvent): Boolean {
        val bLevel = getProps().level
        if (user.compareLevel(bLevel)) return true
        if (e is GroupMessageEvent && e.sender.permission != MemberPermission.MEMBER) return true
        return false
    }

    private fun permList(user: BotUser, args: List<String>, event: MessageEvent): MessageChain {
        return if (args.size > 1) {
            val target: BotUser? = BotUtil.parseAtAsBotUser(event, args[1])
            val permission = target?.getPermissions()
            if (permission != null) {
                BotUtil.sendMessage(permission)
            } else {
                BotUtil.sendMessage("该用户没有任何权限")
            }
        } else {
            BotUtil.sendMessage(user.getPermissions())
        }
    }

    private fun permAdd(user: BotUser, args: List<String>, event: MessageEvent): MessageChain {
        if (user.isBotOwner()) {
            if (args.size > 1) {
                val target: BotUser? = BotUtil.parseAtAsBotUser(event, args[1])

                target?.addPermission(args[2])
                return BotUtil.sendMessage("添加权限成功")
            }
        } else {
            return BotUtil.sendMessage("你没有权限")
        }
        return EmptyMessageChain
    }

    private fun giveCommandUseTime(event: MessageEvent, args: List<String>): MessageChain {
        if (args.size > 1) {
            val target: BotUser? = BotUtil.parseAtAsBotUser(event, args[1])

            return if (target != null) {
                if (args[2].toInt() <= 1000000) {
                    target.addTime(args[2].toInt())
                    BotUtil.sendMessage("成功为 $target 添加 ${args[2]} 次命令条数")
                } else {
                    BotUtil.sendMessage("给予的次数超过系统限制上限")
                }
            } else {
                BotUtil.sendMessage("找不到此用户")
            }
        }
        return EmptyMessageChain
    }

    private fun clockIn(args: List<String>, message: GroupMessageEvent): MessageChain {
        if (!ClockInManager.isDuplicate(message.group.id, 10)) {
            val startTime: LocalDateTime
            val endTime: LocalDateTime

            when (args.size) {
                3 -> {
                    startTime = LocalDateTime.of(
                        LocalDate.now(),
                        LocalTime.parse(args[1], dateFormatter)
                    )
                    endTime = LocalDateTime.of(
                        LocalDate.now(),
                        LocalTime.parse(args[2], dateFormatter)
                    )
                }
                2 -> {
                    startTime = LocalDateTime.now()
                    endTime = LocalDateTime.of(
                        LocalDate.now(),
                        LocalTime.parse(args[1], dateFormatter)
                    )
                }
                else -> {
                    return BotUtil.sendMessage("/admin dk (开始时间) [结束时间])")
                }
            }

            val usersList = arrayListOf<Member>()

            return if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
                BotUtil.sendMessage("在吗 为什么时间穿越")
            } else {
                for (member in message.group.members) {
                    usersList.add(member)
                }

                ClockInManager.newClockIn(message.group.id, usersList, startTime, endTime)
                BotUtil.sendMessage("打卡已开启 请发送 /dk 来打卡")
            }
        } else {
            return BotUtil.sendMessage("10 分钟内还有一个打卡未结束")
        }
    }
}