package ren.natsuyuk1.comet.commands

import moe.sdl.yac.core.PrintMessage
import moe.sdl.yac.parameters.arguments.argument
import moe.sdl.yac.parameters.arguments.convert
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.command.CometCommand
import ren.natsuyuk1.comet.api.command.CommandProperty
import ren.natsuyuk1.comet.api.command.PlatformCommandSender
import ren.natsuyuk1.comet.api.message.MessageWrapper
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.util.toMessageWrapper

val RAND = CommandProperty(
    "random",
    listOf("rand", "随机"),
    "生成随机数",
    "/rand [表达式]"
)

class RandCommand(
    comet: Comet,
    override val sender: PlatformCommandSender,
    override val subject: PlatformCommandSender,
    val message: MessageWrapper,
    user: CometUser
) : CometCommand(comet, sender, subject, message, user, RAND) {
    private val exprHelp = """
        表达式 x y 皆为整数
        
        x..y 在 x 到 y 之中随机，包含 y
        
        x..<y 在 x 到 y 之中随机，不包含 y
    """.trimIndent()
    private val exprRegex = Regex("""^(?<x>[+-]?\d+)(?<operator>..<?)(?<y>[+-]?\d+)$""")
    private val expression by argument("EXPR", exprHelp).convert {
        it.trim()
    }

    override suspend fun run() {
        val syntaxError by lazy(LazyThreadSafetyMode.NONE) {
            PrintMessage(
                "表达式错误，输入 rand -h 查看用法。",
                error = true,
            )
        }
        val intError by lazy(LazyThreadSafetyMode.NONE) {
            PrintMessage(
                "表达式错误，数字区间需要为整数(int64)。",
                error = true,
            )
        }
        val match = exprRegex.matchEntire(expression)?.groups ?: throw syntaxError
        val x = (match["x"]?.value ?: throw syntaxError).toString().toLongOrNull() ?: throw intError
        val y = (match["y"]?.value ?: throw syntaxError).toString().toLongOrNull() ?: throw intError
        val op = (match["operator"]?.value ?: throw syntaxError).toString()
        val rand = when (op) {
            ".." -> (x..y).random()
            "..<" -> (x until y).random()
            else -> throw syntaxError
        }
        subject.sendMessage("结果为 $rand".toMessageWrapper())
    }
}
