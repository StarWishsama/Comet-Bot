/*
 * Copyright (c) 2019-2021 StarWishsama.
 *
 * 此源代码的使用受 GNU General Affero Public License v3.0 许可证约束, 欲阅读此许可证, 可在以下链接查看.
 *  Use of this source code is governed by the GNU AGPLv3 license which can be found through the following link.
 *
 * https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE
 *
 */

package io.github.starwishsama.comet.file

import cn.hutool.core.io.file.FileWriter
import io.github.starwishsama.comet.CometVariables
import io.github.starwishsama.comet.CometVariables.cfg
import io.github.starwishsama.comet.CometVariables.daemonLogger
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.NumberUtil.fixDisplay
import io.github.starwishsama.comet.utils.NumberUtil.toLocalDateTime
import io.github.starwishsama.comet.utils.TaskUtil
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.time.toKotlinDuration

object DataSaveHelper {
    private val location: File = FileUtil.getChildFolder("backups")
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

    private fun createBackup() {
        try {
            if (!location.exists()) {
                location.mkdirs()
            }

            val backupTime = LocalDateTime.now()
            val backupFileName = "backup-${dateFormatter.format(backupTime)}.json"

            val backupFile = File(location, backupFileName)
            backupFile.createNewFile()
            FileWriter.create(backupFile, Charsets.UTF_8)
                .write(CometVariables.mapper.writeValueAsString(CometVariables.cometUsers))
            CometVariables.logger.info("[备份] 备份成功! 文件名是 $backupFileName")
        } catch (e: IOException) {
            CometVariables.logger.error("[备份] 尝试备份时发生了异常", e)
        }
    }

    fun scheduleBackup() =
        TaskUtil.scheduleAtFixedRate(
            cfg.autoSaveTime,
            cfg.autoSaveTime,
            TimeUnit.MINUTES,
            DataSaveHelper::createBackup
        )

    fun scheduleSave() =
        TaskUtil.scheduleAtFixedRate(cfg.autoSaveTime, cfg.autoSaveTime, TimeUnit.MINUTES, DataSetup::saveAllResources)

    fun checkOldFiles() {
        if (cfg.autoCleanDuration < 1) return

        var counter = 0
        var totalSize = 0.0

        val files = mutableListOf<File>()

        files.addAll(FileUtil.getChildFolder("logs").listFiles() ?: arrayOf())
        files.addAll(FileUtil.getErrorReportFolder().listFiles() ?: arrayOf())
        files.addAll(FileUtil.getChildFolder("backup").listFiles() ?: arrayOf())

        files.forEach { f ->
            val modifiedTime = f.lastModified().toLocalDateTime(true)
            val currentTime = LocalDateTime.now()
            if (Duration.between(modifiedTime, currentTime).toKotlinDuration().inWholeDays >= cfg.autoCleanDuration) {
                try {
                    totalSize += f.length()
                    f.delete()
                    counter++
                } catch (e: IOException) {
                    daemonLogger.warning("删除旧文件失败", e)
                }
            }
        }

        if (counter > 0) daemonLogger.info("已成功清理 $counter 个旧文件, 节省了 ${(totalSize / 1024 / 1024).fixDisplay()} MB")
    }
}
