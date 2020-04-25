package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.BotConstants
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.objects.checkin.CheckInData
import io.github.starwishsama.nbot.util.BotUtils
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.asMessageChain
import net.mamoe.mirai.message.data.toMessage
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AdminCommand : UniversalCommand {
    val commands = arrayOf("checkin")
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (message is GroupMessage && BotUser.isBotOwner(message.sender.id)){
            when (args[0]) {
                "clockin", "dk", "打卡" -> {
                    if (args.size == 3 && args[1].isNotEmpty() && args[2].isNotEmpty()){
                        if (BotConstants.checkInCalendar.isEmpty() || BotConstants.checkInCalendar.containsKey(message.group.id)){
                            val timeNow = LocalDateTime.now()
                            val startTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(args[2], BotConstants.dateFormatter))
                            val endTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(args[3], BotConstants.dateFormatter))
                            val usersList = arrayListOf<Member>()

                            return if (startTime.isBefore(timeNow) || endTime.isBefore(timeNow)){
                                (BotUtils.getLocalMessage("msg.bot-prefix") + "在吗 为什么时间穿越").toMessage()
                                    .asMessageChain()
                            } else {
                                for (member in message.group.members) {
                                    usersList.add(member)
                                }

                                BotConstants.checkInCalendar[message.group.id] =
                                    CheckInData(startTime, endTime, usersList)
                                (BotUtils.getLocalMessage("msg.bot-prefix") + "打卡已开启 请发送 /dk 来打卡").toMessage()
                                    .asMessageChain()
                            }
                        } else {
                            message.quoteReply(BotUtils.getLocalMessage("msg.bot-prefix") + "该群还有一个未完成的签到!")
                        }
                    } else if (args.size == 2 && args[1].isNotEmpty()){
                        if (BotConstants.checkInCalendar.isEmpty() || BotConstants.checkInCalendar.containsKey(message.group.id)){
                            val startTime = LocalDateTime.now()
                            val endTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(args[2], BotConstants.dateFormatter))
                            val usersList = arrayListOf<Member>()

                            return if (endTime.isBefore(startTime)){
                                (BotUtils.getLocalMessage("msg.bot-prefix") + "在吗 为什么时间穿越").toMessage()
                                    .asMessageChain()
                            } else {
                                for (member in message.group.members) {
                                    usersList.add(member)
                                }

                                BotConstants.checkInCalendar[message.group.id] =
                                    CheckInData(startTime, endTime, usersList)
                                (BotUtils.getLocalMessage("msg.bot-prefix") + "打卡已开启 请发送 /dk 来打卡").toMessage()
                                    .asMessageChain()
                            }
                        } else {
                            message.quoteReply(BotUtils.getLocalMessage("msg.bot-prefix") + "该群还有一个未完成的签到!")
                        }
                    }
                }
                "stopclock", "关闭打卡", "gbdk" -> {
                    if (BotConstants.checkInCalendar.isEmpty() || !BotConstants.checkInCalendar.containsKey(message.group.id)){
                        return (BotUtils.getLocalMessage("msg.bot-prefix") + "本群没有正在进行的打卡").toMessage().asMessageChain()
                    } else {
                        val data = BotConstants.checkInCalendar[message.group.id]
                        val checkedCount = data?.checkedUsers?.size
                        var lateText = StringBuilder()
                        var unCheckedText = StringBuilder()

                        val unChecked = data?.groupUsers?.minus(data.checkedUsers)?.minus(data.lateUsers)

                        unChecked?.forEach { member ->
                            run {
                                unCheckedText.append(member.nameCardOrNick).append(",")
                            }
                            unCheckedText.removeSuffix(",")
                        }

                        data?.lateUsers?.forEach { member ->
                            run {
                                lateText.append(member.nameCardOrNick).append(",")
                            }
                            lateText.removeSuffix(",")
                        }

                        if (lateText.toString().isEmpty()){
                            lateText = StringBuilder("无")
                        }

                        if (unCheckedText.toString().isEmpty()){
                            unCheckedText = StringBuilder("无")
                        }

                        BotConstants.checkInCalendar.remove(message.group.id)
                        return "${BotUtils.getLocalMessage("msg.bot-prefix")}打卡已关闭\n已打卡人数: $checkedCount\n迟到: $lateText\n未打卡: $unCheckedText".toMessage().asMessageChain()
                    }
                }
                "help", "帮助" -> {
                    return getHelp().toMessage().asMessageChain()
                }
                else -> return (BotUtils.getLocalMessage("msg.bot-prefix") + "命令不存在, 使用 /admin help 查看更多").toMessage().asMessageChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps = CommandProps("admin", arrayListOf("管理", "管", "gl"), "nbot.commands.admin", UserLevel.ADMIN)

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /admin dk [结束时间] 创建一个打卡
        /admin gbdk 结束一个正在进行的打卡
    """.trimIndent()

}