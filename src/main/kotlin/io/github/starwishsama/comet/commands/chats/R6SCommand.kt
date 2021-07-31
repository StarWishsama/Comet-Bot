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
import io.github.starwishsama.comet.api.thirdparty.rainbowsix.R6StatsApi
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.IDGuidelineType
import io.github.starwishsama.comet.utils.StringUtil.isLegitId
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.at

class R6SCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isEmpty()) {
            return toChain(getHelp(), true)
        } else {
            when (args[0].lowercase()) {
                "info", "查询", "cx" -> {
                    val account = user.r6sAccount

                    return if (args.size <= 1) {
                        if (account.isNotEmpty()) {
                            event.subject.sendMessage(toChain("查询中..."))
                            val result = R6StatsApi.getPlayerStat(account)
                            val resultText = "\n" + result.toMessageChain(event.subject)
                            if (event is GroupMessageEvent) event.sender.at() + resultText else resultText.toChain(false)
                        } else {
                            "你还未绑定育碧账号, 输入 /r6s bind [账号] 绑定快速查询战绩".toChain()
                        }
                    } else {
                        if (isLegitId(args[1], IDGuidelineType.UBISOFT)) {
                            event.subject.sendMessage(toChain("查询中..."))
                            val result = R6StatsApi.getPlayerStat(args[1])
                            val resultText = "\n" + result.toMessageChain(event.subject)
                            if (event is GroupMessageEvent) event.sender.at() + resultText else resultText.toChain(false)
                        } else {
                            toChain("你输入的 ID 不符合育碧用户名规范!")
                        }
                    }
                }
                "bind", "绑定" ->
                    if (args[1].isNotEmpty() && args.size > 1) {
                        return if (isLegitId(args[1], IDGuidelineType.UBISOFT)) {
                            user.r6sAccount = args[1]
                            toChain("绑定成功!")
                        } else {
                            toChain("ID 格式有误!")
                        }
                    }
                else -> {
                    return toChain("/r6s info [Uplay账号名]")
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps =
        CommandProps("r6", arrayListOf("r6s", "彩六"), "彩虹六号数据查询", "nbot.commands.r6s", UserLevel.USER)

    override fun getHelp(): String = """
        /r6 info [Uplay账号名] 查询战绩
        /r6 bind [Uplay账号名] 绑定账号
        /r6 info 查询战绩 (需要绑定账号)
    """.trimIndent()
}