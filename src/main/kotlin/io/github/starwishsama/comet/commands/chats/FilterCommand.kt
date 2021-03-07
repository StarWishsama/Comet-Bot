package io.github.starwishsama.comet.commands.chats

import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class FilterCommand: ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        if (args.isEmpty()) return getHelp().toChain()
        return when (args[0]) {
            "add", "tj", "添加", "加" -> {
                handleAddFilterWord(args.subList(1, args.size), event)
            }
            "remove", "del", "delete", "sc", "yc", "删除", "删" -> {
                TODO("删除逻辑未完成")
            }
            "list", "列表" -> {
                TODO("展示列表逻辑未完成")
            }
            else -> getHelp().toChain()
        }
    }

    override fun getProps(): CommandProps = CommandProps(
        "filter",
        listOf("屏蔽", "glq", "pb"),
        "加/删机器人禁止发送的词汇",
        "nbot.commands.filter",
        UserLevel.ADMIN
    )

    override fun getHelp(): String = """
        /filter add(tj/添加/加) [屏蔽词] 添加屏蔽词
        /filter remove(del/delete/sc/yc/删除/删) [屏蔽词] 删除屏蔽词
        /filter list(列表)
        
        []内为必填内容 ()内子命令的其他形式
        该命令亦可使用 /屏蔽 /glq /pb 调用
    """.trimIndent()

    override fun hasPermission(user: BotUser, e: MessageEvent): Boolean {
        if (e is GroupMessageEvent) {
            return e.sender.isOperator() || super.hasPermission(user, e)
        }

        return super.hasPermission(user, e)
    }

    private fun handleAddFilterWord(words: List<String>, event: MessageEvent): MessageChain {
        val counter: Int = if (event is GroupMessageEvent) {
            doAddWord(words, GroupConfigManager.getConfigOrNew(event.group.id))
        } else {
            doAddWord(words, null)
        }

        return "成功添加该群新的屏蔽词${if (counter > 0) ", 有${counter}个添加失败" else ""}".toChain()
    }

    private fun doAddWord(words: List<String>, groupCfg: PerGroupConfig?): Int {
        var counter = 0
        if (groupCfg == null) {
            words.forEach { word ->
                if (!cfg.filterWords.add(word)) {
                    counter++
                }
            }
        } else {
            words.forEach { word ->
                if (groupCfg.groupFilterWords.add(word)) {
                    counter++
                }
            }
        }
        return counter
    }
}