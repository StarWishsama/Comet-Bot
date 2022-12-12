package ren.natsuyuk1.comet.objects.pjsk.local

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiLocalFileTable.fileName
import ren.natsuyuk1.comet.objects.pjsk.local.ProjectSekaiLocalFileTable.lastUpdateTime

object ProjectSekaiLocalFileTable : Table("pjsk_local_file") {
    val fileName = text("file_name")
    val lastUpdateTime = timestamp("last_update_time")
}

fun ProjectSekaiLocalFile.updateLastUpdateTime() =
    transaction {
        val triggerTime = Clock.System.now()
        val row = ProjectSekaiLocalFileTable.update(
            {
                fileName eq this@updateLastUpdateTime.file.nameWithoutExtension
            }
        ) {
            it[lastUpdateTime] = triggerTime
        }

        if (row == 0) {
            ProjectSekaiLocalFileTable.insert {
                it[fileName] = this@updateLastUpdateTime.file.nameWithoutExtension
                it[lastUpdateTime] = triggerTime
            }
        }
    }

fun ProjectSekaiLocalFile.getLastUpdateTime(): Instant =
    transaction {
        val triggerTime = Clock.System.now()
        val time = ProjectSekaiLocalFileTable.select {
            fileName eq this@getLastUpdateTime.file.nameWithoutExtension
        }.map {
            it[lastUpdateTime]
        }.firstOrNull()

        return@transaction if (time == null) {
            ProjectSekaiLocalFileTable.insert {
                it[fileName] = this@getLastUpdateTime.file.nameWithoutExtension
                it[lastUpdateTime] = triggerTime
            }
            triggerTime
        } else {
            time
        }
    }
