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

import cn.hutool.core.util.RandomUtil

import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.objects.BotUser
import io.github.starwishsama.comet.objects.RandomResult
import io.github.starwishsama.comet.utils.CometUtil
import io.github.starwishsama.comet.utils.CometUtil.getRestString
import io.github.starwishsama.comet.utils.StringUtil.convertToChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain


class DivineCommand : ChatCommand {
    override suspend fun execute(event: MessageEvent, args: List<String>, user: BotUser): MessageChain {
        return if (args.isNotEmpty()) {
            if (user.commandTime > 0 || user.level != UserLevel.USER) {
                if (args.isEmpty()) return CometUtil.toChain("请检查需要占卜的字符是否超过上限或为空!")

                val randomEventName = args.getRestString(0)
                if (randomEventName.isNotBlank() && randomEventName.length < 30) {
                    val result = RandomResult(-1000, RandomUtil.randomDouble(0.0, 1.0), randomEventName)
                    user.decreaseTime()
                    RandomResult.getChance(result).convertToChain()
                } else {
                    CometUtil.toChain("请检查需要占卜的字符是否超过上限或为空!")
                }
            } else {
                CometUtil.toChain("今日命令条数已达上限, 请等待条数自动恢复哦~\n命令条数现在每小时会恢复100次, 封顶1000次")
            }
        } else {
            return getHelp().convertToChain()
        }
    }

    override fun getProps(): CommandProps =
        CommandProps("divine", arrayListOf("zb", "占卜"), "占卜命令", "nbot.commands.divine", UserLevel.USER)

    override fun getHelp(): String = """
         ======= 命令帮助 =======
         /zb [占卜内容] 占卜
    """.trimIndent()

    private fun getResultFromList(results: List<RandomResult>, id: Long): RandomResult? {
        if (results.isNotEmpty()) {
            for (result in results) {
                if (result.id == id) {
                    return result
                }
            }
        }
        return null
    }
}
