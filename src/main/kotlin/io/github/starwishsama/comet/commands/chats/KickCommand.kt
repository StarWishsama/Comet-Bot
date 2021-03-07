package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

@CometCommand
class KickCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (event is GroupMessageEvent) {
            if (hasPermission(user, event)) {
                if (event.group.botPermission.isOperator()) {
                    if (args.isNotEmpty()) {
                        val at = CometUtil.parseAtToId(event, args[0])
                        if (at > -1) {
                            doKick(event, at, "")
                        } else {
                            getHelp().convertToChain()
                        }
                    } else {
                        return getHelp().convertToChain()
                    }
                } else {
                    CometUtil.toChain("我不是绿帽 我爬 我爬")
                }
            } else {
                CometUtil.toChain("你不是绿帽 你爬 你爬")
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
            CommandProps("kick", arrayListOf("tr", "踢人"), "踢人", "nbot.commands.kick", UserLevel.USER)

    override fun getHelp(): String = """
        ======= 命令帮助 =======
        /kick [@/Q] (理由) 踢出一名非管理群员
    """.trimIndent()

    override fun hasPermission(user: BotUser, e: MessageEvent): Boolean {
        if (e is GroupMessageEvent) {
            if (e.sender.permission == MemberPermission.MEMBER) return false
            val cfg = GroupConfigManager.getConfigOrNew(e.group.id)
            if (cfg.isHelper(user.id)) return true
        }

        return user.hasPermission(getProps().permission)
    }

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
}