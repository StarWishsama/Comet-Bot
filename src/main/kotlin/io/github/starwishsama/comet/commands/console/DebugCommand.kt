package io.github.starwishsama.comet.commands.console

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.api.annotations.CometCommand
import io.github.starwishsama.comet.api.command.CommandExecutor
import io.github.starwishsama.comet.api.command.CommandProps
import io.github.starwishsama.comet.api.command.interfaces.ConsoleCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.BotUtil
import kotlin.time.ExperimentalTime

@CometCommand
class DebugCommand : ConsoleCommand {
    @ExperimentalTime
    override suspend fun execute(args: List<String>): String {
        if (args.isNotEmpty()) {
            when (args[0]) {
                "sessions" -> {
                    val sessions = SessionManager.getSessions()
                    return StringBuilder("目前活跃的会话列表: \n").apply {
                        if (sessions.isEmpty()) {
                            append("无")
                        } else {
                            var i = 1
                            for (session in sessions) {
                                append(i + 1).append(" ").append(session.key.toString()).append("\n")
                                i++
                            }
                        }
                    }.trim().toString()
                }
                "info" ->
                    return ("彗星 Bot ${BotVariables.version}\n" +
                            "今日もかわいい~\n" +
                            "已注册命令数: ${CommandExecutor.countCommands()}\n" +
                            BotUtil.getMemoryUsage() + "\n" +
                            "构建时间: ${BotVariables.buildTime}"
                            )
                "switch" -> {
                    BotVariables.switch = !BotVariables.switch

                    return if (!BotVariables.switch) {
                        "Bot > おつまち~"
                    } else {
                        "今日もかわいい!"
                    }
                }
            }
        }
        return ""
    }

    override fun getProps(): CommandProps = CommandProps("debug", mutableListOf(), "", "", UserLevel.CONSOLE)
}