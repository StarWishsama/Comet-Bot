package io.github.starwishsama.comet.commands.subcommands.chats

import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.group.PerGroupConfig
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.isNumeric
import io.github.starwishsama.comet.utils.toMsgChain
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.isContentNotEmpty

class KickCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (BotUtil.isNoCoolDown(user.id) && event is GroupMessageEvent) {
            if (event.group.botPermission.isOperator()) {
                if (hasPermission(user.id, GroupConfigManager.getConfigSafely(event.group.id), event.sender.permission)) {
                    if (args.isNotEmpty()) {
                        val at = event.message[At]
                        if (at != null && at.isContentNotEmpty()) {
                            doKick(event, at.target, "")
                        } else {
                            if (args[0].isNumeric()) {
                                doKick(event, args[0].toLong(), "")
                            } else {
                                getHelp().toMsgChain()
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
        /kick [@/Q] (理由) 踢出一名非管理群员
    """.trimIndent()

    private fun doKick(event: GroupMessageEvent, target: Long, reason: String) {
        event.group.members.forEach {
            if (it.id == target) {
                runBlocking {
                    if (reason.isNotBlank()) it.kick(reason)
                    else it.kick("管理员操作")
                }
            }
        }
    }

    private fun hasPermission(id: Long, cfg: PerGroupConfig, permission: MemberPermission): Boolean {
        return permission >= MemberPermission.ADMINISTRATOR || cfg.isHelper(id)
    }
}