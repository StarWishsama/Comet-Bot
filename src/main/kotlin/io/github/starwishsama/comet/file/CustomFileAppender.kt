package io.github.starwishsama.comet.file

import io.github.starwishsama.comet.BotVariables
import org.hydev.logger.appenders.Appender
import org.hydev.logger.withoutFormat
import org.hydev.logger.withoutRGB
import java.io.File
import java.io.PrintWriter
import java.time.LocalDateTime

open class CustomFileAppender(file: File) : Appender() {
    var fileWriter: PrintWriter

    init
    {
        // Create formatter (File format defaults to csv)
        formatter = {
            listOf<Any>(BotVariables.yyMMddPattern.format(LocalDateTime.now()), it.prefix, it.level, it.msg.withoutFormat()).joinToString("|", "", "")
        }

        // File
        file.parentFile.mkdirs()
        if (!file.exists()) file.createNewFile()
        fileWriter = file.printWriter()

        // Save on close
        Runtime.getRuntime().addShutdownHook(Thread {
            fileWriter.flush()
            fileWriter.close()
        })
    }

    override fun logRaw(message: String)
    {
        fileWriter.write(message.withoutFormat().withoutRGB() + "\n")
        fileWriter.flush()
    }
}