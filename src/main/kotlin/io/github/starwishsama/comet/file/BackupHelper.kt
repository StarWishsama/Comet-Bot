package io.github.starwishsama.comet.file

import cn.hutool.core.io.file.FileWriter
import io.github.starwishsama.comet.BotVariables
import io.github.starwishsama.comet.utils.TaskUtil
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object BackupHelper {
    private val location: File = File(BotVariables.filePath.toString() + "/backups")

    private fun createBackup(){
        try {
            if (!location.exists()) {
                location.mkdirs()
            }

            val backupTime = LocalDateTime.now()
            val backupName =
                    "backup-${backupTime.year}-${backupTime.month.value}-${backupTime.dayOfMonth}-${backupTime.hour}-${backupTime.minute}.json"

            val backupFile = File(BotVariables.filePath.toString() + "/backups/${backupName}")
            backupFile.createNewFile()
            FileWriter.create(backupFile, Charsets.UTF_8)
                .write(BotVariables.gson.toJson(BotVariables.users))
            BotVariables.logger.info("[备份] 备份成功! 文件名是 $backupName")
        } catch (e: Exception) {
            BotVariables.logger.error("[备份] 备份时出问题", e)
        }
    }

    fun scheduleBackup() =
        TaskUtil.runScheduleTaskAsync(0, BotVariables.cfg.autoSaveTime, TimeUnit.MINUTES, BackupHelper::createBackup)
}