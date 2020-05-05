package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.checkin.CheckInData
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.*
import org.apache.commons.lang3.StringUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AdminCommand : UniversalCommand {
    val commands = arrayOf("checkin")
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (user.isBotAdmin()) {
            if (args.isEmpty()) {
                return (BotUtil.getLocalMessage("msg.bot-prefix") + "命令不存在, 使用 /admin help 查看更多").toMessage()
                        .asMessageChain()
            } else {
                when (args[0]) {
                    "clockin", "dk", "打卡" -> {
                        return if (message is GroupMessage) {
                            clockIn(args, message)
                        } else {
                            BotUtil.sendMsgPrefix("该命令只能在群聊使用").toMirai()
                        }
                    }
                    "stopclock", "关闭打卡", "gbdk" -> {
                        return if (message is GroupMessage) {
                            val data = BotConstants.checkInCalendar[message.group.id]
                            data?.unregister(message.group.id) ?: BotUtil.sendMsgPrefix("本群没有正在进行的打卡").toMirai()
                        } else {
                            BotUtil.sendMsgPrefix("该命令只能在群聊使用").toMirai()
                        }
                    }
                    "help", "帮助" -> {
                        return getHelp().toMirai()
                    }
                    "permadd", "添加权限", "tjqx" -> {
                        if (user.isBotOwner()) {
                            if (args.size > 1) {
                                user.addPermission(args[1])
                                return BotUtil.sendMsgPrefix("添加权限成功").toMirai()
                            }
                        } else {
                            return BotUtil.sendMsgPrefix("你没有权限").toMirai()
                        }
                    }
                    "give", "增加次数" -> {
                        if (args.size > 1) {
                            val target: BotUser? = try {
                                val at = message[At]
                                BotUser.getUser(at.target)
                            } catch (e: NoSuchElementException) {
                                if (StringUtils.isNumeric(args[1])) {
                                    BotUser.getUser(args[1].toLong())
                                } else {
                                    return BotUtil.sendLocalMessage("msg.bot-prefix", "给予的次数超过上限").toMirai()
                                }
                            }

                            return if (target != null) {
                                if (args[2].toInt() <= 1000000) {
                                    target.addTime(args[2].toInt())
                                    BotUtil.sendMsgPrefix("成功为 $target 添加 ${args[2]} 次命令条数").toMirai()
                                } else {
                                    BotUtil.sendLocalMessage("msg.bot-prefix", "给予的次数超过上限").toMessage()
                                            .asMessageChain()
                                }
                            } else {
                                BotUtil.sendLocalMessage("msg.bot-prefix", "找不到此用户").toMirai()
                            }
                        }
                    }
                    else -> return (BotUtil.getLocalMessage("msg.bot-prefix") + "命令不存在, 使用 /admin help 查看更多").toMirai()
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
            CommandProps("admin", arrayListOf("管理", "管", "gl"), "机器人管理员命令", "nbot.commands.admin", UserLevel.ADMIN)

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /admin dk [结束时间] 创建一个打卡
        /admin gbdk 结束一个正在进行的打卡
    """.trimIndent()

    private suspend fun clockIn(args: List<String>, message: GroupMessage): MessageChain {
        if (BotConstants.checkInCalendar.isEmpty() || BotConstants.checkInCalendar.containsKey(message.group.id)) {
            val startTime: LocalDateTime
            val endTime: LocalDateTime

            when (args.size) {
                3 -> {
                    startTime = LocalDateTime.of(
                            LocalDate.now(),
                            LocalTime.parse(args[1], BotConstants.dateFormatter)
                    )
                    endTime = LocalDateTime.of(
                            LocalDate.now(),
                            LocalTime.parse(args[2], BotConstants.dateFormatter)
                    )
                }
                2 -> {
                    startTime = LocalDateTime.now()
                    endTime = LocalDateTime.of(
                            LocalDate.now(),
                            LocalTime.parse(args[1], BotConstants.dateFormatter)
                    )
                }
                else -> {
                    return BotUtil.sendMsgPrefix("/admin dk (开始时间) [结束时间])").toMirai()
                }
            }

            val usersList = arrayListOf<Member>()

            return if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
                BotUtil.sendMsgPrefix("在吗 为什么时间穿越").toMirai()
            } else {
                for (member in message.group.members) {
                    usersList.add(member)
                }

                BotConstants.checkInCalendar[message.group.id] =
                        CheckInData(startTime, endTime, usersList)
                BotUtil.sendMsgPrefix("打卡已开启 请发送 /dk 来打卡").toMirai()
            }
        } else {
            message.quoteReply(BotUtil.sendMsgPrefix("该群还有一个未完成的签到!"))
        }
        return EmptyMessageChain
    }
}