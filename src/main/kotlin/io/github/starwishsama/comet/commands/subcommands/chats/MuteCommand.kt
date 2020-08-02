package io.github.starwishsama.comet.commands.subcommands.chats

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.group.PerGroupConfig
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.isNumeric
import io.github.starwishsama.comet.utils.toMsgChain
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.isContentNotEmpty

class MuteCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.id) && event is GroupMessageEvent) {
            if (event.group.botPermission.isOperator()) {
                if (hasPermission(user.id, GroupConfigManager.getConfigSafely(event.group.id), event.sender.permission)) {
                    if (args.isNotEmpty()) {
                        val at = event.message[At]
                        if (at != null && at.isContentNotEmpty()) {
                            doMute(event.group, at.target, getMuteTime(args[1]), false)
                        } else {
                            if (args[0].isNumeric()) {
                                doMute(event.group, args[0].toLong(), getMuteTime(args[1]), false)
                            } else {
                                when (args[0]) {
                                    "all", "全体", "全禁", "全体禁言" -> doMute(
                                        event.group,
                                        args[0].toLong(),
                                        getMuteTime(args[1]),
                                        false
                                    )
                                    "random", "rand", "随机", "抽奖" -> doRandomMute(event)
                                }
                            }
                        }
                    } else {
                        return getHelp().toMsgChain()
                    }
                } else {
                    BotUtil.sendMsgPrefix("你不是绿帽 你爬 你爬").toMsgChain()
                }
            } else {
                BotUtil.sendMsgPrefix("我不是绿帽 我爬 我爬").toMsgChain()
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
                    BotUtil.sendMsgPrefix("The World!").toMsgChain()
                } else {
                    BotUtil.sendMsgPrefix("然后时间开始流动").toMsgChain()
                }
            } else {
                if (group.botAsMember.id == id) BotUtil.sendMsgPrefix("不能踢出机器人").toMsgChain()

                for (member in group.members) {
                    if (member.id == id) {
                        if (member.isOperator()) BotUtil.sendMsgPrefix("不能踢出管理员").toMsgChain()
                        return when (muteTime) {
                            in 1..2592000 -> {
                                member.mute(muteTime)
                                BotUtil.sendMsgPrefix("禁言成功").toMsgChain()
                            }
                            0L -> {
                                member.unmute()
                                BotUtil.sendMsgPrefix("解禁成功").toMsgChain()
                            }
                            else -> {
                                BotUtil.sendMsgPrefix("禁言时间有误, 可能是格式错误, 范围: (0s, 30days]").toMsgChain()
                            }
                        }
                    }
                }
            }

            return BotUtil.sendMsgPrefix("找不到此用户").toMsgChain()
        } catch (e: PermissionDeniedException) {
            return BotUtil.sendMsgPrefix("我不是绿帽 我爬 我爬").toMsgChain()
        }
    }

    /**
     * 这段代码看起来很神必
     * 但是 It just works.
     */
    private fun getMuteTime(message: String): Long {
        var banTime = 0L
        var tempTime: String = message
        if (tempTime.indexOf('d') != -1) {
            banTime += (tempTime.substring(0, tempTime.indexOf('d')).toInt() * 24
                    * 60 * 60)
            tempTime = tempTime.substring(tempTime.indexOf('d') + 1)
        } else if (tempTime.contains("天")) {
            banTime += (tempTime.substring(0, tempTime.indexOf('天')).toInt() * 24
                    * 60 * 60)
            tempTime = tempTime.substring(tempTime.indexOf('天') + 1)
        }
        if (tempTime.indexOf('h') != -1) {
            banTime += (tempTime.substring(0, tempTime.indexOf('h')).toInt() * 60
                    * 60)
            tempTime = tempTime.substring(tempTime.indexOf('h') + 1)
        } else if (tempTime.contains("小时")) {
            banTime += (tempTime.substring(0, tempTime.indexOf("时")).toInt() * 60
                    * 60)
            tempTime = tempTime.substring(tempTime.indexOf("时") + 1)
        }
        if (tempTime.indexOf('m') != -1) {
            banTime += tempTime.substring(0, tempTime.indexOf('m')).toInt() * 60
        } else if (tempTime.contains("分钟")) {
            banTime += tempTime.substring(0, tempTime.indexOf("分钟")).toInt() * 60
        }
        return banTime
    }

    private fun hasPermission(id: Long, cfg: PerGroupConfig, permission: MemberPermission): Boolean {
        return permission >= MemberPermission.ADMINISTRATOR || cfg.isHelper(id)
    }
}