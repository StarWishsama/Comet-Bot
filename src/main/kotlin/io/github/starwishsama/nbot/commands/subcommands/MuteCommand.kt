package io.github.starwishsama.nbot.commands.subcommands

import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtil
import io.github.starwishsama.nbot.util.BotUtil.toMirai
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.contact.mute
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.GroupMessage
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.isContentNotEmpty
import org.apache.commons.lang3.StringUtils

class MuteCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.userQQ) && message is GroupMessage && (user.isBotAdmin() || message.sender.isOperator())) {
            if (message.group.botPermission.isOperator()) {
                if (args.isNotEmpty()) {
                    try {
                        val at = message[At]
                        if (at.isContentNotEmpty()) {
                            doMute(message.group, at.target, getMuteTime(args[1]), false)
                        }
                    } catch (e: NoSuchElementException) {
                        if (StringUtils.isNumeric(args[0])) {
                            doMute(message.group, args[0].toLong(), getMuteTime(args[1]), false)
                        } else if (args[0].contentEquals("all")){
                            doMute(message.group, 0, 0, true)
                        }
                    }
                } else {
                    return getHelp().toMirai()
                }
            } else {
                BotUtil.sendLocalMessage("msg.bot-prefix", "我不是绿帽 我爬 我爬").toMirai()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("mute", arrayListOf("jy", "禁言"), "禁言", "nbot.commands.mute", UserLevel.ADMIN)

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /mute [@/Q/all] [禁言时长]
        时长为 0 时解禁
    """.trimIndent()

    private suspend fun doMute(group: Group, id: Long, muteTime: Long, isAll: Boolean): MessageChain {
        try {
            if (isAll) {
                group.settings.isMuteAll = !group.settings.isMuteAll
                return if (group.settings.isMuteAll) {
                    BotUtil.sendLocalMessage("msg.bot-prefix", "The World!").toMirai()
                } else {
                    BotUtil.sendLocalMessage("msg.bot-prefix", "然后时间开始流动").toMirai()
                }
            } else {
                group.members.forEach { member ->
                    run {
                        if (member.id == id) {
                            return when (muteTime) {
                                in 1..2592000 -> {
                                    member.mute(muteTime)
                                    BotUtil.sendLocalMessage("msg.bot-prefix", "禁言成功").toMirai()
                                }
                                0L -> {
                                    member.unmute()
                                    BotUtil.sendLocalMessage("msg.bot-prefix", "解禁成功").toMirai()
                                }
                                else -> {
                                    BotUtil.sendLocalMessage("msg.bot-prefix", "禁言时间有误, 范围: (0s, 30days]").toMirai()
                                }
                            }
                        }
                    }
                }
            }

            return BotUtil.sendLocalMessage("msg.bot-prefix", "找不到此用户").toMirai()
        } catch (e: PermissionDeniedException){
            return BotUtil.sendLocalMessage("msg.bot-prefix", "我不是绿帽 我爬 我爬").toMirai()
        }
    }

    private fun getMuteTime(message: String) : Long {
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
        } else if (tempTime.contains("钟")) {
            banTime += tempTime.substring(0, tempTime.indexOf("钟")).toInt() * 60
        }
        return banTime
    }
}