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

import io.github.starwishsama.comet.CometVariables.cfg
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.managers.GroupConfigManager
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.config.PerGroupConfig
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.utils.CometUtil.toChain
import net.mamoe.mirai.contact.isOperator
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

class FilterCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
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

    override val props: CommandProps = CommandProps(
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

    override fun hasPermission(user: CometUser, e: MessageEvent): Boolean {
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