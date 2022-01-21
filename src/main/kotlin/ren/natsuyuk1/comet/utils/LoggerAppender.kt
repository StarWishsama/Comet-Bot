/*
 * Copyright (c) 2019-2022 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package ren.natsuyuk1.comet.utils

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