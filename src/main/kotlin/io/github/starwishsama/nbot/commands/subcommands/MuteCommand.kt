package io.github.starwishsama.nbot.commands.subcommands

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.utils.BotUtil
import io.github.starwishsama.nbot.utils.isNumeric
import io.github.starwishsama.nbot.utils.toMirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.isContentNotEmpty
import org.apache.commons.lang3.StringUtils
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class MuteCommand : UniversalCommand {
    private val patternZH: DateTimeFormatter = DateTimeFormatter.ofPattern("HH时mm分ss秒")
    private val patternEN: DateTimeFormatter = DateTimeFormatter.ofPattern("HHhmmmsss")

    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.userQQ) && event is GroupMessageEvent) {
            if (event.group.botPermission.isOperator()) {
                if (user.isBotAdmin() || event.sender.isOperator()) {
                    if (args.isNotEmpty()) {
                        val at = event.message[At]
                        if (at != null) {
                            if (at.isContentNotEmpty()) {
                                doMute(event.group, at.target, getMuteTime(args[1]), false)
                            }
                        } else {

                            if (args[0].isNumeric()) {
                                doMute(event.group, args[0].toLong(), getMuteTime(args[1]), false)
                            } else {
                                when (args[0]) {
                                    "all", "全体", "全禁", "全体禁言" -> doMute(event.group, args[0].toLong(), getMuteTime(args[1]), false)
                                    "random", "rand", "随机", "抽奖" -> doRandomMute(event)
                                }
                            }

                            if (StringUtils.isNumeric(args[0])) {
                                doMute(event.group, args[0].toLong(), getMuteTime(args[1]), false)
                            } else if (args[0].contentEquals("all")) {
                                doMute(event.group, 0, 0, true)
                            }
                        }
                    } else {
                        return getHelp().toMirai()
                    }
                } else {
                    BotUtil.sendMsgPrefix("你不是绿帽 你爬 你爬").toMirai()
                }
            } else {
                BotUtil.sendMsgPrefix("我不是绿帽 我爬 我爬").toMirai()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
            CommandProps("mute", arrayListOf("jy", "禁言"), "禁言", "nbot.commands.mute", UserLevel.USER)

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /mute [@/Q/all] [禁言时长]
        时长为 0 时解禁
    """.trimIndent()

    private suspend fun doRandomMute(event: GroupMessageEvent) {
        val iterator = event.group.members.iterator()
        var runTime = 0
        var randomTime = RandomUtil.randomInt(0, event.group.members.size)
        var target: Long = -1
        while (iterator.hasNext()) {
            val member = iterator.next()
            if (runTime == randomTime) {
                if (member.isAdministrator()) {
                    randomTime++
                    continue
                }
                target = member.id
            }
            runTime++
        }
        doMute(event.group, target, RandomUtil.randomLong(1, 2592000), false)
    }

    private suspend fun doMute(group: Group, id: Long, muteTime: Long, isAll: Boolean): MessageChain {
        try {
            if (isAll) {
                group.settings.isMuteAll = !group.settings.isMuteAll
                return if (group.settings.isMuteAll) {
                    BotUtil.sendMsgPrefix("The World!").toMirai()
                } else {
                    BotUtil.sendMsgPrefix("然后时间开始流动").toMirai()
                }
            } else {
                group.members.forEach { member ->
                    run {
                        if (member.id == id) {
                            return when (muteTime) {
                                in 1..2592000 -> {
                                    member.mute(muteTime)
                                    BotUtil.sendMsgPrefix("禁言成功").toMirai()
                                }
                                0L -> {
                                    member.unmute()
                                    BotUtil.sendMsgPrefix("解禁成功").toMirai()
                                }
                                else -> {
                                    BotUtil.sendMsgPrefix("禁言时间有误, 可能是格式错误, 范围: (0s, 30days]").toMirai()
                                }
                            }
                        }
                    }
                }
            }

            return BotUtil.sendMsgPrefix("找不到此用户").toMirai()
        } catch (e: PermissionDeniedException){
            return BotUtil.sendMsgPrefix("我不是绿帽 我爬 我爬").toMirai()
        }
    }

    private fun getMuteTime(message: String) : Long {
        return try {
            val time = LocalTime.parse(message, patternZH)
            (time.toSecondOfDay() / 60).toLong()
        } catch (x: DateTimeParseException) {
            try {
                val time = LocalTime.parse(message, patternEN)
                (time.toSecondOfDay() / 60).toLong()
            } catch (x: DateTimeParseException) {
                -1
            }
        }
    }
}