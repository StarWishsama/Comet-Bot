/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 MIT 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 * Use of this source code is governed by the MIT License which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/dev/LICENSE
 */

package ren.natsuyuk1.comet.cli.console

import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import java.io.OutputStream
import java.io.PrintStream

object Console {
    private val terminal: Terminal = TerminalBuilder.terminal()

    private var reader: LineReader? = null

    internal fun initReader() {
        reader = LineReaderBuilder.builder()
            .appName("CometTerminal")
            .terminal(terminal)
            .build()
    }

    fun readln(prompt: String = "> "): String = reader?.readLine(prompt) ?: error("Reader not prepared")

    fun println(any: Any?) {
        reader?.printAbove(any.toString()) ?: kotlin.io.println(any)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun println(string: String?) = println(any = string)

    internal fun redirectToJLine() {
        System.setErr(JLineRedirector)
        System.setOut(JLineRedirector)
    }

    internal fun redirectToNull() {
        val out = PrintStream(OutputStream.nullOutputStream())
        System.setOut(out)
        System.setErr(out)
    }
}
