/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.cli

import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.CommandResult
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException
import ren.natsuyuk1.comet.api.command.CommandManager
import ren.natsuyuk1.comet.api.command.ConsoleCommandSender
import ren.natsuyuk1.comet.api.user.CometUser
import ren.natsuyuk1.comet.cli.console.Console
import ren.natsuyuk1.comet.config.branch
import ren.natsuyuk1.comet.config.hash
import ren.natsuyuk1.comet.config.version
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.jvm.addShutdownHook
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

private val logger = mu.KotlinLogging.logger {}

object CometTerminal {
    private var scope = ModuleScope("CometTerminal")

    fun init(parentContext: CoroutineContext) {
        scope = ModuleScope("CometTerminal", parentContext)
        CommandManager.init(scope.coroutineContext)
    }
}

class CometTerminalCommand : CliktCommand(name = "comet") {
    override suspend fun run() = scope.launch {
        setupShutdownHook()

        logger.info { "Running Comet Terminal ${version}-${branch}-${hash}" }

        CometTerminal.init(scope.coroutineContext)

        // Load config

        // Load database

        setupConsole()
    }.join()

    private fun setupConsole() = scope.launch {
        val consoleSender = ConsoleCommandSender(ModuleScope("ConsoleCommandSenderScope"))
        Console.initReader()
        Console.redirectToJLine()

        while (scope.isActive) {
            try {
                CommandManager.executeCommand(consoleSender, CometUser(EntityID(1L, LongIdTable())), Console.readln())
                    .join()
            } catch (e: UserInterruptException) { // Ctrl + C
                println("<Interrupted> use 'quit' command to exit process")
            } catch (e: EndOfFileException) { // Ctrl + D
                exitProcess(0)
            }
        }
    }

    private fun setupShutdownHook() {
        addShutdownHook {
            closeAll()
            println("\nExiting Comet Terminal...")
            Console.redirectToNull()
        }
    }

    companion object {
        private val scope = ModuleScope("CometFrontendRootScope")

        internal fun closeAll() {
            scope.dispose()
            scope.cancel()
        }
    }
}

suspend fun main(args: Array<String>) {
    val cmd = CometTerminalCommand()

    when (val result = cmd.main(args)) {
        is CommandResult.Success -> {
            exitProcess(0)
        }
        is CommandResult.Error -> {
            println(result.userMessage)
            exitProcess(1)
        }
    }
}
