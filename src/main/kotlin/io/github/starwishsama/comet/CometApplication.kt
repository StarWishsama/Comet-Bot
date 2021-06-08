/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet

import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.comet
import io.github.starwishsama.comet.BotVariables.daemonLogger
import io.github.starwishsama.comet.startup.CometRuntime
import kotlinx.coroutines.runBlocking
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder
import org.jline.utils.InfoCmp
import kotlin.system.exitProcess

object CometApplication {
    val console: LineReader = LineReaderBuilder
        .builder()
        .terminal(
            TerminalBuilder.builder()
                .jansi(true)
                .encoding(Charsets.UTF_8)
                .build()
                .also { it.puts(InfoCmp.Capability.exit_ca_mode) }
        ).appName("Comet").build()
        .also {
            it.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION)
            it.unsetOpt(LineReader.Option.INSERT_TAB)
        }

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            CometRuntime.postSetup()
        } catch (e: Exception) {
            daemonLogger.serve("无法正常加载 Comet, 程序将自动关闭", e)
            exitProcess(0)
        }

        comet.apply {
            id = cfg.botId
            password = cfg.botPassword
        }

        try {
            runBlocking {
                comet.login()
            }

            CometRuntime.handleConsoleCommand()

            runBlocking {
                comet.join()
            }

        } catch (e: Exception) {
            // 忽略
        }
    }
}
