package io.github.starwishsama.comet.utils

import java.io.File
import java.io.PrintWriter

class LoggerAppender(file: File) {
    private val fileWriter: PrintWriter

    init {
        if (!file.exists()) file.createNewFile()

        fileWriter = file.printWriter()

        Runtime.getRuntime().addShutdownHook(Thread {
            fileWriter.flush()
            fileWriter.close()
        })
    }

    fun appendLog(log: String) {
        fileWriter.write(log + "\n")
        fileWriter.flush()
    }
}