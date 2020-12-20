package io.github.starwishsama.comet.commands.console

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.BotVariables.bot
import io.github.starwishsama.comet.Versions
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.BotUtil
import io.github.starwishsama.comet.utils.RuntimeUtil
import kotlin.time.ExperimentalTime

@CometCommand
class DebugCommand : ConsoleCommand {
    @ExperimentalTime
    override suspend fun execute(args: List<String>): String {
        if (args.isNotEmpty()) {
            when (args[0]) {
                "sessions" -> {
                    val sessions = SessionManager.getSessions()
                    return buildString {
                        append("目前活跃的会话列表: \n")
                        if (sessions.isEmpty()) {
                            append("无")
                            return@buildString
                        }
                        var i = 1
                        sessions.forEach { (s, _) ->
                            append(i + 1).append(" ").append(s.toString()).append("\n")
                            i++
                        }
                    }.trim()
                }
                "info" ->
                    return ("彗星 Bot ${Versions.version}\n" +
                            "Comet 状态: ${bot.isOnline} | ${BotVariables.switch}\n" +
                            "已注册命令数: ${CommandExecutor.countCommands()}\n" +
                            BotUtil.getMemoryUsage() + "\n" +
                            "CPU 负载: ${RuntimeUtil.getOperatingSystemBean().systemLoadAverage}\n" +
                            "构建时间: ${Versions.buildTime}"
                            )
                "switch" -> {
                    BotVariables.switch = !BotVariables.switch
                    return "Bot > 维护模式已${if (!BotVariables.switch) "开启" else "关闭"}"
                }
                else -> return getHelp()
            }
        }
        return ""
    }

    override fun getProps(): CommandProps = CommandProps("debug", mutableListOf(), "", "", UserLevel.CONSOLE)

    override fun getHelp(): String = "请自行查阅代码"
}