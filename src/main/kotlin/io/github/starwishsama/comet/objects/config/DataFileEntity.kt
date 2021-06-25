/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.objects.config

import io.github.starwishsama.comet.utils.createBackupFile
import io.github.starwishsama.comet.utils.getContext
import java.io.File

abstract class DataFileEntity(
    val file: File,
    val priority: FilePriority,
) {
    abstract fun init()

    abstract fun save()

    fun exists(): Boolean = file.exists()

    fun createNewFile(): Boolean = file.createNewFile()

    fun check() {
        if (exists() && (file.isDirectory || file.getContext().isNotEmpty())) {
            return
        }

        if (priority >= FilePriority.NORMAL) {
            init()
        }
    }

    fun createBackup() {
        if (!file.isDirectory) {
            file.createBackupFile()
        }
    }

    enum class FilePriority {
        /**
         * 只有使用某些功能时才会生成
         */
        LOW,

        /**
         * 初始化时不需要立即生成
         */
        NORMAL,

        /**
         * 初始化时的必需文件
         */
        HIGH
    }
}
