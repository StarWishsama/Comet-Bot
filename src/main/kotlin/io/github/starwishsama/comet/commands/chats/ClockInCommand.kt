/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.ClockInManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.clock.ClockInData
import io.github.starwishsama.comet.objects.wrapper.MessageWrapper
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ClockInCommand : ChatCommand {
    private val hourMinuteFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (event is GroupMessageEvent) {
            if (args.isNotEmpty()) {
                when (args[0]) {
                    "create", "dk", "打卡" -> {
                        return createClockIn(args, event)
                    }
                    "showdata", "打卡数据", "dksj" -> {
                        return run {
                            val data = ClockInManager.getNearestClockIn(event.group.id)
                            data?.viewData()?.toMessageChain(event.subject)
                                ?: "本群没有正在进行的打卡".toChain()
                        }
                    }
                }
            } else {
                val id = event.group.id
                val data = ClockInManager.getNearestClockIn(id)
                return if (data != null) {
                    if (isClockIn(data, event)) {
                        doClockIn(event.sender, event, data)
                    } else {
                        "你已经打卡过了!".toChain()
                    }
                } else {
                    "没有正在进行的打卡".toChain()
                }
            }
        }
        return EmptyMessageChain
    }

    override val props: CommandProps =
        CommandProps("clockin", arrayListOf("打卡", "dk"), "打卡命令", "nbot.commands.clockin", UserLevel.USER)

    override fun getHelp(): String = "/dk 打卡"

    override fun hasPermission(user: CometUser, e: MessageEvent): Boolean = user.compareLevel(props.level)

    private fun isClockIn(data: ClockInData, event: GroupMessageEvent): Boolean {
        if (data.checkedUsers.isNotEmpty()) {
            data.checkedUsers.forEach { member ->
                run {
                    return member.id == event.sender.id
                }
            }
        }
        return false
    }

    private fun doClockIn(sender: Member, msg: GroupMessageEvent, data: ClockInData): MessageChain {
        val checkInTime = LocalDateTime.now()
        if (Duration.between(data.endTime, checkInTime).toMinutes() <= 5) {
            val result =
                MessageWrapper().addText(
                    "Bot > ${msg.sender.nameCardOrNick} 打卡成功!\n打卡时间: ${
                        checkInTime.format(
                            DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm:ss"
                            )
                        )
                    }"
                )
            result.addText(
                if (checkInTime.isAfter(data.endTime)) {
                    data.lateUsers.add(sender)
                    "\n签到状态: 迟到"
                } else {
                    "\n签到状态: 成功"
                }
            )

            data.checkedUsers.add(sender)
            return result.toMessageChain(null)
        } else {
            return data.viewData().toMessageChain(msg.subject)
        }
    }

    private fun createClockIn(args: List<String>, message: GroupMessageEvent): MessageChain {
        if (!ClockInManager.isDuplicate(message.group.id, 10)) {
            val startTime: LocalDateTime
            val endTime: LocalDateTime

            when (args.size) {
                3 -> {
                    startTime = LocalDateTime.of(
                        LocalDate.now(),
                        LocalTime.parse(args[1], hourMinuteFormatter)
                    )
                    endTime = LocalDateTime.of(
                        LocalDate.now(),
                        LocalTime.parse(args[2], hourMinuteFormatter)
                    )
                }
                2 -> {
                    startTime = LocalDateTime.now()
                    endTime = LocalDateTime.of(
                        LocalDate.now(),
                        LocalTime.parse(args[1], hourMinuteFormatter)
                    )
                }
                else -> {
                    return toChain("/admin dk (开始时间) [结束时间])")
                }
            }

            val usersList = arrayListOf<Member>()

            return if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
                toChain("在吗 为什么时间穿越")
            } else {
                for (member in message.group.members) {
                    usersList.add(member)
                }

                ClockInManager.newClockIn(message.group.id, usersList, startTime, endTime)
                toChain("打卡已开启 请发送 /dk 来打卡")
            }
        } else {
            return toChain("10 分钟内还有一个打卡未结束")
        }
    }
}