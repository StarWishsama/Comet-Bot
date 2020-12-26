package io.github.starwishsama.comet.file

import cn.hutool.core.io.file.FileWriter
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.utils.FileUtil
import io.github.starwishsama.comet.utils.TaskUtil
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object BackupHelper {
    private val location: File = FileUtil.getChildFolder("backups")
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")

    private fun createBackup(){
        try {
            if (!location.exists()) {
                location.mkdirs()
            }

            val backupTime = LocalDateTime.now()
            val backupFileName = "backup-${dateFormatter.format(backupTime)}.json"

            val backupFile = File(location, backupFileName)
            backupFile.createNewFile()
            FileWriter.create(backupFile, Charsets.UTF_8)
                    .write(BotVariables.gson.toJson(BotVariables.users))
            BotVariables.logger.info("[备份] 备份成功! 文件名是 $backupFileName")
        } catch (e: Exception) {
            BotVariables.logger.error("[备份] 尝试备份时发生了异常", e)
        }
    }

    fun scheduleBackup() =
        TaskUtil.runScheduleTaskAsync(0, BotVariables.cfg.autoSaveTime, TimeUnit.MINUTES, BackupHelper::createBackup)
}