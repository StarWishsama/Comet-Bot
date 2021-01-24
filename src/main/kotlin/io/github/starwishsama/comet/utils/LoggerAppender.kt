package io.github.starwishsama.comet.utils

import java.io.File
import java.io.FileWriter

/**
 * [LoggerAppender]
 *
 * Logger 写入文件使用的类
 */
class LoggerAppender(file: File) {
    private val fileWriter: FileWriter

    init {
        if (!file.exists()) file.createNewFile()

        fileWriter = FileWriter(file)

        Runtime.getRuntime().addShutdownHook(Thread {
            fileWriter.flush()
            fileWriter.close()
        })
    }

    @Synchronized
    fun appendLog(log: String) {
        fileWriter.append(log.replace("\u001B\\[0m".toRegex(), "") + "\n")
        fileWriter.flush()
    }
}