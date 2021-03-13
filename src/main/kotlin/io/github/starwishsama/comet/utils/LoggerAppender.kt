package io.github.starwishsama.comet.utils

import io.github.starwishsama.comet.utils.StringUtil.withoutColor
import java.io.File
import java.io.FileWriter

/**
 * [LoggerAppender]
 *
 * Logger 写入文件使用的类
 */
class LoggerAppender(file: File) {
    private val fileWriter: FileWriter
    private var isClosed = false

    init {
        if (!file.exists()) file.createNewFile()
        fileWriter = FileWriter(file)
    }

    @Synchronized
    fun appendLog(log: String) {
        if (!isClosed) {
            fileWriter.append(log.withoutColor() + "\n")
            fileWriter.flush()
        }
    }

    fun close() {
        isClosed = true
        fileWriter.flush()
        fileWriter.close()
    }
}