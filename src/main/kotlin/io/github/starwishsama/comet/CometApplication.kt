package io.github.starwishsama.comet

import io.github.starwishsama.comet.BotVariables.cfg
import io.github.starwishsama.comet.BotVariables.comet
import io.github.starwishsama.comet.startup.CometRuntime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder

object CometApplication {
    val console: LineReader = LineReaderBuilder
        .builder().terminal(TerminalBuilder.builder().encoding(Charsets.UTF_8).build()).appName("Comet").build()
        .apply {
            setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION)
            unsetOpt(LineReader.Option.INSERT_TAB)
        }

    @JvmStatic
    fun main(args: Array<String>) {
        while (true) {
            CometRuntime.postSetup()

            comet.apply {
                id = cfg.botId
                password = cfg.botPassword
            }

            try {
                runBlocking {
                    comet.login()
                }

                CometRuntime.handleConsoleCommand()

                GlobalScope.launch {
                    comet.join()
                }
            } catch (e: CancellationException) {
                // 忽略
            }
        }
    }
}
