package io.github.starwishsama.comet.commands.subcommands.console

import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.commands.CommandProps
import io.github.starwishsama.comet.commands.MessageHandler
import io.github.starwishsama.comet.commands.interfaces.ConsoleCommand
import io.github.starwishsama.comet.enums.UserLevel
import io.github.starwishsama.comet.sessions.SessionManager
import io.github.starwishsama.comet.utils.BotUtil
import kotlin.time.ExperimentalTime

class DebugCommand : ConsoleCommand {
    @ExperimentalTime
    override suspend fun execute(args: List<String>): String {
        if (args.isNotEmpty()) {
            when (args[0]) {
                "sessions" -> {
                    val sb = StringBuilder("目前活跃的会话列表: \n")
                    val sessions = SessionManager.getSessions()
                    if (sessions.isEmpty()) {
                        sb.append("无")
                    } else {
                        var i = 1
                        for (session in sessions) {
                            sb.append(i + 1).append(" ").append(session.key.toString()).append("\n")
                            i++
                        }
                    }
                    return sb.toString().trim()
                }
                "info" ->
                    return ("彗星 Bot ${BotVariables.version}\n已注册的命令个数: ${MessageHandler.countCommands()}\n${BotUtil.getMemoryUsage()}")
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

    override fun getProps(): CommandProps = CommandProps("debug", null, "", "", UserLevel.CONSOLE)
}