/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.commands.chats

import cn.hutool.core.util.RandomUtil
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ChatCommand
import io.github.starwishsama.comet.objects.CometUser
import io.github.starwishsama.comet.objects.enums.UserLevel
import io.github.starwishsama.comet.utils.CometUtil.toChain
import io.github.starwishsama.comet.utils.StringUtil.isNumeric
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import java.util.regex.Pattern

object DiceCommand : ChatCommand {
    // 骰子正则表达式
    private val dicePattern = Pattern.compile("""(\d{1,2})([dD])(\d{1,4})""")

    override suspend fun execute(event: MessageEvent, args: List<String>, user: CometUser): MessageChain {
        if (args.isEmpty()) {
            return getHelp().toChain()
        }

        val (count, size) = parseInput(args[0])

        if (count < 1 || size < 1) {
            return "骰子格式错误! 格式示例: 1d100".toChain()
        }

        val result = doDice(count, size)

        return "结果: ${args[0]}=${result.convertToString()}".toChain()
    }

    override val props: CommandProps =
        CommandProps(
            "dice",
            listOf("tz", "骰子"),
            "投骰子",

            UserLevel.USER
        )

    override fun getHelp(): String = """
        /dice [指定方法] 投骰子
        
        如: /dice 1d100
        注意: 骰子最大只能投 10 个, 1000 面.
    """.trimIndent()

    private fun parseInput(input: String): Pair<Int, Int> {
        val matcher = dicePattern.matcher(input)

        if (!Pattern.matches(dicePattern.pattern(), input)) {
            return Pair(0, 0)
        }

        var diceTime = 0
        var diceSize = 0

        while (matcher.find()) {
            val time = matcher.group(1)
            val d = matcher.group(2)
            val size = matcher.group(3)

            diceTime = if (time.isNumeric()) {
                val parseTime = time.toIntOrNull() ?: return Pair(0, 0)
                if (parseTime in 1..10) {
                    parseTime
                } else {
                    return Pair(0, 0)
                }
            } else if (time.isEmpty() && d.lowercase() == "d") {
                1
            } else {
                0
            }

            diceSize = if (size.isNumeric()) {
                val parseDiceSize = size.toIntOrNull() ?: return Pair(0, 0)
                if (parseDiceSize <= 1000) {
                    parseDiceSize
                } else {
                    return Pair(0, 0)
                }
            } else {
                0
            }
        }

        return Pair(diceTime, diceSize)
    }

    private fun doDice(time: Int, base: Int): List<DiceResult> {
        val results = mutableListOf<DiceResult>()

        repeat(time) {
            val result = RandomUtil.randomInt(0, base)

            results.add(DiceResult(base, result))
        }

        return results
    }

    data class DiceResult(
        val base: Int,
        val result: Int
    )

    private fun List<DiceResult>.convertToString(): String {
        return buildString {
            var count = 0

            this@convertToString.forEach {
                append("d${it.base}(${it.result.also { result -> count += result }})")
                append("+")
            }

            delete(length - 1, length)

            append("=$count")
        }
    }
}
