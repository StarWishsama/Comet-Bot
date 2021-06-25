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

import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.utils.createBackupFile
import io.github.starwishsama.comet.utils.getContext
import java.io.File

abstract class DataFileEntity(
    val file: File,
) {
    abstract fun init()

    abstract fun save()

    fun exists(): Boolean = file.exists()

    fun createNewFile(): Boolean = file.createNewFile()

    fun check() {
        if (!exists() || (file.isFile && file.getContext().isEmpty())) {
            init()
        }

        daemonLogger.debug("加载资源 ${this.javaClass.simpleName}(${file.name})")
    }

    fun createBackup() {
        if (!file.isDirectory) {
            file.createBackupFile()
        }
    }
}
