/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.console

import kotlinx.coroutines.*
import moe.sdl.yac.core.CliktCommand
import moe.sdl.yac.core.CommandResult
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jline.reader.EndOfFileException
import org.jline.reader.UserInterruptException
import ren.natsuyuk1.comet.api.Comet
import ren.natsuyuk1.comet.api.cometInstances
import ren.natsuyuk1.comet.api.command.CommandManager
import ren.natsuyuk1.comet.api.command.ConsoleCommandSender
import ren.natsuyuk1.comet.api.config.CometConfig
import ren.natsuyuk1.comet.api.database.AccountDataTable
import ren.natsuyuk1.comet.api.database.DatabaseManager
import ren.natsuyuk1.comet.api.event.EventManager
import ren.natsuyuk1.comet.api.message.MessageSource
import ren.natsuyuk1.comet.api.message.buildMessageWrapper
import ren.natsuyuk1.comet.api.platform.LoginPlatform
import ren.natsuyuk1.comet.api.session.SessionManager
import ren.natsuyuk1.comet.api.task.TaskManager
import ren.natsuyuk1.comet.api.user.Group
import ren.natsuyuk1.comet.api.user.User
import ren.natsuyuk1.comet.api.wrapper.WrapperLoader
import ren.natsuyuk1.comet.config.branch
import ren.natsuyuk1.comet.config.hash
import ren.natsuyuk1.comet.config.version
import ren.natsuyuk1.comet.console.command.registerTerminalCommands
import ren.natsuyuk1.comet.console.util.Console
import ren.natsuyuk1.comet.console.util.login
import ren.natsuyuk1.comet.console.util.loginStatus
import ren.natsuyuk1.comet.consts.cometPersistDataFile
import ren.natsuyuk1.comet.consts.cometTables
import ren.natsuyuk1.comet.consts.defaultCommands
import ren.natsuyuk1.comet.network.CometServer
import ren.natsuyuk1.comet.service.CometCoreService
import ren.natsuyuk1.comet.utils.brotli4j.BrotliLoader
import ren.natsuyuk1.comet.utils.coroutine.ModuleScope
import ren.natsuyuk1.comet.utils.jvm.addShutdownHook
import ren.natsuyuk1.comet.utils.skiko.SkikoHelper
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

private val logger = mu.KotlinLogging.logger {}

private val dummyComet =
    object :
        Comet(LoginPlatform.TEST, CometConfig(0, "", LoginPlatform.TEST), logger, ModuleScope("dummy-comet")) {
        override val id: Long = 0
        override fun login() {}
        override fun afterLogin() {}
        override fun close() {}
        override suspend fun getGroup(id: Long): Group? = null
        override suspend fun deleteMessage(source: MessageSource): Boolean = false
        override suspend fun getFriend(id: Long): User? = null
        override suspend fun getStranger(id: Long): User? = null
    }

object CometTerminal {
    private var scope = ModuleScope("CometTerminal")

    fun init(parentContext: CoroutineContext) {
        scope = ModuleScope("CometTerminal", parentContext)
        CommandManager.init(scope.coroutineContext)
    }
}

class CometTerminalCommand : CliktCommand(name = "comet") {
    override suspend fun run() {
        scope.launch {
            setupShutdownHook()
            setupConsole()

            logger.info { "正在运行 Comet Terminal $version-$branch-$hash" }

            CometTerminal.init(scope.coroutineContext)

            setupConfig()
            setupDatabase()
            setupCommands()

            autoLogin()

            startService()

            if (terminalAvaliable()) {
                handleConsoleCommand()
            } else {
                logger.warn { "检测到不支持标准输入的环境, Comet 暂时不支持在这样的环境下完成操作, 请在支持的环境下完成这些操作后再复制相关文件." }
                while (scope.isActive) {
                }
            }
        }.join()
    }

    private fun setupConfig(): Job = scope.launch {
        logger.info { "加载配置文件..." }
        val loadScope = ModuleScope("ConfigLoaderCoroutine", scope.coroutineContext, Dispatchers.IO)
        cometPersistDataFile.map {
            loadScope.launch {
                it.init()
                it.save()
            }
        }.joinAll()
    }

    private fun setupDatabase() {
        logger.info { "加载数据库..." }
        DatabaseManager.loadDatabase()
        DatabaseManager.loadTables(*cometTables, AccountDataTable)
    }

    private fun setupCommands() = scope.launch {
        logger.info { "注册命令..." }
        if (terminalAvaliable()) registerTerminalCommands()
        CommandManager.registerCommands(defaultCommands)
        EventManager.init(coroutineContext)
    }

    private fun setupConsole() {
        Console.initReader()
        Console.redirectToJLine()
    }

    private suspend fun handleConsoleCommand() = scope.launch {
        while (isActive) {
            if (!loginStatus.value) {
                try {
                    CommandManager.executeCommand(
                        dummyComet,
                        ConsoleCommandSender,
                        ConsoleCommandSender,
                        buildMessageWrapper { appendText(Console.readln()) }
                    ).join()
                } catch (e: UserInterruptException) { // Ctrl + C
                    println("请使用 Ctrl + D 退出 Comet 终端")
                } catch (e: EndOfFileException) { // Ctrl + D
                    exitProcess(0)
                }
            }
        }
    }.join()

    private fun setupShutdownHook() {
        addShutdownHook {
            println("\n正在退出 Comet Terminal...")
            CometServer.stop()
            cometInstances.forEach {
                try {
                    it.close()
                } catch (e: Exception) {
                    logger.warn(e) { "无法正常关闭 Comet ${it.id} (${it.platform})" }
                }
            }
            runBlocking { cometPersistDataFile.forEach { it.save() } }
            closeAll()
            Console.redirectToNull()
        }
    }

    private fun startService() = scope.launch {
        TaskManager.init(coroutineContext)
        CometCoreService.init(coroutineContext)
        CometServer.init()
        SessionManager.init(coroutineContext)

        SkikoHelper.findSkikoLibrary()
        BrotliLoader.loadBrotli()
    }

    private fun autoLogin() {
        transaction {
            val accounts = AccountDataTable.selectAll()

            accounts.forEach {
                logger.info { "正在自动登录账号 ${it[AccountDataTable.id]} (${it[AccountDataTable.platform]})" }

                scope.launch {
                    try {
                        login(
                            it[AccountDataTable.id].value,
                            it[AccountDataTable.password],
                            it[AccountDataTable.platform],
                            it[AccountDataTable.protocol]
                        )
                    } catch (e: Throwable) {
                        logger.warn(e) { "登录 ${it[AccountDataTable.id]} (${it[AccountDataTable.platform]}) 失败" }
                    }
                }
            }
        }
    }

    companion object {
        internal val scope = ModuleScope("CometFrontendScope")

        internal fun closeAll() {
            scope.dispose()
            scope.cancel()
        }
    }
}

suspend fun main(args: Array<String>) {
    WrapperLoader.load()
    Thread.currentThread().contextClassLoader = WrapperLoader.wrapperClassLoader

    when (val result = CometTerminalCommand().main(args)) {
        is CommandResult.Success -> {
            exitProcess(0)
        }

        is CommandResult.Error -> {
            println(result.userMessage)
            exitProcess(1)
        }
    }
}

private fun terminalAvaliable(): Boolean =
    (System.getProperty("comet.no-terminal") ?: System.getenv("COMET_NO_TERMINAL")).isNullOrBlank()
